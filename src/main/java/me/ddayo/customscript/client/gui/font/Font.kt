package me.ddayo.customscript.client.gui.font

import org.lwjgl.BufferUtils
import org.lwjgl.stb.STBTTFontinfo
import org.lwjgl.stb.STBTruetype
import java.io.File
import java.nio.ByteBuffer
import kotlin.math.roundToInt


class Font(ttfFile: String) {
    val bytes = File(ttfFile).readBytes()
    val byteBuf = BufferUtils.createByteBuffer(bytes.size)
    init {
        byteBuf.put(bytes)
        byteBuf.flip()
    }

    val fontInfo = STBTTFontinfo.create()
    init {
        STBTruetype.stbtt_InitFont(fontInfo, byteBuf)
    }

    fun getBitmap(text: String, width: Int, height: Int): ByteBuffer {
        val buf = BufferUtils.createByteBuffer(width * height)
        val scale = STBTruetype.stbtt_ScaleForPixelHeight(fontInfo, height.toFloat())

        val ascent = createIntArray()
        val descent = createIntArray()
        val lineGap = createIntArray()
        STBTruetype.stbtt_GetFontVMetrics(fontInfo, ascent, descent, lineGap)

        ascent[0] = (ascent[0] * scale).roundToInt()
        descent[0] = (descent[0] * scale).roundToInt()

        var x = 0
        for(k in text.withIndex()) {
            val ax = createIntArray()
            val lsb = createIntArray()
            STBTruetype.stbtt_GetCodepointHMetrics(fontInfo, k.value.code, ax, lsb)

            val c_x1 = createIntArray()
            val c_x2 = createIntArray()
            val c_y1 = createIntArray()
            val c_y2 = createIntArray()

            STBTruetype.stbtt_GetCodepointBitmapBox(fontInfo, k.value.code, scale, scale, c_x1, c_y1, c_x2, c_y2)

            val y = ascent[0] + c_y1[0]
            buf.position(x + (lsb[0] * scale).roundToInt() + (y * width))
            STBTruetype.stbtt_MakeCodepointBitmap(fontInfo, buf, c_x2[0] - c_x1[0], c_y2[0] - c_y1[0], width, scale, scale, k.value.code)

            if(k.index != text.length - 1)
                x += (ax[0] * scale).roundToInt() + (STBTruetype.stbtt_GetCodepointKernAdvance(fontInfo, k.value.code, text[k.index + 1].code) * scale).roundToInt()
        }

        return buf
    }

    private fun createIntArray() = arrayOf(0).toIntArray()
}