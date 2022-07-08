package me.ddayo.customscript.client.script.blocks

import me.ddayo.customscript.util.options.Option
import me.ddayo.customscript.util.options.Option.Companion.string

class BeginBlock: BlockBase() {
    var label: String = ""
        private set
    override fun parseContext(context: Option) {
        label = context["Label"].string
    }
}