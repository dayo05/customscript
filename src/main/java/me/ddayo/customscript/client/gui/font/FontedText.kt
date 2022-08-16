package me.ddayo.customscript.client.gui.font

import me.ddayo.customscript.client.gui.RenderUtil
import org.lwjgl.opengl.GL21
import java.nio.ByteBuffer

class FontedText(private val font: Font, private val text: String) {
    private lateinit var buf: ByteBuffer
    private var tex: Int = 0
    var width = 0
        private set
    private var height = 0

    fun calculateBuffer(height: Int): ByteBuffer {
        this.width = font.calculateWidth(text, height)
        this.height = height
        buf = font.getBitmap(text, height)
        return buf
    }

    fun bindBuf() {
        tex = RenderUtil.bindGrayscaleBuffer(buf, width, height)
    }

    fun useBuf(x: () -> Unit) = RenderUtil.useTexture(tex, x)

    fun free() {
        GL21.glDeleteTextures(tex)
    }

    fun render(textX: Double, textY: Double) {
        useBuf {
            GL21.glEnable(GL21.GL_TEXTURE_2D)
            GL21.glEnable(GL21.GL_BLEND)
            GL21.glColor3d(1.0, 1.0, 1.0)
            GL21.glBlendFunc(GL21.GL_SRC_ALPHA, GL21.GL_ONE_MINUS_SRC_ALPHA)
            RenderUtil.render(textX, textY, width.toDouble(), height.toDouble(), 0.0, 1.0, 0.0, 1.0)
        }
    }
}