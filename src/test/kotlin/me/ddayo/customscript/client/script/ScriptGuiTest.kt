package me.ddayo.customscript.client.script

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

internal class ScriptGuiTest {
    @Test
    public fun parsingTest() {
        val instance = ScriptGui("/home/dayo/UnityProjects/Script-Maker/test.sc", "default")
        instance.cancelPending(1)
        instance.cancelPending(3)
        instance.cancelPending(5)
    }
}