package me.ddayo.customscript.client.gui.script

import com.mojang.blaze3d.matrix.MatrixStack
import com.mojang.blaze3d.systems.RenderSystem
import me.ddayo.customscript.CustomScript
import me.ddayo.customscript.client.event.OnDynamicValueUpdateEvent
import me.ddayo.customscript.client.gui.script.arrows.Arrow
import me.ddayo.customscript.client.gui.GuiBase
import me.ddayo.customscript.client.gui.RenderUtil
import me.ddayo.customscript.client.gui.script.blocks.*
import me.ddayo.customscript.network.CloseGuiNetworkHandler
import me.ddayo.customscript.util.js.CalculableValueManager
import me.ddayo.customscript.util.js.ICalculableHolder
import me.ddayo.customscript.util.options.CompileError
import me.ddayo.customscript.util.options.Option
import me.ddayo.customscript.util.options.Option.Companion.bool
import me.ddayo.customscript.util.options.Option.Companion.string
import net.minecraft.client.Minecraft
import net.minecraft.util.Util
import net.minecraft.util.text.StringTextComponent
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.registries.ForgeRegistries
import org.apache.logging.log4j.LogManager
import org.apache.maven.artifact.versioning.DefaultArtifactVersion
import org.lwjgl.opengl.GL21
import java.io.File
import java.util.*


class ScriptGui(private val mode: ScriptMode, script: Option, beginPos: String) : GuiBase() {
    companion object {
        val minimumRequiredVersion = DefaultArtifactVersion("0.8")

        fun fromFile(mode: ScriptMode, scriptFile: String, beginPos: String): ScriptGui? {
            val scriptFileRoot =
                if (CustomScript.isTest) File("dummy") else File(Minecraft.getInstance().gameDir, CustomScript.MOD_ID)
            val scFile = if (CustomScript.isTest) File(scriptFile) else File(scriptFileRoot, scriptFile)
            if (!scFile.exists() || !scFile.isFile || !scFile.canRead()) {
                Minecraft.getInstance().player?.sendMessage(
                    StringTextComponent("Cannot open script: $scriptFile"),
                    Util.DUMMY_UUID
                )
                LogManager.getLogger().warn("Cannot open file: $scriptFile")
                return null
            }
            return ScriptGui(mode, Option.readOption(scFile.readText()), beginPos)
        }

        fun fromString(mode: ScriptMode, script: String, beginPos: String): ScriptGui {
            return ScriptGui(mode, Option.readOption(script), beginPos)
        }
    }

    enum class RenderParse {
        Pre, Main, Post
    }


    init {
        MinecraftForge.EVENT_BUS.register(this)
        if (minimumRequiredVersion > DefaultArtifactVersion(script["Version"].string)) {
            Minecraft.getInstance().player?.sendMessage(
                StringTextComponent("Not supported version: ${script["Version"].string}, Required: $minimumRequiredVersion"),
                UUID.randomUUID()
            )
            throw CompileError("Not supported version: ${script["Version"].string}, Required: $minimumRequiredVersion")
        }
    }

    private val canMovePrevious = script["CanMovePrevious"].bool ?: false
    private val blocks = script["Block"].map { BlockBase.createBlock(it.value, it, this) }
    private val arrows = script["Arrow"].map { Arrow.createArrow(it.value, it) }

    val renderable =
        mapOf(*RenderParse.values().map { Pair(it, emptyList<ScriptRenderer>().toMutableList()) }.toTypedArray())
    private var current = blocks.filter { it is BeginBlock && it.label == beginPos }

    init {
        if (current.isEmpty()) throw CompileError("There are no begin block with label $beginPos")
    }

    private var pending = false

    private val trackPrev = Stack<Int>()

    init {
        moveNext()
    }

    public fun moveNext() {
        if (pending) return
        trackPrev.push(current.first().ns)
        current = current.flatMap {
            arrows.filter { it.from == trackPrev.peek() }.flatMap { to -> blocks.filter { it.ns == to.to } }
        }

        if (current.size > 2) {
            if (current.any { it !is PendingBlock }) throw CompileError("There are two+ non-multi-selectable blocks which is connect to current pos")
            current.forEach {
                it.onEnter()
            }
            pending = true
        } else if (current.isEmpty()) closeScreen()
        else {
            current.first().let {
                it.onEnter()
                pending = it is PendingBlock
            }
            if (!pending) moveNext()
        }
    }

