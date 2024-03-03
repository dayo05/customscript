package me.ddayo.customscript.client

import me.ddayo.customscript.client.gui.script.ScriptGui
import me.ddayo.customscript.util.js.CalculableValueManager

object ClientDataHandler {

    fun updateDynamicValue(key: String, value: String) = CalculableValueManager.setValue(key, value)

    internal val enabledHud = emptyMap<String, ScriptGui>().toMutableMap()
}
