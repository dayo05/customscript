package me.ddayo.customscript.server

import me.ddayo.customscript.CustomScript
import me.ddayo.customscript.network.HudNetworkHandler
import me.ddayo.customscript.network.OpenScriptNetworkHandler
import me.ddayo.customscript.network.QueueCloseGuiNetworkHandler
import me.ddayo.customscript.network.UpdateValueNetworkHandler
import me.ddayo.customscript.util.Validator
import me.ddayo.customscript.util.options.Option
import net.minecraft.command.CommandSource
import net.minecraft.entity.player.ServerPlayerEntity
import net.minecraft.util.text.StringTextComponent
import net.minecraftforge.fml.network.NetworkDirection
import net.minecraftforge.fml.server.ServerLifecycleHooks
import java.io.File
import java.util.UUID
import kotlin.math.pow
import kotlin.math.sqrt

object ServerDataHandler {
    internal val dynamicDataSubscribeState = emptyMap<UUID, MutableMap<String, Int>>().toMutableMap()
    fun subscribeDynamicValue(player: ServerPlayerEntity, vararg value: String) {
        value.forEach {
            if(dynamicDataSubscribeState[player.uniqueID]!!.containsKey(it))
                dynamicDataSubscribeState[player.uniqueID]!![it] = dynamicDataSubscribeState[player.uniqueID]!![it]!! + 1
            else dynamicDataSubscribeState[player.uniqueID]!![it] = 1
        }
    }
    fun unsubscribeDynamicValue(player: ServerPlayerEntity, vararg value: String) {
        value.forEach {
            if(dynamicDataSubscribeState[player.uniqueID]!!.containsKey(it))
                dynamicDataSubscribeState[player.uniqueID]!![it] = dynamicDataSubscribeState[player.uniqueID]!![it]!! - 1
            else dynamicDataSubscribeState[player.uniqueID]!![it] = 0
        }
    }

    internal data class ScriptStateData(val script: String, val validator: Validator<ServerPlayerEntity>)
    internal val playerScriptState = emptyMap<UUID, ScriptStateData>().toMutableMap()

    internal val dynamicData = emptyMap<UUID, MutableMap<String, String>>().toMutableMap()
    fun updateData(sender: CommandSource, player: ServerPlayerEntity, name: String, value: String) {
        if(!dynamicData.containsKey(player.uniqueID))
            dynamicData[player.uniqueID] = emptyMap<String, String>().toMutableMap()
        dynamicData[player.uniqueID]!![name] = value
        if(!ServerConfiguration.scriptValueUpdater.containsKey(name))
            sender.sendFeedback(StringTextComponent("Tried to update value $name which not registered on server configuration file. It will compactly "), true)
        else if((dynamicDataSubscribeState[player.uniqueID]!![name] ?: 0) > 0)
            CustomScript.network.sendTo(UpdateValueNetworkHandler(name, value), player.connection.networkManager, NetworkDirection.PLAY_TO_CLIENT)
    }

    internal val internalData = emptyMap<String, String>().toMutableMap()
    fun updateInternalData(sender: CommandSource, name: String, value: String) {
        internalData[name] = value

        if(!ServerConfiguration.scriptValueUpdater.containsKey(name))
            sender.sendFeedback(StringTextComponent("Tried to update value $name which not registered on server configuration file. It will compactly "), true)
        else dynamicDataSubscribeState.filter { (dynamicDataSubscribeState[it.key]!![name] ?: 0) > 0 }.forEach {
            ServerLifecycleHooks.getCurrentServer().playerList.getPlayerByUUID(it.key)?.run {
                CustomScript.network.sendTo(
                    UpdateValueNetworkHandler(name, value),
                    this.connection.networkManager,
                    NetworkDirection.PLAY_TO_CLIENT
                )
            }
        }
    }

