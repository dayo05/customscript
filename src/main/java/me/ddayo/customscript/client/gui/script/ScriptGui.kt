package me.ddayo.customscript.client.gui.script

import com.mojang.blaze3d.matrix.MatrixStack
import me.ddayo.customscript.CustomScript
import me.ddayo.customscript.client.gui.script.arrows.ArrowBase
import me.ddayo.customscript.client.gui.GuiBase
import me.ddayo.customscript.client.gui.script.blocks.*
import me.ddayo.customscript.network.CloseGuiNetworkHandler
import me.ddayo.customscript.util.options.CompileError
import me.ddayo.customscript.util.options.Option
import net.minecraft.client.Minecraft
import java.io.File


class ScriptGui(scriptFile: String, beginPos: String): GuiBase() {
    enum class RenderParse {
        Pre, Main, Post
    }

    public val Pre = RenderParse.Pre
    public val Main = RenderParse.Main
    public val Post = RenderParse.Post

    private val scriptFileRoot =
            if (CustomScript.isTest) File("dummy") else File(Minecraft.getInstance().gameDir, CustomScript.MOD_ID)
    private val scFile = if (CustomScript.isTest) File(scriptFile) else File(scriptFileRoot, scriptFile)

    private val script = Option.readOption(if (scFile.exists() && scFile.isFile && scFile.canRead()) scFile.readText() else "")
    private val blocks = script["Block"].map { BlockBase.createBlock(it.value, it, this) }
    private val arrows = script["Arrow"].map { ArrowBase.createArrow(it.value, it) }

    private val renderable = mapOf(Pair(RenderParse.Pre, emptyList<IRendererBlock>().toMutableList()),
            Pair(RenderParse.Main, emptyList<IRendererBlock>().toMutableList()),
            Pair(RenderParse.Post, emptyList<IRendererBlock>().toMutableList()))

    private var current = blocks.filter { it is BeginBlock && it.label == beginPos }

    init {
        if (current.isEmpty()) throw CompileError("There are no begin block with label $beginPos")
    }

    private var pending = false

    private var prevNs = -1

    init {
        moveNext()
    }

    public fun moveNext() {
        if (pending) return
        prevNs = current.first().ns
        current = current.flatMap { arrows.filter { it.from == prevNs }.flatMap { to -> blocks.filter { it.ns == to.to } } }

        if (current.size > 2) {
            if (current.any { it !is MultiSelectableBlock }) throw CompileError("There are two+ non-multi-selectable blocks which is connect to current pos")
            current.forEach { it.onEnter() }
            pending = true
        } else if (current.isEmpty()) closeScreen()
        else {
            current.first().onEnter()
            pending = current.first() is PendingBlock
            if (!pending) moveNext()
        }
    }

    public fun cancelPending(to: Int) {
        clearRenderer(RenderParse.Main)
        clearRenderer(RenderParse.Post)
        if (!current.any { it.ns == to }) throw IllegalStateException("Not able to load $to")
        if (!pending) throw IllegalStateException("Function is not pending")

        current.forEach {
            if (it !is PendingBlock) throw IllegalStateException("Not pending block")
            it.onExitPending()
        }

        prevNs = current.first().ns
        current = current.filter { it.ns == to }.flatMap { arrows.filter { it.from == to }.flatMap { to -> blocks.filter { it.ns == to.to } } }

        if (current.size > 2) {
            if (current.any { it !is MultiSelectableBlock }) throw CompileError("There are two+ non-multi-selectable blocks which is connect to current pos")
            pending = true
        } else if (current.isEmpty()) {
            if (!CustomScript.isTest) closeScreen()
        } else {
            current.first().onEnter()
            pending = current.first() is PendingBlock
            if (!pending) moveNext()
        }
    }

    public override fun render(matrixStack: MatrixStack, mouseX: Int, mouseY: Int, partialTicks: Float) {
        FHDScale {
            renderable[RenderParse.Pre]!!.forEach { it.render() }
            renderable[RenderParse.Main]!!.forEach { it.render() }
            renderable[RenderParse.Post]!!.forEach { it.render() }
        }
    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int) = mouseHandler(mouseX, mouseY) { mx, my ->
        if (!pending) super.mouseClicked(mouseX, mouseY, button)

        for (x in current) {
            val k = (x as PendingBlock).validateMouseInput(this, mx, my, button)
            if (k == PendingBlock.PendingResult.Force || arrows.firstOrNull { it.from == prevNs && it.to == x.ns }.run { this?.onMouseInput(this@ScriptGui, mx, my, button) == true }
                    && k == PendingBlock.PendingResult.Pass) {
                cancelPending(x.ns)
                break
            }
        }
        super.mouseClicked(mouseX, mouseY, button)
    }

    public val alphabetState = MutableList('Z'.code - 'A'.code + 1) { false }
    public val numberState = MutableList(10) { false }

    override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        if (keyCode in 'A'.code..'Z'.code)
            alphabetState[keyCode - 'A'.code] = true
        else if (keyCode in '0'.code..'9'.code)
            numberState[keyCode - '0'.code] = true

        if (!pending) return super.keyPressed(keyCode, scanCode, modifiers)

        for (x in current) {
            val k = (x as PendingBlock).validateKeyInput(this, keyCode, scanCode, modifiers)
            if (k == PendingBlock.PendingResult.Force || arrows.first { it.from == prevNs && it.to == x.ns }.onKeyboardInput(this, keyCode, scanCode, modifiers)
                    && k == PendingBlock.PendingResult.Pass) {
                cancelPending(x.ns)
                break
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers)
    }

    override fun keyReleased(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        if (keyCode in 'A'.code..'Z'.code)
            alphabetState[keyCode - 'A'.code] = false
        else if (keyCode in '0'.code..'9'.code)
            numberState[keyCode - '0'.code] = false
        return super.keyReleased(keyCode, scanCode, modifiers)
    }

    fun appendRenderer(block: IRendererBlock) = renderable[block.renderParse]!!.add(block)
    fun popRenderer(block: IRendererBlock) = popRenderer(block.renderParse)
    fun popRenderer(parse: RenderParse) {
        renderable[parse]!!.last().onRemovedFromQueue()
        renderable[parse]!!.removeLast()
    }
    fun clearRenderer(block: IRendererBlock) = clearRenderer(block.renderParse)
    fun clearRenderer(parse: RenderParse){
        renderable[parse]!!.forEach { it.onRemovedFromQueue() }
        renderable[parse]!!.clear()
    }

    override fun tick() {
        super.tick()
        if (!pending) return
        current.forEach {
            if ((it as PendingBlock).tick() != PendingBlock.PendingResult.Deny)
                cancelPending(it.ns)
        }
    }

    override fun onClose() {
        RenderParse.values().forEach {
            clearRenderer(it)
        }
        CustomScript.network.sendToServer(CloseGuiNetworkHandler())
        super.onClose()
    }
}