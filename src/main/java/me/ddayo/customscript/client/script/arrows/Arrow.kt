package me.ddayo.customscript.client.script.arrows

import me.ddayo.customscript.client.script.ScriptGui
import me.ddayo.customscript.util.options.Option
import me.ddayo.customscript.util.options.Option.Companion.string
import org.lwjgl.glfw.GLFW

class Arrow: ArrowBase() {
    var mouse = ' '
        private set
    var key = ""
        private set

    override fun parseContext(context: Option) {
        super.parseContext(context)
        val a = context["Key"].string
        mouse = a[0]
        key = a.substring(1)
    }

    private val ignoreKeyboard get() = key == "default"

    override fun onKeyboardInput(gui: ScriptGui, keyCode: Int, scanCode: Int, modifier: Int): Boolean {
        if(mouse != 'p') return false
        return keyCode == key[0].code
    }

    override fun onMouseInput(gui: ScriptGui, mouseX: Double, mouseY: Double, mouseButton: Int) = when (key[0]) {
        'p' -> false
        '0' -> {
            if(mouseButton != GLFW.GLFW_MOUSE_BUTTON_LEFT) false
            else if(ignoreKeyboard) true
            else if(key[0] in '0'..'9')
                gui.numberState[key[0].code - '0'.code]
            else if(key[0] in 'a'..'z')
                gui.alphabetState[key[0].code - 'a'.code]
            else false
        }
        '1' -> {
            if(mouseButton != GLFW.GLFW_MOUSE_BUTTON_RIGHT) false
            else if(ignoreKeyboard) true
            else if(key[0] in '0'..'9')
                gui.numberState[key[0].code - '0'.code]
            else if(key[0] in 'a'..'z')
                gui.alphabetState[key[0].code - 'a'.code]
            else false
        }
        '2' -> {
            if(mouseButton != GLFW.GLFW_MOUSE_BUTTON_MIDDLE) false
            else if(ignoreKeyboard) true
            else if(key[0] in '0'..'9')
                gui.numberState[key[0].code - '0'.code]
            else if(key[0] in 'a'..'z')
                gui.alphabetState[key[0].code - 'a'.code]
            else false
        }
        else -> false
    }
}
