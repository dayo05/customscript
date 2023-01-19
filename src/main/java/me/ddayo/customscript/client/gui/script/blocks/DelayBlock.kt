package me.ddayo.customscript.client.gui.script.blocks

import me.ddayo.customscript.client.gui.script.ScriptGui
import me.ddayo.customscript.util.options.CalculableValue
import me.ddayo.customscript.util.options.CompileError
import me.ddayo.customscript.util.options.Option
import me.ddayo.customscript.util.options.Option.Companion.double
import me.ddayo.customscript.util.options.Option.Companion.string

class DelayBlock: PendingBlock(), ISubscribeDynamicValueBlock {
    private var type = 0
    private var enterTime = -1L
    private var key = ""
    private lateinit var timeLimit: CalculableValue
    override fun parseContext(context: Option) {
        type = when (context["Type"].string) {
            "Key" -> {
                key = context["Key"].string!!
                0
            }

            "Time" -> {
                timeLimit = CalculableValue("(${context["Time"].string!!}) * 1000")
                1
            }

            else -> throw CompileError("Not supported value on DelayBlock")
        }
    }

    override fun validateKeyInput(gui: ScriptGui, keyCode: Int, scanCode: Int, modifier: Int) = when (type) {
        0 -> if (key.last() == '^' && key.all {
                if (it in '0'..'9') base.numberState[it.digitToInt()]
                else base.alphabetState[it.code - 'A'.code]
            }) PendingResult.Pass else PendingResult.Deny

        1 -> PendingResult.Deny
        else -> PendingResult.Deny
    }

    override fun validateMouseInput(gui: ScriptGui, mouseX: Double, mouseY: Double, mouseButton: Int) = when (type) {
        0 -> if (mouseButton.digitToChar() == key.last() && key.all {
                if (it in '0'..'9') base.numberState[it.digitToInt()]
                else base.alphabetState[it.code - 'A'.code]
            }) PendingResult.Pass else PendingResult.Deny

        1 -> PendingResult.Deny
        else -> PendingResult.Deny
    }

    override fun onEnter() {
        super.onEnter()
        enterTime = System.currentTimeMillis()
    }

    override fun tick() = when (type) {
        0 -> PendingResult.Deny
        1 -> if (System.currentTimeMillis() - enterTime > timeLimit.long) PendingResult.Pass else PendingResult.Deny
        else -> PendingResult.Deny
    }

    override fun onUpdateValue() {
        super.onUpdateValue()
        timeLimit.updateValue()
    }
}