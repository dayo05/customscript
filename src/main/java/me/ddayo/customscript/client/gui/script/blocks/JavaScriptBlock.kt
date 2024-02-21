package me.ddayo.customscript.client.gui.script.blocks

import me.ddayo.customscript.util.options.CalculableValue
import me.ddayo.customscript.util.options.Option
import me.ddayo.customscript.util.options.Option.Companion.string

class JavaScriptBlock: BlockBase() {
    var script = ""
    override fun parseContext(context: Option) {
        script = context["script"].string!!
    }

    override fun onEnter() {
        super.onEnter()
        CalculableValue(script).calculated
    }
}