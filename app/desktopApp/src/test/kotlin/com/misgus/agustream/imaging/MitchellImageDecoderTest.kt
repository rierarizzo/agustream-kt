package com.misgus.agustream.imaging

import coil3.PlatformContext
import coil3.decode.ImageSource
import coil3.request.Options
import coil3.size.Precision
import coil3.size.Scale
import coil3.size.Size
import kotlinx.coroutines.runBlocking
import okio.Buffer
import okio.FileSystem
import org.jetbrains.skia.Bitmap
import org.jetbrains.skia.Image
import kotlin.test.Test
import kotlin.test.assertEquals

class MitchellImageDecoderTest {
    @Test
    fun downsamplesToTheRequestedSize() =
        runBlocking {
            val source = Bitmap().apply { allocN32Pixels(200, 200) }
            val encoded = Image.makeFromBitmap(source).encodeToData()!!.bytes
            val imageSource = ImageSource(Buffer().write(encoded), FileSystem.SYSTEM)
            val options =
                Options(
                    context = PlatformContext.INSTANCE,
                    size = Size(50, 50),
                    scale = Scale.FIT,
                    precision = Precision.EXACT,
                )

            val result = MitchellImageDecoder(imageSource, options).decode()

            assertEquals(50, result?.image?.width)
            assertEquals(50, result?.image?.height)
        }
}