    internal fun saveData() {
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

    internal fun loadData() {
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

    fun openScriptOnPlayers(script: String, begin: String, vararg players: ServerPlayerEntity) {
        val values = ServerConfiguration.scriptValueUpdater.filter { it.value.contains(script) }
        for (p in players) {
            val posX = p.posX
            val posY = p.posY
            val posZ = p.posZ
            playerScriptState[p.uniqueID] =
                ScriptStateData(script, object: Validator<ServerPlayerEntity> {
                    override fun validate(t: ServerPlayerEntity) = sqrt(
                        (posX - t.posX).pow(2) + (posY - t.posY).pow(2) + (posZ - t.posZ).pow(2)
                    ) <= 3
                })
            subscribeDynamicValue(p, *values.keys.toTypedArray())
            values.forEach {
                if (dynamicData[p.uniqueID]!!.contains(it.key))
                    CustomScript.network.sendTo(
                        UpdateValueNetworkHandler(
                            it.key,
                            dynamicData[p.uniqueID]!![it.key]!!
                        ), p.connection.networkManager, NetworkDirection.PLAY_TO_CLIENT
                    )
                else if (internalData.contains(it.key))
                    CustomScript.network.sendTo(
                        UpdateValueNetworkHandler(
                            it.key,
                            internalData[it.key]!!
                        ), p.connection.networkManager, NetworkDirection.PLAY_TO_CLIENT
                    )
            }
            CustomScript.network.sendTo(
                OpenScriptNetworkHandler(
                    script, begin
                ), p.connection.networkManager, NetworkDirection.PLAY_TO_CLIENT
            )
        }
    }

    fun openScriptOnPlayers(script: String, begin: String, vararg players: ServerPlayerEntity, validator: Validator<ServerPlayerEntity>) {
        val values = ServerConfiguration.scriptValueUpdater.filter { it.value.contains(script) }
        for (p in players) {
            playerScriptState[p.uniqueID] =
                ScriptStateData(script, validator)
            subscribeDynamicValue(p, *values.keys.toTypedArray())
            values.forEach {
                if (dynamicData[p.uniqueID]!!.contains(it.key))
                    CustomScript.network.sendTo(
                        UpdateValueNetworkHandler(
                            it.key,
                            dynamicData[p.uniqueID]!![it.key]!!
                        ), p.connection.networkManager, NetworkDirection.PLAY_TO_CLIENT
                    )
                else if (internalData.contains(it.key))
                    CustomScript.network.sendTo(
                        UpdateValueNetworkHandler(
                            it.key,
                            internalData[it.key]!!
                        ), p.connection.networkManager, NetworkDirection.PLAY_TO_CLIENT
                    )
            }
            CustomScript.network.sendTo(
                OpenScriptNetworkHandler(
                    script, begin
                ), p.connection.networkManager, NetworkDirection.PLAY_TO_CLIENT
            )
        }
    }

    fun closeGui(vararg players: ServerPlayerEntity) {
        players.forEach { player ->
            val values =
                ServerConfiguration.scriptValueUpdater.filter { it.value.contains(playerScriptState[player.uniqueID]!!.script) }
            playerScriptState.remove(player.uniqueID)
            unsubscribeDynamicValue(player, *values.keys.toTypedArray())
            CustomScript.network.sendTo(
                QueueCloseGuiNetworkHandler(),
                player.connection.netManager,
                NetworkDirection.PLAY_TO_CLIENT
            )
        }
    }

    internal val hudState = emptyMap<UUID, MutableSet<String>>().toMutableMap()
    fun enableHud(script: String, vararg players: ServerPlayerEntity) {
        val values = ServerConfiguration.scriptValueUpdater.filter { it.value.contains(script) }
        for (p in players) {
            if (hudState[p.uniqueID]!!.contains(script)) continue
            values.forEach {
                if (dynamicData[p.uniqueID]!!.contains(it.key))
                    CustomScript.network.sendTo(
                        UpdateValueNetworkHandler(
                            it.key,
                            dynamicData[p.uniqueID]!![it.key]!!
                        ), p.connection.networkManager, NetworkDirection.PLAY_TO_CLIENT
                    )
                else if (internalData.contains(it.key))
                    CustomScript.network.sendTo(
                        UpdateValueNetworkHandler(
                            it.key,
                            internalData[it.key]!!
                        ), p.connection.networkManager, NetworkDirection.PLAY_TO_CLIENT
                    )
            }
            subscribeDynamicValue(p, *values.keys.toTypedArray())
            hudState[p.uniqueID]!!.add(script)
            CustomScript.network.sendTo(
                HudNetworkHandler(script, true),
                p.connection.networkManager,
                NetworkDirection.PLAY_TO_CLIENT
            )
        }
    }

    fun disableHud(script: String, vararg players: ServerPlayerEntity) {
        val values = ServerConfiguration.scriptValueUpdater.filter { it.value.contains(script) }
        for (p in players) {
            if (!hudState[p.uniqueID]!!.contains(script)) continue
            unsubscribeDynamicValue(p, *values.keys.toTypedArray())
            hudState[p.uniqueID]!!.remove(script)
            CustomScript.network.sendTo(
                HudNetworkHandler(script, false),
                p.connection.networkManager,
                NetworkDirection.PLAY_TO_CLIENT
            )
        }
    }
}