# Decisiones de arquitectura

Este documento recoge las decisiones tomadas al migrar la app original de
Flutter (solo como referencia; ya se ha eliminado) a Compose Multiplatform.
Desktop es por ahora el único target completamente cableado; Android compila
contra el mismo código compartido.

## Objetivo

Mantener el comportamiento del producto y el capas de la app de Flutter, pero
expresarlo con las buenas prácticas de Kotlin Multiplatform: dominio puro,
adaptadores de datos intercambiables, un reproductor aislado y una UI que nunca
habla directamente con HTTP, el sistema de ficheros ni el motor de reproducción.

## Módulos y dirección de dependencias

```
:core      pure domain (models, repository contracts, watched rules, player contract)
:data      implementations (Stremio addons, Nuvio/Supabase, local JSON store)
:app:fonts   alternate typeface (IBM Plex Sans) assets as Compose Resources
:app:shared  Compose UI + ViewModels (depends on :core and :app:fonts)
:app:desktopApp  composition root: libmpv player, window control, main()
:app:androidApp  Android composition root (no-op player for now)
```

`ui -> domain` y `data -> domain` (inversión de dependencias): la capa de datos
implementa los contratos declarados en `:core`, y los composition roots son los
únicos que referencian `:data`. Cambiar un backend o el reproductor cambia un
módulo y el composition root, nunca la UI.

`:core` y `:data` son Kotlin Multiplatform (`commonMain` + `jvmMain` +
`androidMain`). El código de dominio y datos vive en `commonMain`; solo el motor
HTTP, el sistema de ficheros y el reproductor tocan source sets de plataforma a
través de `expect`/`actual` (`data/platform/Platform.kt`). Los helpers de
ficheros de JVM y Android comparten un source set `jvmSharedMain`
(`data/platform/PlatformFileIO.kt`), así que la lógica atómica de
leer/escribir/borrar vive una sola vez.

## Dominio (`:core`)

- **Los modelos son data classes planas**, sin mapeo de columnas de base de
  datos ni parsing de JSON. El parsing de filas vive en `:data`
  (`NuvioMappers.kt`), así que los modelos los puede producir cualquier backend.
- **Un repositorio por capacidad**, reflejando el refactor de Flutter:
  `AccountRepository`, `LibraryRepository`, `ProgressRepository`,
  `AddonRepository`, más los contratos de addons (`CatalogRepository`,
  `MetadataRepository`, `StreamRepository`, `SubtitleRepository`) y
  `RatingRepository`.
- **Observabilidad con corrutinas.** Donde Flutter usaba `Listenable`, los
  contratos de Kotlin exponen `StateFlow`. `AccountRepository` expone un único
  `StateFlow<AccountState>` (sesión, perfiles, perfil activo, carga y error);
  `LibraryRepository`/`ProgressRepository` exponen un contador de cambios
  `StateFlow<Long>` que las pantallas recogen para recargar. Esto elimina por
  completo el parche `SessionController`.
- **Los perfiles son opcionales** (`AccountState.requiresProfile`) y obligatorios
  cuando se requieren: no hay perfil por defecto, porque una escritura guardada
  en el perfil equivocado son datos incorrectos, no datos que faltan.
- **La lógica de "visto" es pura** (`watched/WatchedRule.kt`) y el resolver
  (`watched/WatchedBadges.kt`) mantiene el comportamiento original de
  concurrencia acotada, caché y segundo plano.
- **El inicio de sesión es una petición, no una firma.**
  `AccountRepository.signIn` recibe un `SignInRequest` (sealed; hoy solo
  `EmailPassword`) y el backend declara lo que ofrece mediante `signInMethods`,
  así que la UI de inicio de sesión renderiza el formulario correspondiente en
  vez de asumir email + contraseña. Añadir OAuth o un magic link significa un
  subtipo nuevo, no un cambio de contrato; un flujo de varios pasos
  (emparejamiento de dispositivo) aún necesitaría un objeto de sesión por encima.
- **El vocabulario compartido vive con los modelos.** `ContentTypes`,
  `CatalogExtras`, `ProgressThresholds` y `releaseYear()` nombran las cadenas de
  protocolo y los umbrales que comparan el dominio, los datos y la UI, para que
  una errata o un literal que se desvía no pueda cambiar el comportamiento.
- **`PlayerService` es un contrato**, con un `PlayerState` neutro y un handle
  `videoOutput: VideoOutput?` para que la UI pueda incrustar la superficie nativa
  sin depender del motor. `VideoOutput` es un marcador vacío; el `FrameSource` de
  desktop lo extiende.

## Datos (`:data`)

- **Cliente Ktor** (CIO en desktop, OkHttp en Android) en lugar de
  `package:http`.
- **kotlinx.serialization** con la API `JsonElement` y pequeños helpers
  defensivos de coerción (`JsonUtils.kt`), reflejando `json_utils.dart`: los
  payloads de terceros degradan a `null`/vacío en lugar de lanzar.
- **Los clientes de addons se agrupan.** `AddonClientPool` guarda un
  `StremioAddonClient` cacheado por URL de addon, compartido por los cuatro
  repositorios de Stremio, así que un manifiesto se pide una vez incluso cuando
  los catálogos se piden en paralelo.
