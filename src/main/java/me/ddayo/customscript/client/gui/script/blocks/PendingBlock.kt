package me.ddayo.customscript.client.gui.script.blocks

import me.ddayo.customscript.client.gui.script.ScriptGui
import me.ddayo.customscript.util.js.CalculableValueManager
import me.ddayo.customscript.util.js.ICalculableHolder

abstract class PendingBlock: BlockBase() {
    open fun validateKeyInput(gui: ScriptGui, keyCode: Int, scanCode: Int, modifier: Int) = PendingResult.Deny
    open fun validateMouseInput(gui: ScriptGui, mouseX: Double, mouseY: Double, mouseButton: Int) = PendingResult.Deny

    open fun onExitPending() {
        if(this is ICalculableHolder)
            CalculableValueManager.dynamicValueHolder.remove(this)
    }

    override fun onEnter() {
        super.onEnter()
        if(this is ICalculableHolder)
            CalculableValueManager.dynamicValueHolder.add(this)
    }

    open fun tick() = PendingResult.Deny

    enum class PendingResult {
        Deny, Pass
    }
}