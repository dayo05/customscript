package me.ddayo.customscript.client

import me.ddayo.customscript.client.gui.script.ScriptGui

object ClientDataHandler {
    internal val dynamicValues = emptyMap<String, String>().toMutableMap()
    fun decodeDynamicValue(k: String): String {
            var rtn = k
            dynamicValues.forEach {
                rtn = rtn.replace("{${it.key}}", it.value)
            }
            return rtn
        }

    internal val enabledHud = emptyMap<String, ScriptGui>().toMutableMap()
}