- **Stremio**: `StremioAddonClient` más los cuatro repositorios. Mismas reglas
  que el original: la metadata es "el primero que responde gana" entre addons,
  streams/subtítulos se agregan en paralelo, y los valores `extra` de los
  catálogos se codifican manteniendo `=`/`&` sin codificar. La fila "similar"
  elige un catálogo que pueda filtrar por género y salta los catálogos cuyos
  extras obligatorios no se pueden aportar (un término de búsqueda, una lista de
  ids), y luego cae al catálogo genérico, para no dejar un título sin la fila.
- **Nuvio/Supabase**: HTTP crudo dividido por responsabilidad, en lugar de una
  única clase cliente: `NuvioTransport` (verbos y URL base), `NuvioDiscovery`
  (`/.well-known/nuvio` y la clave publicable), `NuvioAuth` (sesión, inicio de
  sesión, refresco, cierre) y `NuvioRest` (select/insert/delete de PostgREST y
  las RPC `sync_*`). Los repositorios toman la parte que necesitan. Los tokens de
  sesión viven en `data/`, nunca en el dominio.
- **Almacén local**: `JsonFileStore`/`JsonMapStore` y los stores de sesión /
  valoraciones / series vistas. La resolución de rutas es un `expect` de
  plataforma (`%APPDATA%\Agustream` en Windows, XDG en el resto). Las escrituras
  pasan por un fichero temporal hermano y se renombran en su sitio, así que una
  escritura interrumpida no puede dejar un documento truncado, y una escritura
  fallida se reporta en vez de tragarse. Los ciclos de leer-modificar-escribir de
  los repositorios locales se serializan con un `Mutex`. El fichero de sesión se
  restringe al usuario actual donde el SO lo permite; es endurecimiento
  best-effort, no un keychain.
- **Los ajustes** viven en `settings.json` como un objeto JSON plano con claves
  punteadas, como el `settings.json` de VS Code (`animations.enabled`,
  `player.resumePlayback`, `player.autoPlayNext`, `poster.size`,
  `library.poster.size`). `JsonSettingsRepository` lo lee de forma defensiva,
  conserva las claves desconocidas al escribir y puede `reload()` tras una
  edición manual; la pantalla de ajustes agrupa los toggles por categoría, edita
  el mismo fichero, muestra su ruta y puede abrirlo en el editor del SO.
- **Renovación de sesión**: `NuvioAuth` renueva el token antes de una petición
  cuando ha caducado, y `NuvioRest` renueva una vez más cuando el backend
  responde `401`. Las renovaciones se serializan con un mutex para que peticiones
  paralelas no refresquen dos veces. Los motores se configuran con timeouts
  finitos de conexión/petición/socket.
- **`BackendKind` + `createBackend`** es el único sitio que conoce las
  implementaciones concretas, reemplazando `backend_factory.dart` de Flutter.
- **I/O en paralelo y caché.** Las lecturas independientes se ejecutan juntas en
  lugar de en fila: Home carga el progreso, la biblioteca y la lista de catálogos
  de forma concurrente; Library carga la biblioteca y su progreso juntos; el
  resolver de badges lee las dos fuentes de progreso en paralelo y sus búsquedas
  de metadata son acotadas pero concurrentes; los chunks de `watched_items` se
  consultan juntos; los manifiestos de catálogo se piden en paralelo y se cachean
  por addon (un manifiesto es inmutable). La lista de addons se cachea por uuid
  de perfil, así que la sincronización de badges no la reconsulta para cada
  serie. Los ViewModels con scope de cuenta (Home, Library, Search (y su feed de
  discover), Calendar) esperan a un perfil antes de cargar en vez de cargar sin
  él y fallar.

## UI (`:app:shared`)

- **Sin framework de DI.** `AppServices` es un contenedor de datos provisto
  mediante `LocalAppServices`; los composition roots lo construyen. Es el
  equivalente directo del `InheritedWidget` `AppServices` de Flutter.
- **ViewModel + `StateFlow`.** Cada sección/ruta tiene un `ViewModel` que posee
  su estado y lanza trabajo en `viewModelScope`. La UI recoge con
  `collectAsStateWithLifecycle()`, así que la colección se detiene cuando una
  pantalla sale del ciclo de vida.
- **Comportamiento de pantalla compartido.** Las secciones de datos extienden
  `AccountScopedViewModel` (`ui/common`), que posee la regla "carga cuando hay
  sesión y un perfil elegido, limpia al cerrar sesión", y usan `WatchedBadgeSync`
  para la búsqueda de badges, así que ninguno se reimplementa por pantalla. Su
  observador de cuenta arranca en `Dispatchers.Main`, no en `Main.immediate`:
  este último se ejecuta inline cuando el ViewModel se construye en el hilo
  principal, lo que disparaba el hook de carga desde el constructor de la
  superclase antes de que existiera el estado de la subclase. Forzar un dispatch
  hace que la primera emisión espere a que termine la construcción.
- **Los tokens de diseño son datos, no código.** `theme/Tokens.kt` contiene las
  paletas (`BrandPalette` / `AppThemes`), el espaciado, los tamaños, los alphas y
  los scrims; `theme/AppTheme.kt`
  contiene solo el esquema de color de Material, la tipografía y las formas.
  `AppSizes` posee los anchos de contenido/formulario/mensaje y los tamaños de
  borde/progreso, `AppAlpha` posee toda la opacidad de overlays, y
  `Modifier.focusRing` (en `Interaction.kt`) da a cada control enfocable el mismo
  anillo de teclado.
