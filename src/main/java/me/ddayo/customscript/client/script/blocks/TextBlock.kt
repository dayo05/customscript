package me.ddayo.customscript.client.script.blocks

import com.mojang.blaze3d.matrix.MatrixStack
import me.ddayo.customscript.client.gui.IRenderable
import me.ddayo.customscript.client.gui.RenderUtil
import me.ddayo.customscript.client.script.ScriptGui
import me.ddayo.customscript.util.options.Option
import me.ddayo.customscript.util.options.Option.Companion.double
import me.ddayo.customscript.util.options.Option.Companion.string
import net.minecraft.client.Minecraft
import org.lwjgl.glfw.GLFW
import org.lwjgl.opengl.GL21

open class TextBlock: PendingBlock(), IRenderable {
    var text = ""
        private set
    var textX = 0.0
        private set
    var textY = 0.0
        private set
    var textScale = 0.0
        private set

    override fun parseContext(context: Option) {
        text = context["Text"].string
        textX = context["TextX"].double
        textY = context["TextY"].double
        textScale = context["TextScale"].double
    }

    override fun render() {
        RenderUtil.push {
            GL21.glScaled(textScale, textScale, textScale)
            Minecraft.getInstance().fontRenderer.drawString(
                MatrixStack(),
                text,
                textX.toFloat(),
                textY.toFloat(),
                0xffffff
            )
        }
    }

    override fun validateMouseInput(gui: ScriptGui, mouseX: Double, mouseY: Double, mouseButton: Int) = true

    override fun validateKeyInput(gui: ScriptGui, keyCode: Int, scanCode: Int, modifier: Int) = keyCode == GLFW.GLFW_KEY_SPACE
}