package me.ddayo.customscript.client.gui.script.blocks

import me.ddayo.customscript.client.gui.script.ScriptGui

interface IRendererBlock {
    fun render()
    fun onRemovedFromQueue() {}
    val renderParse: ScriptGui.RenderParse
}

