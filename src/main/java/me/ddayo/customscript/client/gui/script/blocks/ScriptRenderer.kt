package me.ddayo.customscript.client.gui.script.blocks

import me.ddayo.customscript.client.gui.script.ScriptGui

abstract class ScriptRenderer: ISubscribeDynamicValueBlock {
    abstract fun render()
    open fun onRemovedFromQueue() {}
    abstract val renderParse: ScriptGui.RenderParse
}

