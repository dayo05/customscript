package me.ddayo.customscript.client.gui.script.blocks

import me.ddayo.customscript.client.ClientDataHandler
import me.ddayo.customscript.client.gui.RenderUtil
import me.ddayo.customscript.client.gui.script.ScriptGui
import me.ddayo.customscript.util.options.CompileError
import me.ddayo.customscript.util.options.Option
import me.ddayo.customscript.util.options.Option.Companion.string

class ChangeBackgroundBlock: BlockBase() {
    var image = ""
    var type = 0
    override fun parseContext(context: Option) {
        type = when(context["Type"].string) {
            "Push" -> 0
            "Reset" -> 1
            "Pop" -> 2
            else -> throw CompileError("Not supported value on ChangeBackgroundBlock")
        }
        image = context["Images"].string!!
    }

    override fun onEnter() {
        when(type) {
            0 -> image.split("\n").forEach {
                base.appendRenderer(object: IRendererBlock {
                    override fun render() {
                        RenderUtil.useExtTexture(ClientDataHandler.decodeDynamicValue(it)) {
                            RenderUtil.render()
                        }
                    }

                    override val renderParse: ScriptGui.RenderParse
                        get() = base.Pre
                })
            }
            1 -> {
                base.clearRenderer(base.Pre)
                image.split("\n").forEach {
                    base.appendRenderer(object : IRendererBlock {
                        override fun render() {
                            RenderUtil.useExtTexture(ClientDataHandler.decodeDynamicValue(it)) {
                                RenderUtil.render()
                            }
                        }

                        override val renderParse: ScriptGui.RenderParse
                            get() = base.Pre
                    })
                }
            }
            2 -> base.popRenderer(base.Pre)
        }
    }
}