- **Los iconos se vendorizan, no son una dependencia.** `ui/icons/AppIcons.kt`
  contiene los glifos de Material Symbols que usa la UI como `ImageVector`s
  (outlined y filled, más las flechas auto-espejadas), generados desde los SVG
  oficiales con los ejes por defecto. La app ya no depende de
  `materialIconsExtended` — congelado en 1.7.3 mientras el resto de Compose
  seguía avanzando — así que solo se empaquetan los glifos usados. La escala
  `AppIconSize` de `theme/Tokens.kt` es la única fuente del tamaño de icono:
  `md` (24dp) es el default de Material 3 para navegación, campos y listas,
  `sm` (20dp) es para glifos compactos y dentro de botones, y `lg` (28dp) es para
  controles prominentes sobre artwork. Todos los sitios de uso dimensionan cada
  icono mediante esos tokens, para que el rail, la barra inferior y los campos
  de texto queden en el mismo tamaño óptico.
- **Ciclo de vida de Compose.** `AgustreamApp` reutiliza el `ViewModelStoreOwner`
  del host cuando existe — una `Activity` de Android, para que los ViewModels de
  sección sobrevivan a un cambio de configuración — y cae a un owner con scope
  de composición donde no lo hay, como desktop. Los ViewModels de sección viven
  en él y sobreviven a los cambios de sección (reemplazando la retención de
  estado del `IndexedStack` de Flutter); la sección seleccionada y la pila de
  rutas viven en un `ShellViewModel` por la misma razón, y `PlatformBackHandler`
  (Android) desapila con el back del sistema. Las rutas apiladas tienen su propio
  `ViewModelStoreOwner` (`ContentArea`) que se limpia al desapilar, así que los
  ViewModels transitorios no se filtran.
- **Onboarding y arranque de sesión.** `AgustreamApp` resuelve la sesión
  persistida una vez, antes que nada: muestra un splash hasta que termina, luego
  el onboarding (sin sesión) o el shell, para que un usuario recurrente nunca vea
  el inicio de sesión durante un frame. Es una puerta dura, así que las secciones
  ya no llevan un estado "sin sesión": cerrar sesión vuelve al onboarding. El
  formulario se comparte con la tarjeta de cuenta de Ajustes y se guía por
  `signInMethods`, así que un backend que inicia sesión de otra forma renderiza
  otro formulario. El atajo de desktop `NUVIO_EMAIL` / `NUVIO_PASSWORD` se pasa
  como petición `autoSignIn`, aplicada solo cuando no hay sesión almacenada.
- **Layout adaptable.** `AgustreamApp` mide la ventana una vez con
  `BoxWithConstraints` y publica un `WindowSize` (compact / medium / expanded, en
  los breakpoints de Material de 600 y 840 dp) más el ancho de ventana, a través
  de `LocalWindowSize` / `LocalWindowWidth`, para que una superficie elija un
  layout sin medir nada por sí misma. Las ventanas compactas (teléfonos) reciben
  siempre una barra inferior solo-iconos y reservan su altura; las ventanas más
  anchas reciben siempre un rail de navegación izquierdo y reservan su ancho.
  Las rejillas y raíles de pósters reducen su ancho de celda en ventanas
  compactas (la elección small / default / large del usuario sigue aplicando), el
  hero de Home cambia el billboard ancho por el recorte del póster sin el logo
  del título, el billboard de detalle se limita en ventana expandida para que el
  contenido quede cerca, y el diálogo de streams se ajusta a la ventana de la app
  porque abre la suya. El billboard ancho de Home toma su altura del área de
  trabajo de la pantalla (`HOME_HERO_SCREEN_FRACTION`, 55% de ella), no de la
  ventana, así que conserva la altura que tiene en una ventana maximizada:
  redimensionar en cualquier eje solo revela más o menos artwork, y las
  secciones de abajo se quedan en su sitio. Las secciones empiezan justo debajo
  del billboard, sin solaparse: el antiguo tirón hacia arriba sobre el fondo
  difuminado del hero se ha eliminado, así que una fila también puede quedar
  cortada por el borde del viewport. Las cabeceras de sección mantienen también
  una altura fija: el título la fija y el
  icono "ver todo" de la derecha se centra en ella y desborda hacia el padding,
  así que el hueco con la fila de abajo es el mismo con y sin botón; un subtítulo
  opcional (el Movies / Series del catálogo) se apoya en la línea base del
  título. Las ventanas compactas también usan un padding de pantalla más ajustado
  (16dp) y un gap de rejilla menor, y las acciones de detalle son solo-icono (con
  `contentDescription`) a la altura del call-to-action, para que la fila quede
  alineada.
- **El hero de Home toma de todos los catálogos.** `pickFeatured` (en
  `HomeViewModel`) arma el pool con los ítems de todas las filas, deduplica por
  `type:id`, balancea películas y series y baraja, así que el billboard no queda
  atado al primer catálogo. Los títulos bien calificados (IMDb >= 6, desconocido
  pasa) van primero y los de baja calificación solo se usan cuando no alcanza el
  buen contenido. Los seis slides auto-avanzan cada `HERO_AUTO_ADVANCE_INTERVAL`
  (7 s, dentro del rango usual de 5-8 s para carruseles), y un cambio manual
  pausa esa rotación durante `HERO_MANUAL_COOLDOWN` (60 s): el watcher se apoya en
  `PagerState.isScrollInProgress` y marca todo scroll que el carrusel no inició
  él mismo, así cubre tanto un drag en teléfono como un swipe de rueda / trackpad
  en desktop, y el timer cede ante el usuario hasta que queda inactivo, momento en
  que el hero vuelve a moverse solo. La pantalla vuelve a
  elegir el set completo cada `FEATURED_ROTATION_INTERVAL` (10 minutos) mientras
  Home está visible, priorizando los ítems que no están en pantalla, así cambia
  con el tiempo y no en cada recomposición. La selección usa un `Random`
  inyectable, así los tests son deterministas.
