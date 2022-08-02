package me.ddayo.customscript.client.gui.font

object FontManager {
    private val fontList = emptyMap<String, Font>().toMutableMap()

    fun getFont(font: String): Font {
        if(fontList.containsKey(font)) return fontList[font]!!
        fontList[font] = Font(font)
        return fontList[font]!!
    }
}