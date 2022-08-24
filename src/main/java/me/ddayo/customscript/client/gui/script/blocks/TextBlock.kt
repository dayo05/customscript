package me.ddayo.customscript.client.gui.script.blocks

import com.mojang.blaze3d.matrix.MatrixStack
import me.ddayo.customscript.client.ClientDataHandler
import me.ddayo.customscript.client.event.OnDynamicValueUpdateEvent
import me.ddayo.customscript.client.gui.RenderUtil
import me.ddayo.customscript.client.gui.font.FontManager
import me.ddayo.customscript.client.gui.font.FontedText
import me.ddayo.customscript.client.gui.script.ScriptGui
import me.ddayo.customscript.util.options.Option
import me.ddayo.customscript.util.options.Option.Companion.double
import me.ddayo.customscript.util.options.Option.Companion.string
import net.minecraft.client.Minecraft
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.eventbus.api.SubscribeEvent
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

    var textColor = 0xffffffu

    var usingCustomFont = false

    var renderText = ""

    override fun parseContext(context: Option) {
        text = context["Text"].string!!
        renderText = ClientDataHandler.decodeDynamicValue(text)
        textX = context["TextX"].double!!
        textY = context["TextY"].double!!
        textScale = context["TextScale"].double ?: 1.0
        textFont = context["TextFont"].string ?: ""
        textColor = (context["TextColor"].string ?: "ffffff").toUInt(16)

        if(textFont != "")
            usingCustomFont = true
    }

    override fun onEnter() {
        base.appendRenderer(this)
        if(usingCustomFont)
            FontManager.getFont(textFont).run {
                fontedText = FontedText(this, renderText).setHeight(textScale.toInt())
            }
        MinecraftForge.EVENT_BUS.register(this)
    }

    override fun onRemovedFromQueue() {
        if(usingCustomFont)
            fontedText.free()
        MinecraftForge.EVENT_BUS.unregister(this)
    }

    @SubscribeEvent
    fun onDynamicValueUpdated(event: OnDynamicValueUpdateEvent) {
        renderText = ClientDataHandler.decodeDynamicValue(text)
        if (usingCustomFont)
            fontedText.updateText(renderText)
    }

    override fun render() {
        RenderUtil.push {
            if(!usingCustomFont) {
                GL21.glScaled(textScale, textScale, textScale)

                Minecraft.getInstance().fontRenderer.drawString(
                        MatrixStack(),
                        renderText,
                        (textX / textScale).toFloat(),
                        (textY / textScale).toFloat(),
                    textColor.toInt()
                )
            }
            else fontedText.render(textX, textY, textColor)
        }
    }

    override val renderParse: ScriptGui.RenderParse
        get() = base.Post
}