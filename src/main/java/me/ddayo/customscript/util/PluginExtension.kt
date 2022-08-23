package me.ddayo.customscript.util

import me.ddayo.customscript.server.ServerDataHandler
import net.minecraft.entity.player.ServerPlayerEntity
import net.minecraftforge.fml.server.ServerLifecycleHooks
import java.util.UUID


object PluginExtension {
    private fun nameToServerEntityPlayer(name: String) =
        ServerLifecycleHooks.getCurrentServer().playerList.getPlayerByUsername(name)!!
    private fun uuidToServerEntityPlayer(uuid: UUID) =
        ServerLifecycleHooks.getCurrentServer().playerList.getPlayerByUUID(uuid)!!

    fun subscribeDynamicValue(player: String, vararg value: String) = ServerDataHandler.subscribeDynamicValue(
        nameToServerEntityPlayer(player), *value
    )
    fun subscribeDynamicValue(player: UUID, vararg value: String) = ServerDataHandler.subscribeDynamicValue(
        uuidToServerEntityPlayer(player), *value
    )

    fun unsubscribeDynamicValue(player: String, vararg value: String) = ServerDataHandler.unsubscribeDynamicValue(
        nameToServerEntityPlayer(player), *value
    )
    fun unsubscribeDynamicValue(player: UUID, vararg value: String) = ServerDataHandler.unsubscribeDynamicValue(
        uuidToServerEntityPlayer(player), *value
    )

    fun updateData(sender: String, player: String, name: String, value: String) = ServerDataHandler.updateData(
        nameToServerEntityPlayer(sender).commandSource, nameToServerEntityPlayer(player), name, value
    )
    fun updateData(sender: UUID, player: String, name: String, value: String) = ServerDataHandler.updateData(
        uuidToServerEntityPlayer(sender).commandSource, nameToServerEntityPlayer(player), name, value
    )

    fun updateInternalData(sender: String, name: String, value: String) = ServerDataHandler.updateInternalData(
        nameToServerEntityPlayer(sender).commandSource, name, value
    )
    fun updateInternalData(sender: UUID, name: String, value: String) = ServerDataHandler.updateInternalData(
        uuidToServerEntityPlayer(sender).commandSource, name, value
    )

    fun openScript(script: String, begin: String, vararg players: String) = ServerDataHandler.openScriptOnPlayers(
        script,
        begin,
        *players.map { nameToServerEntityPlayer(it) }.toTypedArray()
    )
    fun openScript(script: String, begin: String, vararg players: UUID) = ServerDataHandler.openScriptOnPlayers(
        script,
        begin,
        *players.map { uuidToServerEntityPlayer(it) }.toTypedArray()
    )

    fun openScript(script: String, begin: String, vararg players: String, validator: Validator<String>) =
        ServerDataHandler.openScriptOnPlayers(
            script,
            begin,
            *players.map { nameToServerEntityPlayer(it) }.toTypedArray(),
            validator = object : Validator<ServerPlayerEntity> {
                override fun validate(t: ServerPlayerEntity) = validator.validate(t.name.string)
            })
    fun openScript(script: String, begin: String, vararg players: UUID, validator: Validator<String>) =
        ServerDataHandler.openScriptOnPlayers(
            script,
            begin,
            *players.map { uuidToServerEntityPlayer(it) }.toTypedArray(),
            validator = object : Validator<ServerPlayerEntity> {
                override fun validate(t: ServerPlayerEntity) = validator.validate(t.name.string)
            })

    fun closeScript(vararg players: String) =
        ServerDataHandler.closeGui(*players.map { nameToServerEntityPlayer(it) }.toTypedArray())
    fun closeScript(vararg players: UUID) =
        ServerDataHandler.closeGui(*players.map { uuidToServerEntityPlayer(it) }.toTypedArray())

    fun enableHud(script: String, vararg players: String) =
        ServerDataHandler.enableHud(script, *players.map { nameToServerEntityPlayer(it) }.toTypedArray())
    fun enableHud(script: String, vararg players: UUID) =
        ServerDataHandler.enableHud(script, *players.map { uuidToServerEntityPlayer(it) }.toTypedArray())

    fun disableHud(script: String, vararg players: String) =
        ServerDataHandler.disableHud(script, *players.map { nameToServerEntityPlayer(it) }.toTypedArray())
    fun disableHud(script: String, vararg players: UUID) =
        ServerDataHandler.disableHud(script, *players.map { uuidToServerEntityPlayer(it) }.toTypedArray())
}