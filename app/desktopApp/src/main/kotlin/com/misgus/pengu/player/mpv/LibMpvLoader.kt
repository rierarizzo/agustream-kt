package com.misgus.pengu.player.mpv

import com.sun.jna.FunctionMapper
import com.sun.jna.Library
import com.sun.jna.Native
import java.io.File

/**
 * Loads libmpv.
 *
 * On Windows the DLL is bundled with the app and extracted from the classpath
 * on first use, so no separate mpv install is needed. On Linux the system
 * `libmpv.so` is used, mirroring how the desktop player is normally packaged
 * there.
 */
internal object LibMpvLoader {
    private const val WINDOWS_RESOURCE = "/native/windows/libmpv-2.dll"

    private val camelBoundary = Regex("[A-Z]")

    /** Maps `mpvSetOptionString` to the `mpv_set_option_string` C symbol. */
    private val cSymbolMapper =
        FunctionMapper { _, method ->
            method.name.replace(camelBoundary) { match -> "_" + match.value.lowercase() }
        }

    private val loadOptions = mapOf(Library.OPTION_FUNCTION_MAPPER to cSymbolMapper)

    private val linuxCandidates =
        listOf(
            "mpv",
            "/usr/lib/x86_64-linux-gnu/libmpv.so.2",
            "/usr/lib/x86_64-linux-gnu/libmpv.so",
            "/usr/lib/aarch64-linux-gnu/libmpv.so.2",
            "/usr/lib/aarch64-linux-gnu/libmpv.so",
            "/usr/lib64/libmpv.so.2",
            "/usr/lib/libmpv.so.2",
            "/usr/local/lib/libmpv.so",
        )

    fun load(): LibMpv {
        val explicit = System.getProperty("pengu.libmpv") ?: System.getenv("PENGU_LIBMPV")
        if (!explicit.isNullOrBlank()) {
            return loadExact(explicit)
        }
        return if (isWindows()) loadWindows() else loadLinux()
    }

    private fun loadExact(nameOrPath: String): LibMpv = Native.load(nameOrPath, LibMpv::class.java, loadOptions)

    private fun loadWindows(): LibMpv {
        val packaged =
            System
                .getProperty("compose.application.resources.dir")
                ?.takeIf { it.isNotBlank() }
                ?.let { File(it, "native/windows/libmpv-2.dll") }
                ?.takeIf(File::isFile)
        if (packaged != null) {
            return loadExact(packaged.absolutePath)
        }

        localWindowsCandidates().firstOrNull(File::isFile)?.let {
            return loadExact(it.absolutePath)
        }

        val bytes =
            LibMpvLoader::class.java.getResourceAsStream(WINDOWS_RESOURCE)?.use { it.readBytes() }
                ?: throw UnsatisfiedLinkError(
                    "libmpv-2.dll was not found. Bundle it under resources$WINDOWS_RESOURCE " +
                        "or point PENGU_LIBMPV at it.",
                )
        return loadExact(extractWindowsDll(bytes).absolutePath)
    }

    private fun loadLinux(): LibMpv {
        var lastError: Throwable? = null
        for (candidate in linuxCandidates) {
            try {
                return loadExact(candidate)
            } catch (error: UnsatisfiedLinkError) {
                lastError = error
            }
        }
        throw UnsatisfiedLinkError(
            "libmpv was not found. Install the libmpv runtime (e.g. the 'libmpv2' package) " +
                "or point PENGU_LIBMPV at libmpv.so. Cause: ${lastError?.message}",
        )
    }

    /** Local build outputs, for development runs. */
    private fun localWindowsCandidates(): List<File> =
        listOf(
            File("libs/libmpv/windows/libmpv-2.dll"),
            File("app/desktopApp/build/native/windows/libmpv-2.dll"),
            File("build/native/windows/libmpv-2.dll"),
        )

    private fun extractWindowsDll(bytes: ByteArray): File {
        val directory = File(System.getProperty("java.io.tmpdir"), "pengu-native/windows")
        val dll = File(directory, "libmpv-2.dll")
        if (dll.isFile && dll.length() == bytes.size.toLong()) return dll
        directory.mkdirs()
        dll.writeBytes(bytes)
        return dll
    }

    private fun isWindows(): Boolean = System.getProperty("os.name").lowercase().contains("win")
}
