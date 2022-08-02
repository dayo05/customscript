package me.ddayo.customscript.network

import me.ddayo.customscript.client.gui.RenderUtil
import net.minecraft.network.PacketBuffer
import net.minecraftforge.fml.network.NetworkEvent
import java.util.function.Supplier

class ClearCacheNetworkHandler {
    companion object {
        fun onMessageReceived(message: ClearCacheNetworkHandler, ctxSuf: Supplier<NetworkEvent.Context>) = message.run {
            val ctx = ctxSuf.get()
            ctx.packetHandled = true
            ctx.enqueueWork {
                RenderUtil.removeAllTextures()
            }
        }


        @JvmStatic
        fun decode(buf: PacketBuffer) = ClearCacheNetworkHandler()
    }

    fun encode(buf: PacketBuffer) = buf
}