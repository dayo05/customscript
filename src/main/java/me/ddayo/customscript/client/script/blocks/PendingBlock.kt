package me.ddayo.customscript.client.script.blocks

import me.ddayo.customscript.client.script.ScriptGui

abstract class PendingBlock: BlockBase() {
    open fun validateKeyInput(gui: ScriptGui, keyCode: Int, scanCode: Int, modifier: Int) = false
    open fun validateMouseInput(gui: ScriptGui, mouseX: Double, mouseY: Double, mouseButton: Int) = false
}