package me.ddayo.customscript.client.gui.script.blocks

interface IRendererBlock {
    fun render()
    fun onRemovedFromQueue() {}
    val renderParse: RenderParse
}

enum class RenderParse {
    Pre, Main, Post
}