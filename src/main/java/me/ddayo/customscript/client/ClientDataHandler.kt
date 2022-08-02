package me.ddayo.customscript.client

object ClientDataHandler {
    public val dynamicValues = emptyMap<String, String>().toMutableMap()
    fun decodeDynamicValue(k: String): String {
            var rtn = k
            dynamicValues.forEach {
                rtn = rtn.replace("{${it.key}}", it.value)
            }
            return rtn
        }
}