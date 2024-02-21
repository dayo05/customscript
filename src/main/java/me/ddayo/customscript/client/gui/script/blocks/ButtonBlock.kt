package me.ddayo.customscript.client.gui.script.blocks

import me.ddayo.customscript.client.gui.RenderUtil
import me.ddayo.customscript.client.gui.script.ScriptGui
import me.ddayo.customscript.util.options.CalculableValue
import me.ddayo.customscript.util.options.Option
import me.ddayo.customscript.util.options.Option.Companion.string
import me.ddayo.customscript.util.options.Option.Companion.bool
import net.minecraft.client.Minecraft

class ButtonBlock: PendingBlock() {
    private class ButtonRenderer(
        private val buttonX: CalculableValue,
        private val buttonY: CalculableValue,
        private val buttonWidth: CalculableValue,
        private val buttonHeight: CalculableValue,
        private val buttonImage: CalculableValue,
        private val autoSize: Boolean
    ) : ScriptRenderer() {
        override fun render() {
            Minecraft.getInstance().fontRenderer
            RenderUtil.push {
                RenderUtil.useExtTexture(buttonImage.string) {
                    if (autoSize)
                        RenderUtil.render(buttonX.int, buttonY.int, RenderUtil.getWidth(), RenderUtil.getHeight())
                    else RenderUtil.render(buttonX.int, buttonY.int, buttonWidth.int, buttonHeight.int)
                }
            }
        }

        override val renderParse: ScriptGui.RenderParse
            get() = ScriptGui.RenderParse.Main
    }

    private lateinit var buttonX: CalculableValue
    private lateinit var buttonY: CalculableValue
    private lateinit var buttonWidth: CalculableValue
    private lateinit var buttonHeight: CalculableValue
    private lateinit var buttonImage: CalculableValue
    private var autoSize = false

    override fun parseContext(context: Option) {
        buttonX = CalculableValue(context["ButtonX"].string!!)
        buttonY = CalculableValue(context["ButtonY"].string!!)
        buttonWidth = CalculableValue(context["ButtonWidth"].string!!)
        buttonHeight = CalculableValue(context["ButtonHeight"].string!!)
        buttonImage = CalculableValue(context["ButtonImage"].string!!, true)
        autoSize = context["AutoSize"].bool ?: false
    }

    override val rendererInstance: ScriptRenderer?
        get() = ButtonRenderer(buttonX, buttonY, buttonWidth, buttonHeight, buttonImage, autoSize)

    override fun validateKeyInput(gui: ScriptGui, keyCode: Int, scanCode: Int, modifier: Int) = PendingResult.Pass

    override fun validateMouseInput(gui: ScriptGui, mouseX: Double, mouseY: Double, mouseButton: Int) =
        if (mouseX in buttonX.double..(buttonX.double + buttonWidth.double) && mouseY in buttonY.double..(buttonY.double + buttonHeight.double)) PendingResult.Pass else PendingResult.Deny
}