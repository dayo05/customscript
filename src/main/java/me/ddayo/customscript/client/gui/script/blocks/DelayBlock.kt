package me.ddayo.customscript.client.gui.script.blocks

import me.ddayo.customscript.client.gui.script.ScriptGui
import me.ddayo.customscript.util.options.CompileError
import me.ddayo.customscript.util.options.Option
import me.ddayo.customscript.util.options.Option.Companion.string

class DelayBlock: PendingBlock() {
    var type = 0
    private var enterTime = -1L
    override fun parseContext(context: Option) {
        type = when(context["Type"].string) {
            "Arrow" -> 0
            "Time" -> throw CompileError("Not supported value on DelayBlock") //TODO
            else -> throw CompileError("Not supported value on DelayBlock")
        }
    }

    override fun validateKeyInput(gui: ScriptGui, keyCode: Int, scanCode: Int, modifier: Int) = when(type) {
        0 -> PendingResult.Pass
        1 -> PendingResult.Deny
        else -> PendingResult.Deny
    }

    override fun validateMouseInput(gui: ScriptGui, mouseX: Double, mouseY: Double, mouseButton: Int) = when(type) {
        0 -> PendingResult.Pass
        1 -> PendingResult.Deny
        else -> PendingResult.Deny
    }

    override fun onEnter() {
        super.onEnter()
        enterTime = System.currentTimeMillis()
    }

    override fun tick() = when(type) {
        0 -> PendingResult.Deny
        1 -> PendingResult.Pass
        else -> PendingResult.Deny
    }
}