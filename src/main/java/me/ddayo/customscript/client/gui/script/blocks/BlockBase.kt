package me.ddayo.customscript.client.gui.script.blocks

import me.ddayo.customscript.client.gui.script.ScriptGui
import me.ddayo.customscript.util.options.CompileError
import me.ddayo.customscript.util.options.Option
import me.ddayo.customscript.util.options.Option.Companion.int
import me.ddayo.customscript.util.options.Option.Companion.string
import org.apache.logging.log4j.LogManager

abstract class BlockBase {
    companion object {
        private val blocks = emptyMap<String, Pair<String, Class<out BlockBase>>>().toMutableMap()

        fun <T> registerBlock(name: String, contextName: String, cls: Class<T>) where T: BlockBase {
            blocks[name] = Pair(contextName, cls)
        }

        init {
            registerBlock("BeginBlock", "BeginBlockContext", BeginBlock::class.java)
            registerBlock("TextBlock", "TextContext", TextBlock::class.java)
            registerBlock("ButtonBlock", "ButtonContext", ButtonBlock::class.java)
            registerBlock("ChangeBackgroundBlock", "ChangeBackgroundContext", ChangeBackgroundBlock::class.java)
            registerBlock("RunCommandBlock", "RunCommandContext", RunCommandBlock::class.java)
            registerBlock("DelayBlock", "DelayContext", DelayBlock::class.java)
        }

        fun createBlock(name: String, opt: Option, base: ScriptGui): BlockBase {
            if(!blocks.containsKey(name))
                throw CompileError("Not supported block: $name")
            return (blocks[name]!!.second.constructors.first().newInstance() as BlockBase).apply {
                this.base = base
                if (opt["Context"].string != blocks[name]!!.first) throw IllegalArgumentException("Context type ${opt["Context"].first()} and declared context type ${blocks[name]!!.first} are different")
                this.ns = opt["NS"].int!!
                this.parseContext(opt["Context"].first())
            }
        }
    }

    protected lateinit var base: ScriptGui
        private set

    protected val logger = LogManager.getLogger()

    abstract fun parseContext(context: Option)
    public var ns = 0
        private set

    open fun onEnter() {}
}