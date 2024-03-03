package me.ddayo.customscript.client.gui.font

import com.mojang.blaze3d.systems.RenderSystem
import me.ddayo.customscript.client.gui.FontResource
import me.ddayo.customscript.client.gui.RenderUtil
import org.lwjgl.opengl.GL21
import java.nio.ByteBuffer

class FontedText(private val font: FontResource, private var text: String) {
    private lateinit var buf: ByteBuffer
    private var tex: Int = -1

    var width = 0
        private set
    private var height = 0

    private var needReload = true
    fun updateText(text: String) {
        this.text = text
        needReload = true
    }

    fun setHeight(height: Int): FontedText {
        this.height = height
        return this
    }

    private fun reloadBuffer() {
        this.width = font.calculateWidth(text, height)
        buf = font.getBitmap(text, height)

        if(tex == -1)
            tex = GL21.glGenTextures()
        RenderUtil.renderer.useTexture(tex) {
            RenderUtil.renderer.bindGrayscaleBuffer(buf, width, height)
        }
        needReload = false
    }

    fun free() {
        GL21.glDeleteTextures(tex)
        tex = -1
        needReload = true
    }

    fun render(textX: Double, textY: Double, color: UInt) {
        RenderSystem.enableBlend()
        if(needReload)
            reloadBuffer()

        RenderUtil.renderer.push {
            val b = (color % 256u).toInt() / 256.0
            val g = ((color / 256u) % 256u).toInt() / 256.0
            val r = ((color / 256u / 256u) % 256u).toInt() / 256.0
            //val a = (color / 256u / 256u / 256u).toInt() / 256.0
            RenderUtil.renderer.useTexture(tex) {
                RenderUtil.renderer.renderColorTex(textX, textY, width.toDouble(), height.toDouble(), r, g, b, 1.0)
            }
        }
    }
}