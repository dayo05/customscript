package me.ddayo.customscript.client.gui.script.blocks

import me.ddayo.customscript.client.gui.RenderUtil
import me.ddayo.customscript.client.gui.script.ScriptGui

abstract class ScriptRenderer {
    abstract fun RenderUtil.renderInternal()
    open fun onRemovedFromQueue() {}
    abstract val renderParse: ScriptGui.RenderParse

    fun render(renderer: RenderUtil) {
        renderer.renderInternal()
    }

    abstract val isLoading: Boolean
}

