package me.ddayo.customscript.client.gui.script.blocks

import me.ddayo.customscript.client.gui.script.ScriptGui

abstract class PendingBlock: BlockBase() {
    open fun validateKeyInput(gui: ScriptGui, keyCode: Int, scanCode: Int, modifier: Int) = PendingResult.Deny
    open fun validateMouseInput(gui: ScriptGui, mouseX: Double, mouseY: Double, mouseButton: Int) = PendingResult.Deny

    open fun onExitPending() {}

    open fun tick() = PendingResult.Deny

    enum class PendingResult {
        Deny, Pass, Force
    }
}