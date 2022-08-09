package me.ddayo.customscript

import me.ddayo.customscript.CustomScript.MOD_ID
import me.ddayo.customscript.client.ClientEventHandler
import me.ddayo.customscript.commands.CommandHandler
import me.ddayo.customscript.network.*
import me.ddayo.customscript.server.ServerConfiguration
import me.ddayo.customscript.server.ServerDataHandler
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.ServerPlayerEntity
import net.minecraft.util.ResourceLocation
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.event.RegisterCommandsEvent
import net.minecraftforge.event.TickEvent.PlayerTickEvent
import net.minecraftforge.event.entity.EntityJoinWorldEvent
import net.minecraftforge.eventbus.api.EventPriority
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.LogicalSide
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.event.server.FMLServerStartingEvent
import net.minecraftforge.fml.event.server.FMLServerStoppingEvent
import net.minecraftforge.fml.network.NetworkDirection
import net.minecraftforge.fml.network.NetworkRegistry
import net.minecraftforge.fml.network.simple.SimpleChannel
import org.apache.logging.log4j.LogManager
import java.util.*

@Mod(MOD_ID)
class CustomScriptMod {
    init {
        FMLJavaModLoadingContext.get().modEventBus.register(this)
        MinecraftForge.EVENT_BUS.register(CustomScript)
    }

    @SubscribeEvent
    fun clientInit(event: FMLClientSetupEvent) {
        CustomScript.isClient = true
        MinecraftForge.EVENT_BUS.register(ClientEventHandler)
        ClientEventHandler.initialize()
    }

    @SubscribeEvent
    @Suppress("INACCESSIBLE_TYPE")
    fun init(event: FMLCommonSetupEvent) {
        CustomScript.isTest = false

        CustomScript.network = NetworkRegistry.newSimpleChannel(
                ResourceLocation(MOD_ID, "networkchannel"),
                { CustomScript.VERSION },
                CustomScript.VERSION::equals,
                CustomScript.VERSION::equals
        )
        CustomScript.network.registerMessage(
                11, OpenScriptNetworkHandler::class.java,
                OpenScriptNetworkHandler::encode, OpenScriptNetworkHandler.Companion::decode,
                OpenScriptNetworkHandler.Companion::onMessageReceived, Optional.of(NetworkDirection.PLAY_TO_CLIENT)
        )
        CustomScript.network.registerMessage(
                12, ServerSideCommandNetworkHandler::class.java,
                ServerSideCommandNetworkHandler::encode, ServerSideCommandNetworkHandler.Companion::decode,
                ServerSideCommandNetworkHandler.Companion::onMessageReceived, Optional.of(NetworkDirection.PLAY_TO_SERVER)
        )
        CustomScript.network.registerMessage(
                13, ClearCacheNetworkHandler::class.java,
                ClearCacheNetworkHandler::encode, ClearCacheNetworkHandler.Companion::decode,
                ClearCacheNetworkHandler.Companion::onMessageReceived, Optional.of(NetworkDirection.PLAY_TO_CLIENT)
        )
        CustomScript.network.registerMessage(
                14, QueueCloseGuiNetworkHandler::class.java,
                QueueCloseGuiNetworkHandler::encode, QueueCloseGuiNetworkHandler.Companion::decode,
                QueueCloseGuiNetworkHandler.Companion::onMessageReceived, Optional.of(NetworkDirection.PLAY_TO_CLIENT)
        )
        CustomScript.network.registerMessage(
                15, CloseGuiNetworkHandler::class.java,
                CloseGuiNetworkHandler::encode, CloseGuiNetworkHandler.Companion::decode,
                CloseGuiNetworkHandler.Companion::onMessageReceived, Optional.of(NetworkDirection.PLAY_TO_SERVER)
        )
        CustomScript.network.registerMessage(
                16, UpdateValueNetworkHandler::class.java,
                UpdateValueNetworkHandler::encode, UpdateValueNetworkHandler.Companion::decode,
                UpdateValueNetworkHandler.Companion::onMessageReceived, Optional.of(NetworkDirection.PLAY_TO_CLIENT)
        )
    }
}

object CustomScript {
    const val MOD_ID = "customscript"
    const val VERSION = "0.7"

    var isClient = false
        internal set

    var isTest = true

    lateinit var network: SimpleChannel

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
    fun playerTick(event: PlayerTickEvent) {
        if(event.side == LogicalSide.CLIENT) return
        if(ServerDataHandler.playerScriptState.containsKey(event.player.uniqueID) && ServerDataHandler.getDistanceByLastOpen(event.player) > 3) {
            ServerDataHandler.playerScriptState.remove(event.player.uniqueID)
            network.sendTo(QueueCloseGuiNetworkHandler(), (event.player as ServerPlayerEntity).connection.netManager, NetworkDirection.PLAY_TO_CLIENT)
        }
    }

    @SubscribeEvent
    fun onPlayerJoin(event: EntityJoinWorldEvent) {
        if(isClient) return
        if(event.entity is PlayerEntity && !ServerDataHandler.dynamicData.containsKey(event.entity.uniqueID))
            ServerDataHandler.dynamicData[event.entity.uniqueID] = emptyMap<String, String>().toMutableMap()
    }
}