- **Los billboards comparten una gramática de copy.** El billboard de Home y el
  hero de detalle renderizan título, metadata, sinopsis y géneros a través de los
  mismos composables (`MediaTitle`, `MetadataRow`, `HeroSynopsis`, `HeroGenres` y
  el builder `metadataParts`, todos en `ui/components/MediaHero.kt`), así que las
  dos pantallas no pueden desviarse en tipo, color, separadores ni medida: la
  sinopsis se limita a `AppSynopsis.maxWidth` dentro de `HeroSynopsis`, y
  `AppType.heroBody` la dimensiona y pondera (17sp / 15sp en teléfono, Normal) en
  ambos. Solo difiere el orden por pantalla: el billboard de Home lee título,
  metadata, sinopsis y luego acciones, mientras que el de detalle mantiene las
  acciones por encima de la sinopsis. También difieren donde difiere el
  contenido: el billboard de Home integra los géneros en la línea de metadata
  (`release • genres • ★ rating`) mientras que el hero de detalle mantiene ahí la
  duración y muestra los géneros bajo la sinopsis; y el copy de Home usa un ritmo
  vertical más ajustado porque se apoya sobre artwork recortado. La metadata es
  un único builder, así que la estrella, los separadores y el formato de la
  valoración no pueden divergir.
- **Los billboards comparten sus acciones de icono.** `HeroActionRow` y
  `HeroActionState` (`ui/components/HeroActions.kt`) renderizan los botones de
  guardar, puntuar, visto e info junto al call to action, así que Home y el
  detalle no pueden desviarse en icono, tamaño, color ni estado. El billboard de
  Home muestra guardar y el "More info" solo-icono; el detalle añade puntuar y
  visto. El guardado de Home es optimista en `HomeViewModel` y se reconcilia con
  `LibraryRepository.changes`, así que un título guardado en cualquiera de las
  dos pantallas se ve como guardado en ambas.
- **Edge-to-edge e insets del sistema.** Android ejecuta la activity edge-to-edge
  (`enableEdgeToEdge`, con iconos de barra de sistema claros forzados porque la
  app siempre pinta oscuro), y el `Scaffold` ya no reserva las barras del sistema
  (`contentWindowInsets` es cero). Las pantallas colocan los insets ellas mismas
  mediante `safeTopInset` / `safeBottomInset` (`ui/theme/WindowSize.kt`,
  respaldados por `WindowInsets.statusBars` / `navigationBars`, cero en desktop)
  o los modificadores `statusBarsPadding` / `navigationBarsPadding` /
  `safeDrawingPadding`. El shell reserva la navegación más las barras del sistema
  tras las que se sienta para una sección (la barra inferior en un teléfono, el
  rail en ventana ancha); las barras de navegación llevan su propio inset; el
  fondo del detalle, el reproductor y las puertas de onboarding/perfil pintan
  tras las barras mientras sus controles se sientan debajo; y el billboard de
  Home se desplaza con el contenido (nunca se fija). En desktop el artwork abarca
  el 75% del ancho de la ventana, anclado a la esquina superior derecha de un
  marco de altura fija, y se recorta (`Crop`, alineado arriba); ensanchar la
  ventana revela más artwork en lugar de estirar el billboard, así que las
  secciones de abajo mantienen su sitio. Lo lavan degradados de
  `BrandPalette.background`: un fundido izquierdo que disuelve el borde izquierdo
  del artwork bajo el copy, un fundido inferior largo (transparente en la mitad
  superior, luego una rampa suave de dos tramos) que se funde con "Continue
  watching" sin costura dura, un fundido superior sutil, una viñeta en el borde
  derecho, y un `AppAlpha.HERO_IMAGE_DIM` plano mantenido sobre el artwork para
  que se lea más oscuro que la imagen cruda. En un teléfono el billboard abarca
  el ancho completo, dimensionado al aspect ratio del artwork. La columna de copy
  está anclada abajo a 100dp del borde del hero, hasta 840dp de ancho, y comparte
  el padding izquierdo de las secciones. La sinopsis es un teaser — limitada a la
  medida compartida de 720dp (`AppSynopsis.maxWidth`, unos 80 caracteres), tres
  líneas como máximo, el resto tras "More info". El mismo límite vive en
  `HeroSynopsis`, así que Home y Detail envuelven la descripción al mismo ancho.
  El paginador del carrusel es una fila de puntos iguales, centrada en el borde
  inferior del billboard: blanco para la página activa y gris para el resto. El
  swipe hace cross-fade en vez de deslizar: cada página cancela el offset
  horizontal del pager en un `graphicsLayer` y su alpha sigue el mismo offset,
  así que un billboard se disuelve a través del fondo de página en el siguiente.
  El copy (título, metadata, sinopsis, acciones) se desliza un poco contra el
  swipe (`HERO_CONTENT_SLIDE_MAX`, 64dp) para que se lea la dirección, mientras
  que el artwork solo hace cross-fade. La página enfocada se eleva con `zIndex`,
  para que sus botones posean los hits; los vecinos difuminados se sientan en el
  mismo sitio y si no capturarían el click. En ventana ancha el rail flota sobre
  el contenido, que reserva su ancho, así que la navegación nunca lo solapa.
