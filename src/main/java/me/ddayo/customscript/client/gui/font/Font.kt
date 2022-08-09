package me.ddayo.customscript.client.gui.font

import org.apache.logging.log4j.LogManager
import org.lwjgl.BufferUtils
import org.lwjgl.stb.STBTTBakedChar
import org.lwjgl.stb.STBTTFontinfo
import org.lwjgl.stb.STBTruetype
import java.io.File
import java.nio.ByteBuffer
import kotlin.math.roundToInt


class Font(ttfFile: String) {
    private val bytes = File("assets/fonts/$ttfFile").readBytes()
    private val byteBuf = BufferUtils.createByteBuffer(bytes.size)
    init {
        byteBuf.put(bytes)
        byteBuf.flip()
    }

    private val fontInfo = STBTTFontinfo.create()
    init {
        STBTruetype.stbtt_InitFont(fontInfo, byteBuf)
    }

    private var _ascent = createIntArray()
    private var _descent = createIntArray()
    private var _lineGap = createIntArray()

    public val ascent: Int
        get() = this._ascent[0]
    public val descent: Int
        get() = this._descent[0]
    public val lineGap: Int
        get() = this._lineGap[0]

    public fun getScale(height: Int) = STBTruetype.stbtt_ScaleForPixelHeight(fontInfo, height.toFloat())

    fun calculateWidth(text: String, height: Int): Int {
        val scale = STBTruetype.stbtt_ScaleForPixelHeight(fontInfo, height.toFloat())

        var x = 0
        for(k in text.withIndex()) {
            val ax = createIntArray()
            val lsb = createIntArray()
            STBTruetype.stbtt_GetCodepointHMetrics(fontInfo, k.value.code, ax, lsb)

            x += (ax[0] * scale).roundToInt()
            x += if(k.index != text.length - 1)
                (STBTruetype.stbtt_GetCodepointKernAdvance(fontInfo, k.value.code, text[k.index + 1].code) * scale).roundToInt()
            else (STBTruetype.stbtt_GetCodepointKernAdvance(fontInfo, k.value.code, 0) * scale).roundToInt()
        }
        return x
    }

    fun getBitmap(text: String, height: Int): ByteBuffer {
        val tlXt = "${text.substring(1)}${text[0]}" //IDK why this code is required...

        val width = calculateWidth(tlXt, height)
        STBTruetype.stbtt_GetFontVMetrics(fontInfo, _ascent, _descent, _lineGap)

        val buf = BufferUtils.createByteBuffer(width * height)
        val scale = STBTruetype.stbtt_ScaleForPixelHeight(fontInfo, height.toFloat())

        _ascent[0] = (ascent * scale).roundToInt()
        _descent[0] = (descent * scale).roundToInt()

        LogManager.getLogger().info("$ascent $descent $scale")

        var x = 0
        for(k in tlXt.withIndex()) {
            val ax = createIntArray()
            val lsb = createIntArray()
            STBTruetype.stbtt_GetCodepointHMetrics(fontInfo, k.value.code, ax, lsb)

            val c_x1 = createIntArray()
            val c_x2 = createIntArray()
            val c_y1 = createIntArray()
            val c_y2 = createIntArray()

            STBTruetype.stbtt_GetCodepointBitmapBox(fontInfo, k.value.code, scale, scale, c_x1, c_y1, c_x2, c_y2)

            val y = ascent + c_y1[0]
            buf.position(x + (lsb[0] * scale).roundToInt() + (y * width))
            STBTruetype.stbtt_MakeCodepointBitmap(fontInfo, buf, c_x2[0] - c_x1[0], c_y2[0] - c_y1[0], width, scale, scale, k.value.code)

            x += if(k.index != tlXt.length - 1)
                (ax[0] * scale).roundToInt() + (STBTruetype.stbtt_GetCodepointKernAdvance(fontInfo, k.value.code, tlXt[k.index + 1].code) * scale).roundToInt()
            else (ax[0] * scale).roundToInt() + (STBTruetype.stbtt_GetCodepointKernAdvance(fontInfo, k.value.code, 0) * scale).roundToInt()
        }

        return buf
    }

    private fun createIntArray() = arrayOf(0).toIntArray()
}