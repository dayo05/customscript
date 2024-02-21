package me.ddayo.customscript.client.gui.script.blocks

import com.mojang.blaze3d.matrix.MatrixStack
import me.ddayo.customscript.client.event.OnDynamicValueUpdateEvent
import me.ddayo.customscript.client.gui.RenderUtil
import me.ddayo.customscript.client.gui.font.FontManager
import me.ddayo.customscript.client.gui.font.FontedText
import me.ddayo.customscript.client.gui.script.ScriptGui
import me.ddayo.customscript.util.options.CalculableValue
import me.ddayo.customscript.util.options.Option
import me.ddayo.customscript.util.options.Option.Companion.string
import net.minecraft.client.Minecraft
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.eventbus.api.SubscribeEvent
import org.lwjgl.opengl.GL21

class TextBlock: BlockBase() {
    private class TextScriptRenderer(private val text: CalculableValue, private val textX: CalculableValue, private val textY: CalculableValue, private val textScale: CalculableValue, private val textFont: String, private val textColor: UInt): ScriptRenderer() {
        private val usingCustomFont = textFont.isNotBlank()
        lateinit var fontedText: FontedText
        init {
            if(usingCustomFont)
                FontManager.getFont(textFont).run {
                    fontedText = FontedText(this, text.string).setHeight(textScale.int)
                }
        }
        override val renderParse: ScriptGui.RenderParse
            get() = ScriptGui.RenderParse.Post
        override fun onRemovedFromQueue() {
            if(usingCustomFont)
                fontedText.free()
        }
        override fun render() {
            GL21.glEnable(GL21.GL_BLEND)
            RenderUtil.push {
                if(!usingCustomFont) {
                    GL21.glScaled(textScale.double, textScale.double, textScale.double)

                    Minecraft.getInstance().fontRenderer.drawString(
                        MatrixStack(),
                        text.string,
                        textX.float / textScale.float,
                        textY.float / textScale.float,
                        textColor.toInt()
                    )
                }
                else fontedText.render(textX.double, textY.double, textColor)
                GL21.glEnable(GL21.GL_ALPHA_TEST)
            }
        }
    }
    private lateinit var text: CalculableValue
    private lateinit var textX: CalculableValue
    private lateinit var textY: CalculableValue
    private lateinit var textScale: CalculableValue

    private var textFont = ""

    private var textColor = 0xffffffu

    override fun parseContext(context: Option) {
        text = CalculableValue(context["Text"].string!!, true)
        textX = CalculableValue(context["TextX"].string!!)
        textY = CalculableValue(context["TextY"].string!!)
        textScale = CalculableValue(context["TextScale"].string ?: "1.0")
        textFont = context["TextFont"].string ?: ""
        textColor = (context["TextColor"].string ?: "ffffff").toUInt(16)
    }

    override val rendererInstance: ScriptRenderer?
        get() = TextScriptRenderer(text, textX, textY, textScale, textFont, textColor)
}