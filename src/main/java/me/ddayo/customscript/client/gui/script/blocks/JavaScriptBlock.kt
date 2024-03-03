package me.ddayo.customscript.client.gui.script.blocks

import me.ddayo.customscript.util.js.CalculableValueManager
import me.ddayo.customscript.util.options.Option
import me.ddayo.customscript.util.options.Option.Companion.string

class JavaScriptBlock: BlockBase() {
    var script = ""
    override fun parseContext(context: Option) {
        script = context["script"].string!!
    }

    override fun onEnter() {
        super.onEnter()
        CalculableValueManager.execute(script)
    }
}