- **Tipografía.** Inter se incluye mediante Compose Resources en `:app:shared` y
  es la tipografía renderizada. IBM Plex Sans queda aislada en su propio módulo
  `:app:fonts` para que las dos tipografías se puedan intercambiar para comparar
  mediante `USE_IBM_PLEX_SANS` en `AgustreamTheme`. Cada target renderiza la
  misma tipografía, y el texto se escribe en `sp` para que el tamaño de fuente
  del sistema de Android siga funcionando. `LocalDensity` nunca se sobrescribe
  deliberadamente: ese es el hack común, y miente a cada componente sobre la
  escala de fuente. Los roles `display*` de Material están dimensionados para
  pantalla grande, así que `AppType` los baja un rol en ventana compacta (título
  de hero, títulos de pantalla, sinopsis de hero); desktop y tablet mantienen la
  escala completa. Toda la escala de Material corre más ligera que la de serie
  (`Bold` a `Medium`, `SemiBold` a `Normal`), así que los encabezados se sientan
  en Medium mientras títulos, labels y cuerpo se sientan en Normal; `body*` llega
  como mínimo a `Normal` porque no se incluye ningún peso más ligero, así que un
  cuerpo realmente más fino necesita añadir `inter_light.ttf` a `:app:shared` (o
  `ibm_plex_sans_light.ttf` a `:app:fonts` mientras se compara). Ningún slot de
  tipo lleva color: el color del texto viene del esquema de color en el punto de
  llamada, como pretende Material. La paleta de lectura está basada en blanco —
  `BrandPalette.textPrimary` es `#FFFFFF` y los roles secundarios (`textSecondary`
  para `onSurfaceVariant`, `posterYear`) son blanco con alpha reducido — así que
  la jerarquía viene de la opacidad en lugar de un segundo gris, y el acento
  ámbar queda reservado para marca/atención en vez de copy de cuerpo.
  `PosterCaption` sube `titleSmall` / `bodySmall` 1sp con el peso de meta en
  Medium, y el subtítulo de sección se sienta también en Medium.
- **Localización.** Cada cadena visible al usuario vive en
  `composeResources/values/strings.xml` y se lee con
  `stringResource(Res.string.key)`, así que añadir un idioma es una carpeta
  `values-*` nueva en vez de un cambio de código. Los mensajes de fallo pasan por
  `Throwable.toUserMessage()`, que devuelve un `StringResource` (no un `String`)
  para que el mapeo siga siendo testeable mientras el llamador resuelve el texto.
  Solo el formato independiente del idioma queda en código: separadores,
  timecodes, tamaños de fichero, tipos de contenido y el ensamblado de la fecha
  de estreno. Los chevrons direccionales pasan por `Modifier.mirrorInRtl()`, ya
  que Material no trae un chevron auto-espejado; las flechas usan los iconos
  `AutoMirrored`.
- **La navegación** es una pequeña pila tipada (`ContentNavigator` +
  `ContentRoute`) renderizada dentro del área de contenido del shell. Las
  secciones mantienen la navegación visible; una ruta apilada (detalle, catálogo)
  la oculta y pinta su propio fondo, así que un hero inmersivo como el de detalle
  llena toda la ventana sin que la sección de debajo se transparente — el mismo
  comportamiento que el `Navigator` anidado de Flutter, sin añadir una librería
  de navegación. El chrome mezcla Material y custom: una ventana compacta recibe
  una `NavigationBar` con `NavigationBarItem`s solo-iconos; una ventana ancha un
  rail de navegación izquierdo con items solo-iconos custom. El rail elimina las
  etiquetas para mantenerse estrecho, centra el avatar de perfil y los items de
  sección en la ventana, y pinta el fondo de página en vez de un color de panel,
  así que se funde con el contenido; el item seleccionado es el tinte ámbar de
  marca sobre un relleno ámbar tenue (el mismo tratamiento `selected` que
  `AppIconButton` y `ActionButton`), con la capa de estado hover/press mezclada
  por encima para que un item seleccionado nunca pierda su relleno. La selección
  se ve desde el primer frame en vez de aparecer solo cuando el item recibe foco.
- **Search es dueña del feed de discover.** Tiene una búsqueda de texto libre en
  cada catálogo que declara el extra `search`, con debounce, ejecutada en
  paralelo y deduplicada entre addons, dividida en raíles Movies / Series /
  Other; y — mientras la consulta está vacía — el feed de discover debajo de la
  barra. El feed aplana cada catálogo que declaran los addons activos en una
  única rejilla mixta, como la Library, para que los límites de catálogo queden
  invisibles. Solo se usan catálogos navegables: uno cuyos extras son
  obligatorios (un término de búsqueda, una lista de ids, ...) no se puede pedir
  sin una consulta, así que se salta en vez de pedir y obtener una página vacía.
  Los catálogos se intercalan entre addons antes del límite, así que un addon con
  muchos catálogos no desplaza a los demás, y los items se intercalan round-robin
  y se deduplican, así que la rejilla alterna fuentes en vez de mostrar un
  catálogo como un bloque. Pagina con `skip` según se desplaza la rejilla, así
  que sigue creciendo en vez de parar en la primera página, y sigue paginando
  mientras un filtro estrecho no haya dado suficientes items para mostrar. Dos
  dropdowns — el tipo de título (All / Movies / Series) y el género — estrechan
  tanto el feed como los resultados de búsqueda; el menú de géneros se construye
  a partir de los géneros de los items que se han cargado, así que nunca ofrece
  uno que los addons no devuelvan. Discover ya no es una sección. **Calendar**
  pinta una rejilla mensual con los próximos episodios de las series de la
  Library; ver la limitación más abajo.
