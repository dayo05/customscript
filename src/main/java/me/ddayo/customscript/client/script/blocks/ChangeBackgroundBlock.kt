package me.ddayo.customscript.client.script.blocks

import me.ddayo.customscript.util.options.CompileError
import me.ddayo.customscript.util.options.Option
import me.ddayo.customscript.util.options.Option.Companion.int
import me.ddayo.customscript.util.options.Option.Companion.string

class ChangeBackgroundBlock: BlockBase() {
    var image = ""
    var type = 0
    override fun parseContext(context: Option) {
        type = when(context["Type"].string) {
            "Push" -> 0
            "Pop" -> 2
            "Reset" -> 1
            else -> throw CompileError("Not supported value on ChangeBackgroundBlock")
        }
        image = context["Images"].string
    }

    override fun onEnter() {
        when(type) {
            0 -> image.split("\n").forEach { base.appendBackground(it) }
            1 -> {
                base.clearBackground()
                image.split("\n").forEach { base.appendBackground(it) }
            }
            2 -> base.popBackground()
        }
    }
}