package me.ddayo.customscript.server

import me.ddayo.customscript.util.options.Option
import me.ddayo.customscript.util.options.Option.Companion.string
import org.apache.logging.log4j.LogManager
import java.io.File

object ServerConfiguration {
    private val customScriptServerOption = File("customscript.option")
    private val serverOption = if(customScriptServerOption.exists()) Option.readOption(customScriptServerOption.readText()) else Option.createRootOption()

    var allowServerSideCommand = false
        private set
    var scriptValueUpdater = emptyMap<String, MutableList<String>>().toMutableMap()
        private set

    fun loadConfig() {
        allowServerSideCommand = serverOption["Allow-server-side-command"].string == "True"
        serverOption["Script"].forEach {
            it["Value"].forEach { v ->
                if(!scriptValueUpdater.containsKey(v.value))
                    scriptValueUpdater[v.value] = emptyList<String>().toMutableList()
                scriptValueUpdater[v.value]!!.add(it.value)
            }
        }

        LogManager.getLogger().info("Server configuration load finished")
    }

    init {
        loadConfig()
    }
}