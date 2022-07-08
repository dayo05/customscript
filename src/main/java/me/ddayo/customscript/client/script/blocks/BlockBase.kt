package me.ddayo.customscript.client.script.blocks

import me.ddayo.customscript.client.script.ScriptGui
import me.ddayo.customscript.util.options.CompileError
import me.ddayo.customscript.util.options.Option
import me.ddayo.customscript.util.options.Option.Companion.int
import me.ddayo.customscript.util.options.Option.Companion.string
import org.apache.logging.log4j.LogManager

abstract class BlockBase {
    companion object {
        private val blocks = emptyMap<String, Pair<String, Class<out BlockBase>>>().toMutableMap()
        private val logger = LogManager.getLogger()

        private fun <T> registerBlock(name: String, contextName: String, cls: Class<T>) where T: BlockBase{
            blocks[name] = Pair(contextName, cls)
        }

        init {
            registerBlock("BeginBlock", "BeginBlockContext", BeginBlock::class.java)
            registerBlock("TextBlock", "TextContext", TextBlock::class.java)
            registerBlock("ButtonBlock", "ButtonContext", ButtonBlock::class.java)
            registerBlock("ChangeBackground", "ChangeBackgroundContext", ChangeBackgroundBlock::class.java)
        }

        fun createBlock(name: String, opt: Option, base: ScriptGui): BlockBase {
            if(!blocks.containsKey(name))
                throw CompileError("Not supported block: $name")
            val block = blocks[name]!!.second.constructors.first().newInstance() as BlockBase
            block.base = base
            if(opt["Context"].string != blocks[name]!!.first) throw IllegalArgumentException("Context type ${opt["Context"].first()} and declared context type ${blocks[name]!!.first} are different")
            block.ns = opt["NS"].int
            block.parseContext(opt["Context"].first())
            return block
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