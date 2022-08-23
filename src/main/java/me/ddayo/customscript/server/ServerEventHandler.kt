package me.ddayo.customscript.server

import me.ddayo.customscript.CustomScript
import me.ddayo.customscript.server.commands.CommandHandler
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.ServerPlayerEntity
import net.minecraftforge.event.RegisterCommandsEvent
import net.minecraftforge.event.TickEvent
import net.minecraftforge.event.entity.EntityJoinWorldEvent
import net.minecraftforge.eventbus.api.EventPriority
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.LogicalSide
import net.minecraftforge.fml.event.server.FMLServerStartingEvent
import net.minecraftforge.fml.event.server.FMLServerStoppingEvent

object ServerEventHandler {
    @SubscribeEvent
    fun serverInit(event: FMLServerStartingEvent) {
        // Initialize server configurations
        ServerConfiguration
        ServerDataHandler.loadData()
    }

    @SubscribeEvent
    fun serverClose(event: FMLServerStoppingEvent) {
        ServerDataHandler.saveData()
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    fun registerCommandEvent(event: RegisterCommandsEvent)
            = CommandHandler.register(event.dispatcher)

    @SubscribeEvent
    fun playerTick(event: TickEvent.PlayerTickEvent) {
        if(event.side == LogicalSide.CLIENT) return
        if(ServerDataHandler.playerScriptState.containsKey(event.player.uniqueID) && !ServerDataHandler.playerScriptState[event.player.uniqueID]!!.validator.validate(event.player as ServerPlayerEntity)) {
            ServerDataHandler.closeGui(event.player as ServerPlayerEntity)
        }
    }

    @SubscribeEvent
    fun onPlayerJoin(event: EntityJoinWorldEvent) {
        if(CustomScript.isClient) return
        if(event.entity is PlayerEntity) {
            if (!ServerDataHandler.dynamicData.containsKey(event.entity.uniqueID)) {
                ServerDataHandler.dynamicData[event.entity.uniqueID] = emptyMap<String, String>().toMutableMap()
            }
            if(!ServerDataHandler.hudState.containsKey(event.entity.uniqueID))
                ServerDataHandler.hudState[event.entity.uniqueID] = emptySet<String>().toMutableSet()
            if(!ServerDataHandler.dynamicDataSubscribeState.containsKey(event.entity.uniqueID))
                ServerDataHandler.dynamicDataSubscribeState[event.entity.uniqueID] = emptyMap<String, Int>().toMutableMap()
        }
    }
}