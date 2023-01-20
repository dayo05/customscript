package me.ddayo.customscript.client.gui.script.blocks

import me.ddayo.customscript.client.gui.RenderUtil
import me.ddayo.customscript.client.gui.script.ScriptGui
import me.ddayo.customscript.util.options.CalculableValue
import me.ddayo.customscript.util.options.CompileError
import me.ddayo.customscript.util.options.Option
import me.ddayo.customscript.util.options.Option.Companion.string
import java.util.Stack

class ChangeBackgroundBlock: BlockBase() {
    private class BackgroundRenderer(private val image: CalculableValue): ScriptRenderer() {
        override val renderParse: ScriptGui.RenderParse
            get() = ScriptGui.RenderParse.Pre

        override fun render() {
            image.string.split("\n").forEach {
                if(it != "null" && it.isNotBlank())
                    RenderUtil.useExtTexture(it) { RenderUtil.render() }
            }
        }
    }

    private lateinit var image: CalculableValue
    private var type = 0

    override fun parseContext(context: Option) {
        type = when (context["Type"].string) {
            "Push" -> 0
            "Reset" -> 1
            "Pop" -> 2
            else -> throw CompileError("Not supported value on ChangeBackgroundBlock")
        }
        image = CalculableValue(context["Images"].string!!, true)
    }

    override fun onEnter() {
        when (type) {
            0 -> {
                base.appendRenderer(BackgroundRenderer(image))
            }
            1 -> {
                base.clearRenderer(ScriptGui.RenderParse.Pre)
                base.appendRenderer(BackgroundRenderer(image))
            }
            2 -> {
                poppedRenderer.push(base.popRenderer(ScriptGui.RenderParse.Pre))
            }
        }
    }

    private val poppedRenderer = Stack<ScriptRenderer>()
    override fun onRevert() {
        super.onRevert()
        if(type == 2)
            base.appendRenderer(poppedRenderer.pop())
        else base.popRenderer(ScriptGui.RenderParse.Pre)
    }
}