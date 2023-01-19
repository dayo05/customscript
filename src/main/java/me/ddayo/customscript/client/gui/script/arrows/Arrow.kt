package me.ddayo.customscript.client.gui.script.arrows

import me.ddayo.customscript.util.options.Option
import me.ddayo.customscript.util.options.Option.Companion.int
import me.ddayo.customscript.util.options.Option.Companion.string

class Arrow {
    companion object {
        val arrows = emptyMap<String, Pair<String, Class<out Arrow>>>().toMutableMap()
        fun<T> registerArrow(name: String, contextName: String, arrow: Class<T>) where T: Arrow {
            arrows[name] = Pair(contextName, arrow)
        }

        fun createArrow(name: String, opt: Option): Arrow {
            val arrow = arrows[name]!!.second.newInstance()
            if(opt["Context"].string != arrows[name]!!.first) throw IllegalArgumentException("Context type ${opt["Context"].first()} and declared context type ${arrows[name]!!.first} are different")
            arrow.parseContext(opt["Context"].first())
            return arrow
        }

        init {
            registerArrow("Arrow", "ArrowContext", Arrow::class.java)
        }
    }

    var from = 0
        private set
    var to = 0
        private set

    fun parseContext(context: Option) {
        from = context["From"].int!!
        to = context["To"].int!!
    }
}