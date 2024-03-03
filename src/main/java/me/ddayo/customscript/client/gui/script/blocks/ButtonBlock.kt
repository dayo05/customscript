package me.ddayo.customscript.client.gui.script.blocks

import me.ddayo.customscript.client.gui.ImageResource
import me.ddayo.customscript.client.gui.RenderUtil
import me.ddayo.customscript.client.gui.script.ScriptGui
import me.ddayo.customscript.util.js.*
import me.ddayo.customscript.util.options.Option
import me.ddayo.customscript.util.options.Option.Companion.string
import me.ddayo.customscript.util.options.Option.Companion.bool
import net.minecraft.client.Minecraft

class ButtonBlock : PendingBlock() {
    private class ButtonRenderer(
        private val buttonX: DoubleCalculable,
        private val buttonY: DoubleCalculable,
        private val buttonWidth: DoubleCalculable,
        private val buttonHeight: DoubleCalculable,
        private val buttonImage: StringCalculable,
        private val autoSize: Boolean
    ) : ScriptRenderer(), ICalculableHolder {
        override fun RenderUtil.renderInternal() {
            if (buttonImage.get.isNotBlank())
                push {
                    useTexture(ImageResource.getOrCreate(buttonImage.get)) {
                        if (autoSize)
                            render(buttonX.get.toInt(), buttonY.get.toInt(), getTexWidth(), getTexHeight())
                        else render(buttonX.get, buttonY.get, buttonWidth.get, buttonHeight.get)
                    }
                }
        }

        override val renderParse: ScriptGui.RenderParse
            get() = ScriptGui.RenderParse.Main
        override val isLoading: Boolean
            get() = !ImageResource.getOrCreate(buttonImage.get).isLoaded

        override val calculable by lazy { listOf(buttonX, buttonY, buttonWidth, buttonHeight, buttonImage) }
    }

    private lateinit var buttonX: DoubleCalculable
    private lateinit var buttonY: DoubleCalculable
    private lateinit var buttonWidth: DoubleCalculable
    private lateinit var buttonHeight: DoubleCalculable
    private lateinit var buttonImage: StringCalculable
    private var autoSize = false
    private var advancedHitBox = false
    private var hitBox = ""

    override fun parseContext(context: Option) {
        buttonX = DoubleCalculable(context["ButtonX"].string!!)
        buttonY = DoubleCalculable(context["ButtonY"].string!!)
        buttonWidth = DoubleCalculable(context["ButtonWidth"].string!!)
        buttonHeight = DoubleCalculable(context["ButtonHeight"].string!!)
        buttonImage = StringCalculable(context["ButtonImage"].string!!)
        autoSize = context["AutoSize"].bool ?: false
        advancedHitBox = context["AdvancedHitBox"].bool ?: false
        hitBox = context["HitBox"].string ?: ""
    }

    override val rendererInstance: ScriptRenderer
        get() = ButtonRenderer(buttonX, buttonY, buttonWidth, buttonHeight, buttonImage, autoSize)

    override fun validateKeyInput(gui: ScriptGui, keyCode: Int, scanCode: Int, modifier: Int) = PendingResult.Deny

    override fun validateMouseInput(gui: ScriptGui, mouseX: Double, mouseY: Double, mouseButton: Int) =
        if (mouseX in buttonX.get..(buttonX.get + buttonWidth.get) && mouseY in buttonY.get..(buttonY.get + buttonHeight.get)) PendingResult.Pass else PendingResult.Deny

}