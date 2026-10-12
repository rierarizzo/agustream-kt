package com.misgus.agustream.data.store

import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SettingsRepositoryTest {
    private fun tempDir(): File =
        File.createTempFile("agustream-settings", "").let { file ->
            file.delete()
            file.mkdirs()
            file
        }

    @Test
    fun fallsBackToDefaultsWhenTheFileIsMissing() =
        runTest {
            val settings =
                JsonSettingsRepository(JsonMapStore(File(tempDir(), "settings.json").path, pretty = true))

            settings.load()

            assertTrue(settings.state.value.animationsEnabled)
            assertTrue(settings.state.value.resumePlayback)
            assertTrue(settings.state.value.autoPlayNext)
        }

    @Test
    fun persistsTogglesAndKeepsUnknownKeys() =
        runTest {
            val path = File(tempDir(), "settings.json").path
            val store = JsonMapStore(path, pretty = true)
            store.write(JsonObject(mapOf("custom.key" to JsonPrimitive("value"))))

            val settings = JsonSettingsRepository(store)
            settings.load()
            settings.setAnimationsEnabled(false)
            settings.setAutoPlayNext(false)

            val reloaded = JsonSettingsRepository(store)
            reloaded.load()
            assertFalse(reloaded.state.value.animationsEnabled)
            assertFalse(reloaded.state.value.autoPlayNext)
            assertEquals("value", (store.read()["custom.key"] as JsonPrimitive).content)
            // The file is written indented, so it can be read and edited by hand.
            assertTrue(File(path).readText().contains('\n'))
        }
}
