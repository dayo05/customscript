package me.ddayo.customscript.client.gui.script.blocks

import me.ddayo.customscript.client.gui.ImageResource
import me.ddayo.customscript.client.gui.RenderUtil
import me.ddayo.customscript.client.gui.script.ScriptGui
import me.ddayo.customscript.util.js.AbstractCalculable
import me.ddayo.customscript.util.js.DoubleCalculable
import me.ddayo.customscript.util.js.ICalculableHolder
import me.ddayo.customscript.util.js.StringCalculable
import me.ddayo.customscript.util.options.CompileError
import me.ddayo.customscript.util.options.Option
import me.ddayo.customscript.util.options.Option.Companion.bool
import me.ddayo.customscript.util.options.Option.Companion.string
import java.util.Stack

class ChangeBackgroundBlock : BlockBase() {
    private class BackgroundRenderer (
        private val image: StringCalculable,
        private val biasX: DoubleCalculable?,
        private val biasY: DoubleCalculable?,
        private val width: DoubleCalculable?,
        private val height: DoubleCalculable?,
        private val customPos: Boolean,
        private val autoSize: Boolean,
        private val parse: String
    ) : ScriptRenderer(), ICalculableHolder {
        override fun RenderUtil.renderInternal() {
            image.get.split("\n").forEach {
                if (it.isNotBlank())
                    useTexture(ImageResource.getOrCreate(it)) {
                        if(!customPos)
                            render()
                        else if (autoSize)
                            render(biasX!!.get.toInt(), biasY!!.get.toInt(), getTexWidth(), getTexHeight())
                        else render(biasX!!.get, biasY!!.get, width!!.get, height!!.get)
                    }
            }
        }

        override val renderParse: ScriptGui.RenderParse
            get() = ScriptGui.RenderParse.valueOf(parse)
        override val calculable = listOfNotNull(image, biasX, biasY, width, height)
        override val isLoading: Boolean
            get() = image.get.split("\n").any { !ImageResource.getOrCreate(it).isLoaded }
    }

    private lateinit var image: StringCalculable
    private var type = 0
    private var customPos = false
    private var biasX: DoubleCalculable? = null
    private var biasY: DoubleCalculable? = null
    private var width: DoubleCalculable? = null
    private var height: DoubleCalculable? = null
    private var autoSize = false
    private var parse = ""

    override fun parseContext(context: Option) {
        type = when (context["Type"].string) {
            "Push" -> 0
            "Reset" -> 1
            "Pop" -> 2
            else -> throw CompileError("Not supported value on ChangeBackgroundBlock")
        }
        image = StringCalculable(context["Images"].string!!)
        customPos = context["CustomPos"].bool == true
        if(customPos) {
            biasX = DoubleCalculable(context["BiasX"].string!!)
            biasY = DoubleCalculable(context["BiasY"].string!!)
            autoSize = context["AutoSize"].bool == true
            if (!autoSize) {
                width = DoubleCalculable(context["Width"].string!!)
                height = DoubleCalculable(context["Height"].string!!)
            }
        }
        parse = context["Parse"].string ?: "Pre"
    }

    override fun onEnter() {
        when (type) {
            0 -> {
                base.appendRenderer(BackgroundRenderer(image, biasX, biasY, width, height, customPos, autoSize, parse))
            }
            1 -> {
                base.clearRenderer(ScriptGui.RenderParse.Pre)
                base.appendRenderer(BackgroundRenderer(image, biasX, biasY, width, height, customPos, autoSize, parse))
            }
            2 -> {
                poppedRenderer.push(base.popRenderer(ScriptGui.RenderParse.Pre))
            }
        }
    }

    private val poppedRenderer = Stack<ScriptRenderer>()
    override fun onRevert() {
        super.onRevert()
        if (type == 2)
            base.appendRenderer(poppedRenderer.pop())
        else base.popRenderer(ScriptGui.RenderParse.Pre)
    }
}