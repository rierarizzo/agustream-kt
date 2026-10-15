package com.misgus.pengu.player.mpv

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class MpvTracksTest {
    private val trackListJson =
        """
        [
          {"id":1,"type":"video","selected":true},
          {"id":1,"type":"audio","title":"English","lang":"eng","selected":true},
          {"id":2,"type":"sub","title":"Spanish","lang":"spa","selected":false,"external":false},
          {"id":3,"type":"sub","external":true,"external-filename":"https://x/subs.srt","selected":true}
        ]
        """.trimIndent()

    @Test
    fun parsesAudioAndSubtitleTracks() {
        val tracks = parseMpvTracks(trackListJson)

        assertEquals(1, tracks.audio.size)
        assertEquals("1", tracks.audio.single().id)
        assertEquals("English", tracks.audio.single().title)
        assertEquals("eng", tracks.audio.single().language)

        assertEquals(2, tracks.subtitles.size)
        val embedded = tracks.subtitles.first { it.id == "2" }
        assertEquals("Spanish", embedded.title)
        assertEquals(false, embedded.isExternal)
        assertNull(embedded.uri)

        val external = tracks.subtitles.first { it.id == "3" }
        assertEquals(true, external.isExternal)
        assertEquals("https://x/subs.srt", external.uri)
    }

    @Test
    fun keepsSelectedIdsPerType() {
        val tracks = parseMpvTracks(trackListJson)
        assertEquals("1", tracks.selectedAudioId)
        assertEquals("3", tracks.selectedSubtitleId)
    }

    @Test
    fun handlesMissingOrEmptyTrackList() {
        assertEquals(MpvTracks(), parseMpvTracks(null))
        assertEquals(MpvTracks(), parseMpvTracks(""))
        assertEquals(MpvTracks(), parseMpvTracks("[]"))
    }

    @Test
    fun formatsHttpHeaderOption() {
        assertEquals(
            "Referer: https://x,User-Agent: test",
            httpHeaderOption(linkedMapOf("Referer" to "https://x", "User-Agent" to "test")),
        )
        assertNull(httpHeaderOption(null))
        assertNull(httpHeaderOption(emptyMap()))
    }
}
