// One-off generator: rebuilds AppIcons.kt from the Material Icons Round SVGs.
// Source: @material-design-icons/svg (Apache-2.0), the classic Material Icons.
// Run: node scripts/gen_icons.mjs > app/shared/src/commonMain/kotlin/com/misgus/pengu/ui/icons/AppIcons.kt

const CDN = "https://cdn.jsdelivr.net/npm/@material-design-icons/svg@0.14.15/round";

// [Kotlin name, Material Icons name]
const groups = {
  Filled: [
    ["PlayArrow", "play_arrow"],
    ["Close", "close"],
    ["Minimize", "remove"],
    ["Maximize", "crop_square"],
    ["Restore", "filter_none"],
    ["Search", "search"],
    ["Settings", "settings"],
    ["Home", "home"],
    ["Bookmark", "bookmark"],
    ["Add", "add"],
    ["CalendarToday", "calendar_today"],
    ["Refresh", "refresh"],
    ["Check", "check"],
    ["ChevronRight", "chevron_right"],
    ["ChevronLeft", "chevron_left"],
    ["Star", "star"],
    ["CheckCircle", "check_circle"],
    ["Pause", "pause"],
    ["Replay10", "replay_10"],
    ["Forward10", "forward_10"],
    ["Fullscreen", "fullscreen"],
    ["ArrowDropDown", "arrow_drop_down"],
    ["Visibility", "visibility"],
    ["VisibilityOff", "visibility_off"],
  ],
  Outlined: [
    ["ErrorOutline", "error_outline"],
    ["Inbox", "inbox"],
    ["Explore", "explore"],
    ["Subtitles", "subtitles"],
    ["StarBorder", "star_border"],
    ["Settings", "settings"],
    ["SearchOff", "search_off"],
    ["Search", "search"],
    ["Person", "person"],
    ["Movie", "movie"],
    ["Info", "info"],
    ["Home", "home"],
    ["FolderOpen", "folder_open"],
    ["ContentCopy", "content_copy"],
    ["CheckCircle", "check_circle_outline"],
    ["CalendarToday", "calendar_today"],
    ["CalendarMonth", "calendar_month"],
    ["BookmarkBorder", "bookmark_border"],
    ["Audiotrack", "audiotrack"],
  ],
};

// Auto-mirrored groups always set autoMirror = true.
const autoMirrored = {
  Filled: [
    ["ArrowBack", "arrow_back"],
    ["VolumeUp", "volume_up"],
  ],
  Outlined: [
    ["Login", "login"],
    ["Logout", "logout"],
  ],
};

async function fetchIcon(name) {
  const res = await fetch(`${CDN}/${name}.svg`);
  if (!res.ok) throw new Error(`${name}: HTTP ${res.status}`);
  const svg = await res.text();
  const match = svg.match(/<path[^>]*\sd="([^"]+)"/);
  if (!match) throw new Error(`${name}: no path`);
  const evenOdd = /fill-rule="evenodd"/.test(svg);
  return { d: match[1], evenOdd };
}

const constName = (snake) => snake.toUpperCase();
const paths = new Map();

let out = "";

function emitGroup(indent, entries, autoMirror) {
  for (const [kotlin, snake] of entries) {
    const flag = autoMirror ? ", autoMirror = true" : "";
    const evenOdd = paths.get(snake).evenOdd ? ", fillType = PathFillType.EvenOdd" : "";
    out += `${indent}val ${kotlin}: ImageVector by lazy { rounded("${kotlin}", ${constName(snake)}${evenOdd}${flag}) }\n`;
  }
}

const all = [
  ...groups.Filled,
  ...groups.Outlined,
  ...autoMirrored.Filled,
  ...autoMirrored.Outlined,
];

for (const [, snake] of all) {
  if (paths.has(snake)) continue;
  paths.set(snake, await fetchIcon(snake));
}

out += `package com.misgus.pengu.ui.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.dp

/**
 * Icon set vendored from Material Icons, Round theme (Apache License 2.0).
 *
 * Only the glyphs the app uses are kept, so the UI does not depend on
 * \`material-icons-extended\`. Each vector is built from the corresponding
 * Material Icons Round SVG path at the default 24dp size; call sites size it
 * with [com.misgus.pengu.ui.theme.AppIconSize].
 *
 * Names mirror the previous \`Icons.*\` references so call sites only swap the
 * receiver. \`AutoMirrored\` groups carry \`autoMirror = true\`, which is what the
 * Material auto-mirrored variants did.
 */
object AppIcons {
    /** Classic \`Icons.Filled\` / \`Icons.Default\`. */
    object Filled {
`;
emitGroup("        ", groups.Filled, false);
out += `    }

    /** Classic \`Icons.Outlined\`. */
    object Outlined {
`;
emitGroup("        ", groups.Outlined, false);
out += `    }

    /** Classic \`Icons.AutoMirrored.Filled\`. */
    object AutoMirrored {
        object Filled {
`;
emitGroup("            ", autoMirrored.Filled, true);
out += `        }

        object Outlined {
`;
emitGroup("            ", autoMirrored.Outlined, true);
out += `        }
    }
}

/**
 * Builds one [ImageVector] from its Material Icons Round path.
 *
 * The path is parsed once, lazily, when the icon is first read.
 */
private fun rounded(
    name: String,
    pathData: String,
    fillType: PathFillType = PathFillType.NonZero,
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
                pathFillType = fillType,
                fill = SolidColor(Color.Black),
            )
        }.build()
`;

const sorted = [...paths.keys()].sort();
out += "\n";
for (const snake of sorted) {
  const chunks = paths.get(snake).d.match(/.{1,110}/g);
  out += `private const val ${constName(snake)} =\n`;
  chunks.forEach((chunk, index) => {
    const indent = index === 0 ? "    " : "        ";
    out += `${indent}"${chunk}"${index === chunks.length - 1 ? "" : " +"}\n`;
  });
}

process.stdout.write(out);