- **El tema** sigue Material 3 y es dato, no código. `BrandPalette`
  (`theme/Tokens.kt`) es una data class con los colores crudos de un tema,
  `AppThemes` mapea el `AppTheme` persistido a una paleta, y `AgustreamTheme(theme)`
  la elige y la proporciona por `LocalBrandPalette`, y luego construye el
  `darkColorScheme` completo a partir de ella (niveles surface-container,
  `outline`/`outlineVariant`, `inverse*` y los contenedores de error). La paleta
  es interna y solo rellena el esquema, así que los componentes de serie de
  Material3 y las superficies custom quedan en los mismos colores; `tertiary`
  refleja el acento, así que ningún tono frío se cuela en un tema cálido. Los
  scrims y el chrome nativo de la ventana también leen `LocalBrandPalette`, así
  que un cambio de tema repinta los degradados del hero y la barra de título en
  el mismo frame. `AppThemes.Coffee` es el tema por defecto y único por ahora: una
  paleta cálida crema/ámbar/naranja quemado, elegida en Ajustes → Apariencia →
  Tema y persistida como `appearance.theme` en `settings.json`. La tipografía
  Inter cargada desde Compose Resources y la escala de formas pequeña y tirando a
  cuadrada (`AppCorner` alimenta `MaterialTheme.shapes`: 2/4/6/8/12dp; la app se
  lee cuadrada, no redondeada) no dependen del tema. La UI lee colores semánticos
  de `MaterialTheme.colorScheme`; `AppColors` guarda los overlays de media para
  los que el esquema no tiene rol: `mediaScrim` y el indicador del carrusel del
  hero (apagado cuando está inactivo, crema cuando está activo).
  `AppSizes.controlHeight` / `minTouchTarget` mantienen los targets
  interactivos en el mínimo de 48dp de Material. Las superficies clicables custom
  dibujan la capa de estado ripple de Material (`clickableSurface`), así que el
  feedback de foco y pulsación sigue visible, y llevan rol de botón para que una
  superficie custom se anuncie correctamente. Los tiles de póster renuncian a esa
  capa de estado (`indication = null`) y señalan el hover con la animación de
  escala/borde, para que un póster con hover no se agrisé. Los paneles de ajustes
  usan el `Card` de Material. Una pequeña capa de componentes en `ui/components`
  mantiene los defaults de Material fuera de la pantalla: `ActionButton` elige su
  contenedor, contenido y colores deshabilitados de un peso `ActionButtonStyle`
  (Filled sobre la superficie inversa, Accent sobre el `primary`/`onPrimary`
  ámbar para el "Watch Now" de los billboards, Tonal sobre un relleno translúcido
  de on-surface, Outline sobre `outline`, Text solo etiqueta), deja el color de
  contenido al `LocalContentColor` del propio botón para que una etiqueta
  deshabilitada se atenúe con su contenedor, fija la elevación a cero (la app es
  plana), fija la etiqueta a un `labelLarge` en SemiBold (la escala
  tipográfica fija todos los slots en Normal, así que los botones vuelven a
  optar por él), un tamaño más en un call to action, con un glifo que escala por
  énfasis — 24dp en un call
  action, 20dp inline — la limita a una línea con elipsis, y baja ligeramente
  todo el control mientras se presiona; un flag `selected` vuelve ámbar los
  pesos de baja
  énfasis para el selector de temporadas, y un `trailingIcon` opcional lleva la
  flecha del filtro desplegable. `AppTextField` fija los campos de formulario a
  `shapes.small`, un contenedor `surfaceContainerLow` y un borde ámbar al
  enfocar, con una variante `AppSearchField` (lupa a la izquierda, acción de
  limpiar) para Search y streams. `AppIconButton` es el único control de icono —
  cabeceras de media, el reproductor (atrás, play/pausa, avance, subtítulos,
  audio, pantalla completa y el botón de play centrado), los toggles de
  guardar/puntuar/visto del detalle y las acciones de limpiar/contraseña de los
  campos — así que todos reciben el target de 48dp, un `mediaScrim` opcional y
  un atenuado de carga, y llevan su estado en el glifo y no en la superficie: sin
  caja de fondo ni ripple; el ícono se ilumina en hover o foco y se vuelve ámbar
  al seleccionarse; ninguna pantalla llama a un `IconButton`,
  `TextButton` u `OutlinedButton` de Material en crudo. Cada control lee solo
  roles del esquema y tokens compartidos, así que ninguno se estiliza en el punto
  de llamada. El foco se reserva para teclado y D-pad: el anillo compartido del
  ancho `AppSizes.focusRing` se dibuja solo cuando `LocalInputModeManager`
  reporta un modo distinto de touch, así que el clic de mouse conserva el look
  limpio sin caja mientras un control remoto o un gamepad muestra el target; y
  los solo-icono (`AppIconButton`, el rail) además iluminan el glifo en hover o
  foco. El fondo de
  página casi negro (`#0B0803`) es la única fuente de verdad: el esquema, el
  fondo nativo de la ventana y cada degradado de fundido de página terminan en
  él. La app es deliberadamente **solo oscura** — cada `AppTheme` es una paleta
  oscura, y no hay esquema claro ni color dinámico — porque la UI está construida
  alrededor de artwork oscuro y scrims;
  el reproductor mantiene una paleta fija de controles blanco-sobre-negro sobre
  el vídeo. La `visualDensity` de Material3 queda en el default de plataforma.
