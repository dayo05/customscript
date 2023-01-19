package me.ddayo.customscript.client.script

import jdk.nashorn.api.scripting.NashornScriptEngineFactory
import me.ddayo.customscript.client.gui.script.ScriptGui
import me.ddayo.customscript.client.gui.script.ScriptMode
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import javax.script.ScriptEngineManager

internal class ScriptGuiTest {
    @Test
    public fun parsingTest() {
        val js = NashornScriptEngineFactory().getScriptEngine("-scripting")
        js.eval("print('asdf')")
        js.put("asdf", 12)
        js.eval("print(asdf)")
        println(js.get("asdf"))
        println(js.eval("asdf") as Int)
        println(js.eval("0.1") as Float)
        js.eval("print(\"\${asdf + 12}\")")
    }
}