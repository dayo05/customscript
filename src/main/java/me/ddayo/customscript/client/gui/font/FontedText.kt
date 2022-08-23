package me.ddayo.customscript.client.gui.font

import me.ddayo.customscript.client.gui.RenderUtil
import org.lwjgl.opengl.GL21
import java.nio.ByteBuffer

class FontedText(private val font: Font, private var text: String) {
    private lateinit var buf: ByteBuffer
    private var tex: Int = -1
    var width = 0
        private set
    private var height = 0

    fun updateText(text: String) {
        this.text = text
        if(tex != -1)
            free()
    }

    fun setHeight(height: Int): FontedText {
        this.height = height
        return this
    }

    private fun bindBuffer() {
        if(font.isAllocated)
            font.allocate()
        this.width = font.calculateWidth(text, height)
        buf = font.getBitmap(text, height)
        tex = RenderUtil.bindGrayscaleBuffer(buf, width, height)
    }

    fun useBuf(x: () -> Unit) {
        if(tex == -1) bindBuffer()
        RenderUtil.useTexture(tex, x)
    }

    fun free() {
        GL21.glDeleteTextures(tex)
        tex = -1
    }

    fun render(textX: Double, textY: Double, color: UInt) {
        if(!font.isAllocated)
            free()
        RenderUtil.push {
            GL21.glEnable(GL21.GL_TEXTURE_2D)
            val b = (color % 256u).toInt() / 256.0
            val g = ((color / 256u) % 256u).toInt() / 256.0
            val r = ((color / 256u / 256u) % 256u).toInt() / 256.0
            //val a = (color / 256u / 256u / 256u).toInt() / 256.0
            GL21.glColor3d(r, g, b)
            useBuf {
                RenderUtil.render(textX, textY, width.toDouble(), height.toDouble())
            }
        }
    }
}