- **Imágenes con Coil 3**, el cargador de imágenes multiplataforma. El artwork
  remoto pasa por dos composables compartidos en `ui/components`: `PosterImage`
  para pósters y thumbnails, y `RemoteImage` para el resto (backdrops, arte de
  hero, fotos de reparto, logos de título). Ambos fijan `ContentScale` y
  `FilterQuality`, porque Coil dibuja con `DefaultFilterQuality` (bilineal) y
  aliasea un póster reducido. El artwork usa `FilterQuality.Medium` (bilineal con
  mipmap), que es el muestreo correcto para minificación, y los logos usan
  `FilterQuality.High` (bicúbico) para que sus bordes queden nítidos. En desktop
  el lado de decodificación también está corregido: el `SkiaImageDecoder` de Coil
  3.6.3 redimensiona con `SamplingMode.DEFAULT`, que Skiko define como vecino más
  cercano, así que `app/desktopApp` instala `MitchellImageDecoder`, un
  decodificador cúbico que mantiene las matemáticas de tamaño de Coil y reduce
  una vez en el momento de decodificar. Juntos afilan el artwork y reducen la
  memoria de bitmap que el renderer tiene que escalar cada frame. Los logos de
  título se contienen en una caja — una altura óptica fija más una guarda
  `maxWidth` (`AppLogoSize`), dibujados con `ContentScale.Fit` — para que un logo
  cuadrado no se eleve sobre el copy y uno muy ancho no desborde su columna. El
  hero de Home precarga cada backdrop y logo destacado a través del loader
  singleton, dimensionado al billboard, para que deslizar el carrusel pinte desde
  caché en vez de empezar una vuelta de red a mitad de gesto.
- **El movimiento** está centralizado en `ui/theme/Motion.kt`: `AppMotion` guarda
  las duraciones y tanto las easings utilitarias como las emphasized de Material
  (las pantallas que entran usan emphasized decelerate, las que salen emphasized
  accelerate), y `LocalMotionEnabled` controla cada animación. Las specs
  construidas con `AppMotion.spec`/`motionTween` colapsan a duración cero cuando
  está apagado, y `Modifier.motionLayer` aplica escala y alpha en la fase de
  dibujo, así que el movimiento nunca relayouta ni recompone por frame. Las
  pestañas cambian sin transición; la sección queda compuesta mientras una ruta
  está abierta (para que su scroll nunca se guarde y restaure, lo que perdía el
  offset dentro del item y lo subía) y la ruta se funde encima, saliendo de
  inmediato al desapilar. El rail flota sobre el contenido en vez de tomar ancho
  de layout, porque ocultarlo agrandaba el viewport de contenido y recortaba el
  scroll de la sección; las secciones reservan su ancho con un padding constante,
  así que el viewport nunca cambia. La animación de tamaño está desactivada
  (`.using(null)`) porque relayouta cada frame. Los pósters escalan al hover y
  los diálogos se funden/escalan al entrar. Las pantallas de carga son skeletons
  movidos por un único shimmer leído en la fase de dibujo: la rejilla de pósters
  deriva su número de celdas del viewport, así que nunca se detiene a mitad de
  una ventana ancha, y los stops de degradado estáticos se construyen una vez en
  vez de cada frame. Home no tiene skeleton a propósito: no pinta nada hasta que
  llegan los datos, en vez de mostrar un placeholder de billboard que luego
  cambia de forma. El hero de detalle no pinta el fallback de la biblioteca como
  si fuera el artwork final: reserva el espacio del logo sin dibujar un
  placeholder, y el logo y el backdrop se funden cuando la imagen está lista
  (`RemoteImage(fadeIn)`), así que una carga lenta nunca aparece de golpe. El
  reproductor está deliberadamente fuera de la transición de ruta porque maneja
  un único motor compartido.
- **Los tamaños de póster** son un ajuste (`PosterSize`: small / default / large,
  210 / 230 / 250 dp). `poster.size` lo comparten Home, el feed de discover, las
  rejillas de catálogo y "More like this", mientras que `library.poster.size`
  aplica solo a la rejilla de Library. Una rejilla usa el tamaño como su ancho
  mínimo de celda y un raíl deriva su altura de él (`width / 0.58`), así que el
  aspect de la tarjeta es el mismo en todas partes y cada superficie se puede
  redimensionar desde una sola clave.
- **Ventana**: la app mantiene la **decoración nativa de ventana** (barra de
  título, minimizar, maximizar, cerrar y redimensionar son gestionados por el
  SO), lo que evita los glitches que una barra de título custom puede causar en
  Windows. Compose Desktop dibuja sus propios resizers solo cuando la ventana no
  tiene decoración, así que no hace falta nada custom. La ventana se abre a
  1280x720 y ese es también su **mínimo** (fijado como `minimumSize` en la
  ventana AWT), así que el SO la deja crecer y maximizar pero nunca encoger por
  debajo del tamaño con el que empezó. El único comportamiento de ventana
  enrutado por `WindowController` es **pantalla completa**, que posee el
  reproductor; la colocación anterior se restaura al salir. También expone la
  altura del área de trabajo de la pantalla, la referencia desde la que se
  dimensiona el billboard de Home. La app mantiene la
  **decoración nativa de ventana** y solo la tematiza: en Windows opta por el
  modo oscuro inmersivo de DWM y, en Windows 11, pinta el caption, el texto y el
  borde desde `AppWindowChrome` y fuerza esquinas redondeadas
  (`WindowAppearance.kt`), porque AWT no sigue el modo oscuro del SO por sí solo.
  Mantener el marco nativo es lo que preserva snap, maximizar, las animaciones de
  ventana y el alto contraste, y coincide en Linux, donde el gestor de ventanas
  dibuja la decoración.

