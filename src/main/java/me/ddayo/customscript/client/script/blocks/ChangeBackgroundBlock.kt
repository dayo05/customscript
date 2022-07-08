package me.ddayo.customscript.client.script.blocks

import me.ddayo.customscript.util.options.Option

class ChangeBackgroundBlock: BlockBase() {
    var image = ""
    override fun parseContext(context: Option) {
        throw NotImplementedError("changebackgroundblock not implemented")
    }

    override fun onEnter() {
        base.clearBackground()
        image.split("\n").forEach { base.appendBackground(it) }
    }
}