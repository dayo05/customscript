package me.ddayo.customscript.client.gui.script.blocks

import me.ddayo.customscript.util.js.CalculableValueManager
import me.ddayo.customscript.util.js.ICalculableHolder
import me.ddayo.customscript.util.js.StringCalculable
import me.ddayo.customscript.util.options.Option
import me.ddayo.customscript.util.options.Option.Companion.string

class ModifyVariableBlock: BlockBase(), ICalculableHolder {
    lateinit var name: StringCalculable
    lateinit var value: StringCalculable
    override fun parseContext(context: Option) {
        name = StringCalculable(context["Name"].string!!)
        value = StringCalculable(context["Value"].string!!)
    }

    override fun onEnter() {
        super.onEnter()
        CalculableValueManager.setValue(name.get, value.get)
    }

    override val calculable by lazy { listOf(name, value) }
}