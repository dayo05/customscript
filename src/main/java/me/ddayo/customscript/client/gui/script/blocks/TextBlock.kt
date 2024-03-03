package me.ddayo.customscript.client.gui.script.blocks

import com.mojang.blaze3d.matrix.MatrixStack
import me.ddayo.customscript.client.gui.FontResource
import me.ddayo.customscript.client.gui.RenderUtil
import me.ddayo.customscript.client.gui.font.FontedText
import me.ddayo.customscript.client.gui.script.ScriptGui
import me.ddayo.customscript.util.js.DoubleCalculable
import me.ddayo.customscript.util.js.ICalculableHolder
import me.ddayo.customscript.util.js.StringCalculable
import me.ddayo.customscript.util.options.Option
import me.ddayo.customscript.util.options.Option.Companion.string
import net.minecraft.client.Minecraft

class TextBlock : BlockBase() {
    private class TextScriptRenderer(
        private val text: StringCalculable,
        private val textX: DoubleCalculable,
        private val textY: DoubleCalculable,
        private val textScale: DoubleCalculable,
        private val textFont: String,
        private val textColor: UInt
    ) : ScriptRenderer(), ICalculableHolder {
        private val usingCustomFont = textFont.isNotBlank()
        lateinit var fontedText: FontedText

        init {
            if (usingCustomFont) {
                FontResource.getOrCreate(textFont).run {
                    fontedText = FontedText(this, text.get).setHeight(textScale.get.toInt())
                }
                text.setUpdateHook {
                    fontedText.updateText(it)
                }
            }
        }

        override val renderParse: ScriptGui.RenderParse
            get() = ScriptGui.RenderParse.Post

        override fun onRemovedFromQueue() {
            if (usingCustomFont)
                fontedText.free()
        }

        override fun RenderUtil.renderInternal() {
            push {
                if (!usingCustomFont) {
                    scale(textScale.get, textScale.get, textScale.get)
                    Minecraft.getInstance().fontRenderer.drawString(
                        MatrixStack(),
                        text.get,
                        (textX.get / textScale.get).toFloat(),
                        (textY.get / textScale.get).toFloat(),
                        textColor.toInt()
                    )
                } else fontedText.render(textX.get, textY.get, textColor)
            }
        }

        override val calculable by lazy { listOf(text, textX, textY, textScale) }
        override val isLoading: Boolean
            get() = usingCustomFont && !FontResource.getOrCreate(textFont).isLoaded
    }

    private lateinit var text: StringCalculable
    private lateinit var textX: DoubleCalculable
    private lateinit var textY: DoubleCalculable
    private lateinit var textScale: DoubleCalculable

    private var textFont = ""

    private var textColor = 0xffffffu

    override fun parseContext(context: Option) {
        text = StringCalculable(context["Text"].string!!)
        textX = DoubleCalculable(context["TextX"].string!!)
        textY = DoubleCalculable(context["TextY"].string!!)
        textScale = DoubleCalculable(context["TextScale"].string ?: "1.0")
        textFont = context["TextFont"].string ?: ""
        textColor = (context["TextColor"].string ?: "ffffff").toUInt(16)
    }


    override val rendererInstance: ScriptRenderer
        get() = TextScriptRenderer(text, textX, textY, textScale, textFont, textColor)
}