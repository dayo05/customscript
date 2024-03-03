package me.ddayo.customscript.client.gui.script.blocks

import me.ddayo.customscript.client.gui.script.ScriptGui
import me.ddayo.customscript.util.js.AbstractCalculable
import me.ddayo.customscript.util.js.DoubleCalculable
import me.ddayo.customscript.util.js.ICalculableHolder
import me.ddayo.customscript.util.options.CompileError
import me.ddayo.customscript.util.options.Option
import me.ddayo.customscript.util.options.Option.Companion.double
import me.ddayo.customscript.util.options.Option.Companion.string

class DelayBlock: PendingBlock(), ICalculableHolder {
    private var type = 0
    private var enterTime = -1L
    private var key = ""
    private lateinit var timeLimit: DoubleCalculable
    override fun parseContext(context: Option) {
        type = when (context["Type"].string) {
            "Key" -> {
                key = context["Key"].string!!
                0
            }

            "Time" -> {
                timeLimit = DoubleCalculable(context["Time"].string!!)
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
        1 -> if (System.currentTimeMillis() - enterTime > timeLimit.get * 1000L) PendingResult.Pass else PendingResult.Deny
        else -> PendingResult.Deny
    }

    override val calculable by lazy { if(::timeLimit.isInitialized) listOf(timeLimit) else listOf() }
}