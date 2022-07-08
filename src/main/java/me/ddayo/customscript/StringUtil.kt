package me.ddayo.customscript

import me.ddayo.customscript.network.StringDataNetworkHandler
import net.minecraftforge.fml.network.NetworkDirection
import net.minecraftforge.fml.server.ServerLifecycleHooks

object StringUtil {
    private val data = emptyMap<String, String>().toMutableMap()

    fun iterate() = data.toMap()

    fun updateValue(key: String, value: String) {
        data[key] = value
        if(!CustomScript.isClient)
            for(player in ServerLifecycleHooks.getCurrentServer().playerList.players)
                CustomScript.network.sendTo(StringDataNetworkHandler(key, value), player.connection.netManager, NetworkDirection.PLAY_TO_CLIENT)
    }

    fun getValueIn(key: String) = data[key]
}