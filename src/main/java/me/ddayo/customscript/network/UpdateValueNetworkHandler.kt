package me.ddayo.customscript.network

import me.ddayo.customscript.client.ClientDataHandler
import me.ddayo.customscript.client.event.OnDynamicValueUpdateEvent
import net.minecraft.network.PacketBuffer
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.network.NetworkEvent
import org.apache.logging.log4j.LogManager
import java.util.function.Supplier

class UpdateValueNetworkHandler() {
    var name = ""
    var value = ""

    constructor(name: String, value: String): this() {
        this.value = value
        this.name = name
    }

    companion object {
        fun onMessageReceived(message: UpdateValueNetworkHandler, ctxSuf: Supplier<NetworkEvent.Context>) = message.run {
            val ctx = ctxSuf.get()
            ctx.packetHandled = true
            ctx.enqueueWork {
                LogManager.getLogger().info("Value updated: ${message.name} ${message.value}")
                ClientDataHandler.dynamicValues[message.name] = message.value

                MinecraftForge.EVENT_BUS.post(OnDynamicValueUpdateEvent(message.name, message.value))
            }
        }


        @JvmStatic
        fun decode(buf: PacketBuffer) = UpdateValueNetworkHandler(buf.readString(), buf.readString())
    }

    fun encode(buf: PacketBuffer)
            = buf.writeString(name)
            .writeString(value)
}