package com.misgus.agustream.core.model

/**
 * One subtitle track an addon offers (the `subtitles` resource).
 *
 * Besides `id`/`url`/`lang` from the Stremio protocol, richer addons (e.g.
 * AIOStreams/OpenSubtitles) also send `title`, `lang_code`, `ai_translated` and
 * `from_trusted`.
 */
data class Subtitle(
    val id: String,
    val url: String,
    /** Language as the addon labels it; may be a code (`spa`) or free text. */
    val lang: String? = null,
    /** Release/file name the subtitle matches, when the addon sends one. */
    val title: String? = null,
    /** ISO language code, when the addon sends one. */
    val langCode: String? = null,
    val aiTranslated: Boolean = false,
    val fromTrusted: Boolean = false,
    /** Addon that offered this subtitle (set when aggregating). */
    val addonName: String? = null,
) {
    /** Human-readable language name, when the code is recognized. */
    val languageName: String?
        get() {
            val code = (langCode ?: lang)?.trim()?.lowercase() ?: return null
            if (code.isEmpty()) return null
            return LANGUAGES[code] ?: LANGUAGES[code.substringBefore('-')]
        }

    /** Copy with [addonName] set (used by the aggregating repository). */
    fun withAddon(name: String?): Subtitle = copy(addonName = name)

    companion object {
        private val LANGUAGES: Map<String, String> = mapOf(
            "en" to "English", "eng" to "English",
            "es" to "Spanish", "spa" to "Spanish",
            "fr" to "French", "fre" to "French", "fra" to "French",
            "de" to "German", "ger" to "German", "deu" to "German",
            "it" to "Italian", "ita" to "Italian",
            "pt" to "Portuguese", "por" to "Portuguese", "pt-br" to "Portuguese (Brazil)",
            "ru" to "Russian", "rus" to "Russian",
            "ja" to "Japanese", "jpn" to "Japanese",
            "ko" to "Korean", "kor" to "Korean",
            "zh" to "Chinese", "chi" to "Chinese", "zho" to "Chinese",
            "ar" to "Arabic", "ara" to "Arabic",
            "hi" to "Hindi", "hin" to "Hindi",
            "nl" to "Dutch", "dut" to "Dutch", "nld" to "Dutch",
            "pl" to "Polish", "pol" to "Polish",
            "tr" to "Turkish", "tur" to "Turkish",
            "sv" to "Swedish", "swe" to "Swedish",
            "no" to "Norwegian", "nor" to "Norwegian",
            "da" to "Danish", "dan" to "Danish",
            "fi" to "Finnish", "fin" to "Finnish",
            "cs" to "Czech", "cze" to "Czech", "ces" to "Czech",
            "el" to "Greek", "gre" to "Greek", "ell" to "Greek",
            "he" to "Hebrew", "heb" to "Hebrew",
            "uk" to "Ukrainian", "ukr" to "Ukrainian",
            "ro" to "Romanian", "ron" to "Romanian",
            "hu" to "Hungarian", "hun" to "Hungarian",
            "th" to "Thai", "tha" to "Thai",
            "vi" to "Vietnamese", "vie" to "Vietnamese",
            "id" to "Indonesian", "ind" to "Indonesian",
            "fa" to "Persian", "per" to "Persian", "fas" to "Persian",
            "ms" to "Malay", "may" to "Malay", "msa" to "Malay",
            "bg" to "Bulgarian", "bul" to "Bulgarian",
            "hr" to "Croatian", "hrv" to "Croatian",
            "sr" to "Serbian", "srp" to "Serbian",
            "sk" to "Slovak", "slo" to "Slovak", "slk" to "Slovak",
            "sl" to "Slovenian", "slv" to "Slovenian",
            "et" to "Estonian", "est" to "Estonian",
            "lv" to "Latvian", "lav" to "Latvian",
            "lt" to "Lithuanian", "lit" to "Lithuanian",
        )
    }
}