## Reproductor

`PlayerService` lo implementa en desktop `MpvPlayerService`, que carga **libmpv**
en proceso mediante JNA, en el composition root para que el motor quede fuera de
la UI compartida. El vídeo se produce con la **API de render** de libmpv (backend
software) y se entrega a la UI como bitmaps de Compose a través de `FrameSource`,
así que el vídeo es contenido Compose normal y los controles Compose quedan
encima; una superficie nativa `wid` los taparía. El resto (comandos, posición,
pistas) pasa por la API cliente de libmpv. En Windows el `libmpv-2.dll` se
incluye con la app; en Linux se usa el `libmpv.so` del sistema. La librería se
carga de forma perezosa, así que una máquina sin ella degrada a un error de
reproducción claro. El reporte de progreso, el resume, la selección de
subtítulos/audio y el autoplay del siguiente episodio están portados del
original. El renderer software está limitado por CPU; pasar al backend de render
OpenGL es el paso de rendimiento planeado.

## Testing

`commonTest` cubre el dominio puro (playback target, reglas de visto, parsing de
año, resolver de badges con fakes) y los parsers/mappers defensivos. `jvmTest`
cubre los stores de fichero y los repositorios locales. El `commonTest` de
`:app:shared` cubre los ViewModels de sección con repositorios en memoria y
`Dispatchers.setMain`.

## Diferencias conocidas / limitaciones

- El reproductor de desktop necesita libmpv: en Windows está incluido
  (`libmpv-2.dll`, extraído de los recursos de la app), en Linux hay que instalar
  el runtime `libmpv.so`. `AGUSTREAM_LIBMPV` puede apuntar a una librería
  concreta.
- Android usa un reproductor no-op hasta que se añada una superficie de vídeo
  nativa. El resto del target está cableado: permiso `INTERNET`, un directorio de
  app basado en `Context` y una sesión persistida.
  `AppServices.playbackSupported` es `false` para el reproductor no-op, así que
  la UI informa de que la reproducción no está disponible en vez de abrir un
  reproductor que solo da error.
- Las pantallas de metadata/edición de addons de Nuvio siguen siendo
  placeholders, como lo eran en la app de Flutter.
- **Calendar** usa una convención de Cinemeta cuando un addon la expone: un
  catálogo `calendar-videos` (Cinemeta y AIOMetadata lo declaran) devuelve
  `metasDetailed` — la meta completa por serie — para las series de la biblioteca
  en una sola petición, que es por lo que existe
  `CatalogRepository.detailedItems`. Cuando ningún addon la expone, la sección
  cae a resolver cada serie a través de los addons de metadata tras un semáforo,
  para que los primeros episodios aparezcan sin esperar al más lento. En
  cualquier caso solo se conservan los episodios dentro de una ventana móvil de
  180 días, se saltan los episodios sin fecha de estreno, y la rejilla llena la
  ventana en vez de desplazarse.

## Backlog de mejoras

Elementos que necesitan una decisión de diseño, tocan varios módulos, o ambas
cosas, así que no son un cambio de un solo fichero. Están agrupados por el eje
que cambian, no por esfuerzo.

### Backends

- **Stremio es un backend completo, no solo un login.** Cambiar Nuvio por
  Stremio ya encaja en la costura `BackendKind` + `createBackend` y no necesita
  cambios en `ui/`, pero una cuenta usable también necesita `LibraryRepository`,
  `ProgressRepository` y `AddonRepository` contra el datastore de Stremio. El
  adaptador de auth es la parte pequeña; los repositorios de datos son el grueso.

### Producto / UX

- **Pulido móvil y endurecimiento de release.** El layout compacto funciona,
  pero aún no está ajustado superficie por superficie: el espaciado y las
  proporciones todavía necesitan una pasada en teléfonos reales. Por separado, el
  build de release de Android está sin optimizar — `isMinifyEnabled` es `false` y
  no hay baseline profile — así que se lee mucho más lento de lo necesario.
- **Navegación con teclado / D-pad.** `Interaction.kt` cubre estados hover,
  focus y pressed, y el reproductor tiene atajos, pero no hay recorrido de foco
  por los raíles de Home ni modelo de navegación espacial. Un target de TV lo
  hace obligatorio en vez de deseable.

### Componentes de UI

Seguimientos de la revisión de la capa de componentes (`ActionButton`,
`AppTextField`/`AppSearchField`, `AppIconButton`).

- **Decidir la altura del campo.** `AppTextField` mantiene los 56dp de Material
  mientras los botones usan `AppSizes.controlHeight` (48dp). O expón un
  `AppSizes.fieldHeight` y documenta la diferencia, o alínealos.
- **Recortar la superficie no usada restante.** `AppTextField.supportingText`
  sigue sin usarse hoy; mantenlo solo donde se planee un call site real, para
  que la API no acumule ramas muertas.

### Rendimiento del reproductor

- **Backend de render OpenGL.** Como se anota en la sección del reproductor, el
  renderer software actual entrega frames a Compose como bitmaps y está limitado
  por CPU. Pasar al backend de render OpenGL de libmpv es el paso de rendimiento
  planeado y necesita un contexto GL cableado en el composition root.
