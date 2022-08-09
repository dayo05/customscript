package me.ddayo.customscript.server

import me.ddayo.customscript.CustomScript
import me.ddayo.customscript.network.UpdateValueNetworkHandler
import me.ddayo.customscript.util.options.Option
import net.minecraft.command.CommandSource
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.ServerPlayerEntity
import net.minecraft.util.text.StringTextComponent
import net.minecraftforge.fml.network.NetworkDirection
import net.minecraftforge.fml.server.ServerLifecycleHooks
import java.io.File
import java.util.UUID
import kotlin.math.pow
import kotlin.math.sqrt

object ServerDataHandler {
    data class ScriptStateData(val script: String, val posX: Double, val posY: Double, val posZ: Double)
    val playerScriptState = emptyMap<UUID, ScriptStateData>().toMutableMap()

    fun getDistanceByLastOpen(player: PlayerEntity): Double {
        val state = playerScriptState[player.uniqueID]!!
        return sqrt((state.posX - player.posX).pow(2) + (state.posY - player.posY).pow(2) + (state.posZ - player.posZ).pow(2))
    }

    val dynamicData = emptyMap<UUID, MutableMap<String, String>>().toMutableMap()
    fun updateData(player: ServerPlayerEntity, name: String, value: String) {
        if(!dynamicData.containsKey(player.uniqueID))
            dynamicData[player.uniqueID] = emptyMap<String, String>().toMutableMap()
        dynamicData[player.uniqueID]!![name] = value
        if(!ServerConfiguration.scriptValueUpdater.containsKey(name))
            player.sendMessage(StringTextComponent("Tried to update value $name which not registered on server configuration file. It will compactly "), player.uniqueID)
        else if(playerScriptState.containsKey(player.uniqueID) && ServerConfiguration.scriptValueUpdater[name]!!.contains(playerScriptState[player.uniqueID]!!.script))
            CustomScript.network.sendTo(UpdateValueNetworkHandler(name, value), player.connection.networkManager, NetworkDirection.PLAY_TO_CLIENT)
    }

    val internalData = emptyMap<String, String>().toMutableMap()
    fun updateInternalData(sender: CommandSource, name: String, value: String) {
        internalData[name] = value

        if(!ServerConfiguration.scriptValueUpdater.containsKey(name))
            sender.sendFeedback(StringTextComponent("Tried to update value $name which not registered on server configuration file. It will compactly "), true)
        else playerScriptState.filter { ServerConfiguration.scriptValueUpdater[name]!!.contains(it.value.script)}.forEach {
            val pl = ServerLifecycleHooks.getCurrentServer().playerList.getPlayerByUUID(it.key)
            if (pl != null)
                CustomScript.network.sendTo(UpdateValueNetworkHandler(name, value), pl.connection.networkManager, NetworkDirection.PLAY_TO_CLIENT)
        }
    }

    fun saveData() {
        File("csData.dat").writeText(
            listOf(
                *dynamicData.keys.map { k ->
                    Option("dynamicData", k).apply {
                        dynamicData[k]!!.forEach {
                            append(it.key, it.value)
                        }
                    }.str()
                }.toTypedArray(),
                Option("internalData", "").apply {
                    internalData.forEach { append(it.key, it.value) }
                }.str()
            ).joinToString("")
        )
    }

    fun loadData() {
        dynamicData.clear()
        internalData.clear()

        val opt = Option.readOption(File("csData.dat")
            .run {
                if(exists())
                    readText()
                else ""
            })
        opt["dynamicData"].onEach {
            dynamicData[UUID.fromString(it.value)] = mutableMapOf(*it.subOptions.map { o -> Pair(o.key, o.value) }.toTypedArray())
        }
        internalData.putAll(opt["internalData"].run {
            if(isEmpty()) emptyList()
            else first().subOptions.map { Pair(it.key, it.value) }
        })
    }
}