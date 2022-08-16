package me.ddayo.customscript.client.gui.script.blocks

import com.mojang.blaze3d.matrix.MatrixStack
import me.ddayo.customscript.client.ClientDataHandler
import me.ddayo.customscript.client.gui.RenderUtil
import me.ddayo.customscript.client.gui.script.ScriptGui
import me.ddayo.customscript.util.options.Option
import me.ddayo.customscript.util.options.Option.Companion.double
import me.ddayo.customscript.util.options.Option.Companion.string
import net.minecraft.client.Minecraft
import org.lwjgl.opengl.GL21

class ButtonBlock: MultiSelectableBlock(), IRendererBlock {
    var buttonX = 0.0
        private set
    var buttonY = 0.0
        private set
    var buttonWidth = 0.0
        private set
    var buttonHeight = 0.0
        private set
    var buttonImage = ""
        private set

    override fun parseContext(context: Option) {
        buttonX = context["ButtonX"].double!!
        buttonY = context["ButtonY"].double!!
        buttonWidth = context["ButtonWidth"].double!!
        buttonHeight = context["ButtonHeight"].double!!
        buttonImage = context["ButtonImage"].string!!
    }

    override fun render() {
        Minecraft.getInstance().fontRenderer
        RenderUtil.push {
            RenderUtil.useExtTexture(ClientDataHandler.decodeDynamicValue(buttonImage)) {
                RenderUtil.render(buttonX, buttonY, buttonWidth, buttonHeight)
            }
        }
    }

    override fun onEnter() {
        base.appendRenderer(this)
    }

    override val renderParse: ScriptGui.RenderParse
        get() = base.Main

    override fun validateKeyInput(gui: ScriptGui, keyCode: Int, scanCode: Int, modifier: Int) = PendingResult.Pass

    override fun validateMouseInput(gui: ScriptGui, mouseX: Double, mouseY: Double, mouseButton: Int)
        = if(mouseX in buttonX..(buttonX + buttonWidth) && mouseY in buttonY..(buttonY + buttonHeight)) PendingResult.Pass else PendingResult.Deny
}