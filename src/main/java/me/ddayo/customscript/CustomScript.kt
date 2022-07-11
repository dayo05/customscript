package me.ddayo.customscript

import me.ddayo.customscript.CustomScript.MOD_ID
import me.ddayo.customscript.commands.CommandHandler
import me.ddayo.customscript.network.OpenScriptNetworkHandler
import net.minecraft.util.ResourceLocation
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.event.RegisterCommandsEvent
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.event.server.FMLServerStartingEvent
import net.minecraftforge.fml.network.NetworkDirection
import net.minecraftforge.fml.network.NetworkRegistry
import net.minecraftforge.fml.network.simple.SimpleChannel
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

    }

    @SubscribeEvent
    fun registerCommandEvent(event: RegisterCommandsEvent)
        = CommandHandler.register(event.dispatcher)
}