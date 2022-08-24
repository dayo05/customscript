package me.ddayo.customscript.client.gui.script.blocks

import me.ddayo.customscript.client.ClientDataHandler
import me.ddayo.customscript.client.event.OnDynamicValueUpdateEvent
import me.ddayo.customscript.client.gui.RenderUtil
import me.ddayo.customscript.client.gui.script.ScriptGui
import me.ddayo.customscript.util.options.Option
import me.ddayo.customscript.util.options.Option.Companion.double
import me.ddayo.customscript.util.options.Option.Companion.string
import me.ddayo.customscript.util.options.Option.Companion.bool
import net.minecraft.client.Minecraft
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.eventbus.api.SubscribeEvent

class ButtonBlock: MultiSelectableBlock(), IRendererBlock {
    var buttonX = 0.0
        private set
    var buttonY = 0.0
        private set
    var buttonWidth = 0.0
        private set
    var buttonHeight = 0.0
        private set
    var renderButtonImage = ""
        private set
    var buttonImage = ""
        private set
    var autoSize = false
        private set

    override fun parseContext(context: Option) {
        buttonX = context["ButtonX"].double!!
        buttonY = context["ButtonY"].double!!
        buttonWidth = context["ButtonWidth"].double!!
        buttonHeight = context["ButtonHeight"].double!!
        buttonImage = context["ButtonImage"].string!!
        autoSize = context["AutoSize"].bool

        renderButtonImage = ClientDataHandler.decodeDynamicValue(buttonImage)
    }


    override fun render() {
        Minecraft.getInstance().fontRenderer
        RenderUtil.push {
            RenderUtil.useExtTexture(buttonImage) {
                if(autoSize)
                    RenderUtil.render(buttonX, buttonY, RenderUtil.getWidth().toDouble(), RenderUtil.getHeight().toDouble())
                else RenderUtil.render(buttonX, buttonY, buttonWidth, buttonHeight)
            }
        }
    }

    override fun onEnter() {
        base.appendRenderer(this)
        MinecraftForge.EVENT_BUS.register(this)
    }

    override fun onRemovedFromQueue() {
        MinecraftForge.EVENT_BUS.unregister(this)
    }

    @SubscribeEvent
    fun onDynamicValueUpdated(event: OnDynamicValueUpdateEvent) {
        renderButtonImage = ClientDataHandler.decodeDynamicValue(buttonImage)
    }

    override val renderParse: ScriptGui.RenderParse
        get() = base.Main

    override fun validateKeyInput(gui: ScriptGui, keyCode: Int, scanCode: Int, modifier: Int) = PendingResult.Pass

    override fun validateMouseInput(gui: ScriptGui, mouseX: Double, mouseY: Double, mouseButton: Int)
        = if(mouseX in buttonX..(buttonX + buttonWidth) && mouseY in buttonY..(buttonY + buttonHeight)) PendingResult.Pass else PendingResult.Deny
}