    public fun movePrev() {
        if (!pending) return
        current.forEach { (it as PendingBlock).onExitPending() }
        while (true) {
            val b = blocks.first { it.ns == trackPrev.peek() }
            if (b is PendingBlock) {
                trackPrev.pop()
                val cached = arrows.filter { it.from == trackPrev.peek() }.map { it.to }
                current = blocks.filter { cached.contains(it.ns) }
                break
            }
            b.onRevert()
            trackPrev.pop()
        }
    }

    public fun cancelPending(to: Int) {
        if (!current.any { it.ns == to }) throw IllegalStateException("Not able to load $to")
        if (!pending) throw IllegalStateException("Function is not pending")

        current.forEach {
            if (it !is PendingBlock) throw IllegalStateException("Not pending block")
            it.onExitPending()
        }

        clearRenderer(RenderParse.Main)
        clearRenderer(RenderParse.Post)

        trackPrev.push(to)
        current = current.filter { it.ns == to }
            .flatMap { arrows.filter { it.from == to }.flatMap { to -> blocks.filter { it.ns == to.to } } }

        if (current.size > 2) {
            if (current.any { it !is PendingBlock }) throw CompileError("There are two+ non-multi-selectable blocks which is connect to current pos")
            pending = true
        } else if (current.isEmpty()) {
            if (!CustomScript.isTest) closeScreen()
        } else {
            current.first().let {
                it.onEnter()
                pending = it is PendingBlock
            }
            if (!pending) moveNext()
        }
    }

    private val renderInit by lazy {
        if (mode == ScriptMode.Gui)
            Minecraft.getInstance().mouseHelper.ungrabMouse()
    }

    override fun render(matrixStack: MatrixStack, mouseX: Int, mouseY: Int, partialTicks: Float) {
        renderInit

        Minecraft.getInstance().mainWindow.let { w ->
            CalculableValueManager.setValueQuiet(
                "mouseX",
                (mouseX - (w.scaledWidth - w.scaledHeight * 16 / 9) / 2) * 1080 / w.scaledHeight
            )
            CalculableValueManager.setValueQuiet(
                "mouseY",
                mouseY * 1080 / w.scaledHeight
            )
            CalculableValueManager.setValueQuiet("partialTick", partialTicks)
        }
        CalculableValueManager.frame()

        RenderSystem.enableBlend()
        RenderUtil.renderer.loadMatrix(matrixStack) {
            FHDScale {
                RenderParse.values().forEach {
                    renderable[it]!!.forEach { f -> if(!f.isLoading) f.render(RenderUtil.renderer) }
                }
            }
        }
        RenderSystem.disableBlend()

        //ForgeRegistries.ITEMS.getValue()
    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int) = mouseHandler(mouseX, mouseY) { mx, my ->
        if (!pending) super.mouseClicked(mouseX, mouseY, button)

        if (canMovePrevious && button == 1) {
            movePrev()
            return@mouseHandler super.mouseClicked(mouseX, mouseY, button)
        }
        for (x in current) {
            val k = (x as PendingBlock).validateMouseInput(this, mx, my, button)
            if (k == PendingBlock.PendingResult.Pass) {
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
            if (k == PendingBlock.PendingResult.Pass) {
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

    fun appendRenderer(block: ScriptRenderer) {
        renderable[block.renderParse]!!.add(block)
        if (block is ICalculableHolder)
            CalculableValueManager.dynamicValueHolder.add(block)
    }

    fun popRenderer(block: ScriptRenderer) = popRenderer(block.renderParse)
    fun popRenderer(parse: RenderParse): ScriptRenderer {
        val r = renderable[parse]!!.removeLast().apply {
            onRemovedFromQueue()
        }
        if(r is ICalculableHolder)
            CalculableValueManager.dynamicValueHolder.remove(r)
        return r
    }

    fun clearRenderer(block: ScriptRenderer) = clearRenderer(block.renderParse)
    fun clearRenderer(parse: RenderParse) {
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
        finish()
        super.onClose()
    }

    fun finish() {
        if (mode == ScriptMode.Gui)
            CustomScript.network.sendToServer(CloseGuiNetworkHandler())
        RenderParse.values().forEach {
            clearRenderer(it)
        }

        MinecraftForge.EVENT_BUS.unregister(this)
        //RenderUtil.removeAllTextures()
    }
}

enum class ScriptMode {
    Gui, Hud
}