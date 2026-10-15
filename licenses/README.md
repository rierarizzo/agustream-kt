# Third-party licenses

Pengu itself is licensed under the GNU General Public License v3.0 (see the
[`LICENSE`](../LICENSE) file). It bundles or links the third-party components
below, which keep their own licenses. The license text is reproduced next to
this file when it is not already covered by the project's own license.

| Component | Use | License | Text |
| --- | --- | --- | --- |
| libmpv | desktop playback, linked through JNA; `libmpv-2.dll` is bundled on Windows | LGPL-2.1-or-later | [libmpv-LGPL-2.1.txt](libmpv-LGPL-2.1.txt) |
| IBM Plex Sans | alternate UI typeface | SIL Open Font License 1.1 | [IBM-Plex-Sans-OFL.txt](IBM-Plex-Sans-OFL.txt) |
| Inter | UI typeface | SIL Open Font License 1.1 | [Inter-OFL.txt](Inter-OFL.txt) |
| Material Symbols / Material Icons | vendored UI icon set (`AppIcons.kt`) | Apache-2.0 | [Material-Symbols-Apache-2.0.txt](Material-Symbols-Apache-2.0.txt) |

libmpv is linked dynamically so it can be replaced by the user, which is what
the LGPL requires. If you redistribute the bundled `libmpv-2.dll`, keep this
notice and the LGPL text with it.
