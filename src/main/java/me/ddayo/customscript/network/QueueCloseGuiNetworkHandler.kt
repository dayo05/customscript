package me.ddayo.customscript.network

import net.minecraft.client.Minecraft
import net.minecraft.network.PacketBuffer
import net.minecraftforge.fml.network.NetworkEvent
import java.util.function.Supplier

class QueueCloseGuiNetworkHandler {
    companion object {
        fun onMessageReceived(message: QueueCloseGuiNetworkHandler, ctxSuf: Supplier<NetworkEvent.Context>) = message.run {
            val ctx = ctxSuf.get()
            ctx.packetHandled = true
            ctx.enqueueWork {
                Minecraft.getInstance().displayGuiScreen(null)
            }
        }


        @JvmStatic
        fun decode(buf: PacketBuffer) = QueueCloseGuiNetworkHandler()
    }

    fun encode(buf: PacketBuffer) = buf
}