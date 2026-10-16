// Regenerates ui/icons/Boxicons.kt from the Boxicons set on Iconify.
// Source: @boxicons (MIT). Run:
//   node scripts/gen_boxicons.mjs > app/shared/src/commonMain/kotlin/com/misgus/pengu/ui/icons/Boxicons.kt

const API = "https://api.iconify.design/boxicons.json";

// [Kotlin name, Iconify name, autoMirror]
const icons = [
  ["Home", "home-filled", false],
  ["Search", "search-big", false],
  ["Bookmark", "bookmark-filled", false],
  ["Calendar", "calendar-alt-filled", false],
  ["Settings", "cog-filled", false],
  ["Play", "play-filled", false],
  ["Plus", "plus-filled", false],
  ["Check", "check-filled", false],
  ["CheckCircle", "check-circle-filled", false],
  ["CheckCircleOutline", "check-circle", false],
  ["Star", "star-filled", false],
  ["StarBorder", "star", false],
  ["ArrowBack", "arrow-left", true],
  ["ChevronLeft", "chevron-left", false],
  ["ChevronRight", "chevron-right", false],
  ["ChevronDown", "chevron-down", false],
  ["Close", "x", false],
  ["Pause", "pause", false],
  ["Refresh", "refresh-cw", false],
  ["Forward", "fast-forward", false],
  ["Replay", "rewind", false],
  ["Fullscreen", "fullscreen", false],
  ["Volume", "volume-full", false],
  ["Visibility", "eye", false],
  ["VisibilityOff", "eye-slash", false],
  ["Subtitles", "captions", false],
  ["Audiotrack", "music", false],
  ["Info", "info-circle", false],
  ["Error", "alert-circle", false],
  ["Compass", "compass", false],
  ["FolderOpen", "folder-open", false],
  ["Inbox", "inbox", false],
  ["Movie", "movie", false],
  ["User", "user", false],
  ["SearchOff", "search-alt", false],
  ["CalendarMonth", "calendar", false],
  ["Copy", "copy", false],
  ["Login", "enter", true],
  ["Logout", "door-open", true],
];

const constName = (kotlin) =>
  kotlin
    .replace(/([a-z0-9])([A-Z])/g, "$1_$2")
    .toUpperCase();

const names = [...new Set(icons.map(([, snake]) => snake))];
const response = await fetch(`${API}?icons=${names.join(",")}`);
if (!response.ok) throw new Error(`HTTP ${response.status}`);
const collection = await response.json();

const paths = new Map();
for (const name of names) {
  const icon = collection.icons?.[name] ?? collection.icons?.[collection.aliases?.[name]?.parent];
  if (!icon) throw new Error(`${name}: not found`);
  const d = [...icon.body.matchAll(/<path[^>]*\sd="([^"]+)"/g)].map((m) => m[1]);
  if (d.length === 0) throw new Error(`${name}: no path`);
  paths.set(name, d.join(""));
}

let out = `package com.misgus.pengu.ui.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.dp

/**
 * Icon set vendored from Boxicons (MIT).
 *
 * The whole UI uses these solid glyphs, on Boxicons' 24dp viewport; call sites
 * size them with [com.misgus.pengu.ui.theme.AppIconSize]. \`scripts/gen_boxicons.mjs\`
 * regenerates the file from the Boxicons set on Iconify. Only the glyphs the UI
 * uses are kept.
 */
object Boxicons {
`;

for (const [kotlin, , autoMirror] of icons) {
  const flag = autoMirror ? ", autoMirror = true" : "";
  out += `    val ${kotlin}: ImageVector by lazy { boxicon("${kotlin}", ${constName(kotlin)}${flag}) }\n`;
}

out += `}

/**
 * Builds one [ImageVector] from its Boxicons path, on the 24dp viewport.
 *
 * \`autoMirror\` flips the glyph in RTL, which is what Material's auto-mirrored
 * variants did for the back arrow and the sign-in/out icons.
 */
private fun boxicon(
    name: String,
    pathData: String,
    autoMirror: Boolean = false,
): ImageVector =
    ImageVector
        .Builder(
            name = name,
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
            autoMirror = autoMirror,
        ).apply {
            addPath(
                pathData = PathParser().parsePathString(pathData).toNodes(),
                fill = SolidColor(Color.Black),
            )
        }.build()
`;

out += "\n";
for (const [kotlin, snake] of icons) {
  const chunks = paths.get(snake).match(/.{1,110}/g);
  out += `private const val ${constName(kotlin)} =\n`;
  chunks.forEach((chunk, index) => {
    const indent = index === 0 ? "    " : "        ";
    out += `${indent}"${chunk}"${index === chunks.length - 1 ? "" : " +"}\n`;
  });
}

process.stdout.write(out);
