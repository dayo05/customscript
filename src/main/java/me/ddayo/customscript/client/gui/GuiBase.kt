package me.ddayo.customscript.client.gui

import net.minecraft.client.Minecraft
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.util.InputMappings
import net.minecraft.util.text.StringTextComponent
import org.lwjgl.glfw.GLFW

open class GuiBase: Screen(StringTextComponent.EMPTY) {
    protected fun render() = RenderUtil.render()

    protected fun render(x: Int, y: Int, w: Int, h: Int) = RenderUtil.render(x, y, w, h)

    protected fun render(x: Double, y: Double, w: Double, h: Double) = RenderUtil.render(x, y, w, h)

    protected fun push(x: () -> Unit) = RenderUtil.push(x)

    protected fun FHDScale(x: () -> Unit) = RenderUtil.FHDScale(width, height, x)

    protected fun mouseHandler(mouseX: Double, mouseY: Double, x: (Double, Double) -> Boolean) = x((mouseX - ((width - height * 16.0 / 9) / 2)) * 1080 / height, mouseY * 1080 / height)

    override fun init() {
        Minecraft.getInstance().skipRenderWorld = false
        super.init()
    }
}