package me.ddayo.customscript.client.gui.script.blocks

import me.ddayo.customscript.client.ClientDataHandler
import me.ddayo.customscript.client.event.OnDynamicValueUpdateEvent
import me.ddayo.customscript.client.gui.RenderUtil
import me.ddayo.customscript.client.gui.script.ScriptGui
import me.ddayo.customscript.util.options.CompileError
import me.ddayo.customscript.util.options.Option
import me.ddayo.customscript.util.options.Option.Companion.string
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.eventbus.api.SubscribeEvent

class ChangeBackgroundBlock: BlockBase() {
    var image = ""
    var type = 0
    override fun parseContext(context: Option) {
        type = when (context["Type"].string) {
            "Push" -> 0
            "Reset" -> 1
            "Pop" -> 2
            else -> throw CompileError("Not supported value on ChangeBackgroundBlock")
        }
        image = context["Images"].string!!
    }

    override fun onEnter() {
        when (type) {
            0 -> image.split("\n").forEach {
                base.appendRenderer(ImageRenderer(it))
            }

            1 -> {
                base.clearRenderer(base.Pre)
                image.split("\n").forEach {
                    base.appendRenderer(ImageRenderer(it))
                }
            }

            2 -> base.popRenderer(base.Pre)
        }
    }

    private class ImageRenderer(val image: String) : IRendererBlock {
        var imageText = ClientDataHandler.decodeDynamicValue(image)
        init {
            MinecraftForge.EVENT_BUS.register(this)
        }

        @SubscribeEvent
        fun onDynamicValueUpdated(event: OnDynamicValueUpdateEvent) {
            imageText = ClientDataHandler.decodeDynamicValue(image)
        }

        override fun onRemovedFromQueue() {
            MinecraftForge.EVENT_BUS.unregister(this)
        }

        override fun render() {
            ClientDataHandler.decodeDynamicValue(imageText).run {
                if (this != "null")
                    RenderUtil.useExtTexture(this) {
                        RenderUtil.render()
                    }
            }
        }

        override val renderParse: ScriptGui.RenderParse
            get() = ScriptGui.RenderParse.Pre
    }
}