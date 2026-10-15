package com.misgus.pengu.data.platform

import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PlatformFileIOTest {
    private fun tempDir(): File =
        File.createTempFile("pengu", "").let { file ->
            file.delete()
            file.mkdirs()
            file
        }

    private fun File.withFile(
        name: String,
        text: String,
    ): File {
        mkdirs()
        File(this, name).writeText(text)
        return this
    }

    @Test
    fun movesTheLegacyDirectoryIntoPlace() {
        val base = tempDir()
        val legacy =
            File(base, "Agustream")
                .withFile("session.json", "token")
                .withFile("settings.json", """{"player.resumePlayback":true}""")
        val current = File(base, "Pengu")

        PlatformFileIO.migrateConfigDirectory(legacy, current)

        assertFalse(legacy.exists())
        assertEquals("token", File(current, "session.json").readText())
        assertEquals(
            """{"player.resumePlayback":true}""",
            File(current, "settings.json").readText(),
        )
    }

    @Test
    fun keepsAnExistingCurrentDirectoryUntouched() {
        val base = tempDir()
        val current = File(base, "Pengu").withFile("session.json", "new")
        val legacy = File(base, "Agustream").withFile("session.json", "old")

        PlatformFileIO.migrateConfigDirectory(legacy, current)

        assertEquals("new", File(current, "session.json").readText())
        assertTrue(legacy.exists(), "the legacy directory must not be consumed")
    }

    @Test
    fun doesNothingWithoutALegacyDirectory() {
        val base = tempDir()
        val current = File(base, "Pengu")

        PlatformFileIO.migrateConfigDirectory(File(base, "Agustream"), current)

        assertFalse(current.exists())
    }
}
