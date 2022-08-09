package me.ddayo.customscript.client.gui.script.blocks

import com.mojang.blaze3d.matrix.MatrixStack
import me.ddayo.customscript.client.ClientDataHandler
import me.ddayo.customscript.client.gui.RenderUtil
import me.ddayo.customscript.client.gui.font.Font
import me.ddayo.customscript.client.gui.font.FontManager
import me.ddayo.customscript.client.gui.font.FontedText
import me.ddayo.customscript.client.gui.script.ScriptGui
import me.ddayo.customscript.util.options.Option
import me.ddayo.customscript.util.options.Option.Companion.double
import me.ddayo.customscript.util.options.Option.Companion.string
import net.minecraft.client.Minecraft
import org.lwjgl.opengl.GL21

open class TextBlock: BlockBase(), IRendererBlock {
    var text = ""
        private set
    var textX = 0.0
        private set
    var textY = 0.0
        private set
    var textScale = 0.0
        private set
    var textFont = ""
    lateinit var fontedText: FontedText

    override fun parseContext(context: Option) {
        text = context["Text"].string!!
        textX = context["TextX"].double!!
        textY = context["TextY"].double!!
        textScale = context["TextScale"].double ?: 1.0
        textFont = context["TextFont"].string ?: ""
        if(textFont != "") {
            val fontInfo = FontManager.getFont(textFont)
            fontedText = FontedText(fontInfo, text)
            fontedText.calculateBuffer(textScale.toInt())
            fontedText.bindBuf()
        }
    }

    override fun onEnter() {
        base.appendRenderer(this)
    }

    override fun render() {
        RenderUtil.push {
            if(textFont == "") {
                GL21.glScaled(textScale, textScale, textScale)

                Minecraft.getInstance().fontRenderer.drawString(
                        MatrixStack(),
                        ClientDataHandler.decodeDynamicValue(text),
                        (textX / textScale).toFloat(),
                        (textY / textScale).toFloat(),
                        0xffffff
                )
            }
            else fontedText.render(textX, textY, 12.0)
        }
    }

    override val renderParse: ScriptGui.RenderParse
        get() = base.Post
}