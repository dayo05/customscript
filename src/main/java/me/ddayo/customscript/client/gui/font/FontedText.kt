package me.ddayo.customscript.client.gui.font

import me.ddayo.customscript.client.gui.RenderUtil
import java.nio.ByteBuffer

class FontedText(private val font: Font, private val text: String) {
    private lateinit var buf: ByteBuffer
    private var tex: Int = 0
    private var width = 0
    private var height = 0

    fun calculateBuffer(width: Int, height: Int): ByteBuffer {
        this.width = width
        this.height = height
        buf = font.getBitmap(text, width, height)
        return buf
    }

    fun bindBuf() {
        tex = RenderUtil.bindGrayscaleBuffer(buf, width, height)
    }

    fun useBuf(x: () -> Unit) = RenderUtil.useTexture(tex, x)
}