package me.ddayo.customscript.client.script

import com.mojang.blaze3d.matrix.MatrixStack
import me.ddayo.customscript.CustomScript
import me.ddayo.customscript.client.script.arrows.ArrowBase
import me.ddayo.customscript.client.script.blocks.BeginBlock
import me.ddayo.customscript.client.script.blocks.BlockBase
import me.ddayo.customscript.client.script.blocks.MultiSelectableBlock
import me.ddayo.customscript.client.script.blocks.PendingBlock
import me.ddayo.customscript.client.gui.GuiBase
import me.ddayo.customscript.client.gui.IRenderable
import me.ddayo.customscript.client.gui.RenderUtil
import me.ddayo.customscript.util.options.CompileError
import me.ddayo.customscript.util.options.Option
import net.minecraft.client.Minecraft
import java.io.File


class ScriptGui(scriptFile: String, beginPos: String): GuiBase() {
    private val scriptFileRoot =
        if (CustomScript.isTest) File("dummy") else File(Minecraft.getInstance().gameDir, CustomScript.MOD_ID)
    private val scFile = if (CustomScript.isTest) File(scriptFile) else File(scriptFileRoot, scriptFile)

    private val script = Option.readOption(if (scFile.exists() && scFile.isFile && scFile.canRead()) scFile.readText() else "")
    private val blocks = script["Block"].map { BlockBase.createBlock(it.value, it, this) }
    private val arrows = script["Arrow"].map { ArrowBase.createArrow(it.value, it) }

    private var current = blocks.filter { it is BeginBlock && it.label == beginPos }
    init { if(current.isEmpty()) throw CompileError("There are no begin block with label $beginPos") }

    private var pending = false

    private var prevNs = -1

    init { moveNext() }

    public fun moveNext() {
        if(pending) return
        prevNs = current.first().ns
        current = current.flatMap { arrows.filter { it.from == prevNs }.flatMap { to -> blocks.filter { it.ns == to.to }} }

        if(current.size > 2) {
            if (current.any { it !is MultiSelectableBlock }) throw CompileError("There are two+ non-multi-selectable blocks which is connect to current pos")
            pending = true
        }
        else if(current.isEmpty()) closeScreen()
        else {
            current.first().onEnter()
            pending = current.first() is PendingBlock
            if(!pending) moveNext()
        }
    }

    public fun cancelPending(to: Int) {
        if(!current.any { it.ns == to }) throw IllegalStateException("Not able to load $to")
        if(!pending) throw IllegalStateException("Function is not pending")

        prevNs = current.first().ns
        current = current.filter { it.ns == to }.flatMap { arrows.filter { it.from == to }.flatMap { to -> blocks.filter { it.ns == to.to }} }

        if(current.size > 2) {
            if (current.any { it !is MultiSelectableBlock }) throw CompileError("There are two+ non-multi-selectable blocks which is connect to current pos")
            pending = true
        }
        else if(current.isEmpty()) {
            if (!CustomScript.isTest) closeScreen()
        }
        else {
            current.first().onEnter()
            pending = current.first() is PendingBlock
            if(!pending) moveNext()
        }
    }

    override fun render(matrixStack: MatrixStack, mouseX: Int, mouseY: Int, partialTicks: Float) {
        bgi.forEach { RenderUtil.useExtTexture(it) { RenderUtil.render() } }
        FHDScale { current.filter { it is IRenderable }.forEach { (it as IRenderable).render() } }
    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int) = mouseHandler(mouseX, mouseY) { mx, my ->
        if (!pending) super.mouseClicked(mouseX, mouseY, button)

        for(x in current)
            if (arrows.firstOrNull { it.from == prevNs && it.to == x.ns }.run { this?.onMouseInput(this@ScriptGui, mx, my, button) == true }
                && (x as PendingBlock).validateMouseInput(this, mx, my, button)) {
                cancelPending(x.ns)
                break
            }
        super.mouseClicked(mouseX, mouseY, button)
    }

    public val alphabetState = MutableList('Z'.code - 'A'.code + 1) { false }
    public val numberState = MutableList(10) { false }

    override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        if(keyCode in 'A'.code..'Z'.code)
            alphabetState[keyCode - 'a'.code] = true
        else if(keyCode in '0'.code..'9'.code)
            numberState[keyCode - '0'.code] = true

        if (!pending) return super.keyPressed(keyCode, scanCode, modifiers)

        for(x in current)
            if(arrows.first { it.from == prevNs && it.to == x.ns }.onKeyboardInput(this, keyCode, scanCode, modifiers)
                && (x as PendingBlock).validateKeyInput(this, keyCode, scanCode, modifiers)) {
                cancelPending(x.ns)
                break
            }
        return super.keyPressed(keyCode, scanCode, modifiers)
    }

    override fun keyReleased(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        if(keyCode in 'A'.code..'Z'.code)
            alphabetState[keyCode - 'A'.code] = false
        else if(keyCode in '0'.code..'9'.code)
            numberState[keyCode - '0'.code] = false
        return super.keyReleased(keyCode, scanCode, modifiers)
    }



    private val bgi = emptyList<String>().toMutableList()
    fun appendBackground(image: String) = bgi.add(image)
    fun clearBackground() = bgi.clear()

    companion object {

    }
}