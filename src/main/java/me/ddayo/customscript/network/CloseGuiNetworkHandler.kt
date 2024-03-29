package me.ddayo.customscript.network

import me.ddayo.customscript.server.ServerConfiguration
import me.ddayo.customscript.server.ServerDataHandler
import net.minecraft.network.PacketBuffer
import net.minecraftforge.fml.network.NetworkEvent
import java.util.function.Supplier

class CloseGuiNetworkHandler {
    companion object {
        fun onMessageReceived(message: CloseGuiNetworkHandler, ctxSuf: Supplier<NetworkEvent.Context>) = message.run {
            val ctx = ctxSuf.get()
            ctx.packetHandled = true
            ctx.enqueueWork {
                if(ServerDataHandler.playerScriptState.containsKey(ctx.sender!!.uniqueID)) {
                    val values = ServerConfiguration.scriptValueUpdater.filter { it.value.contains(ServerDataHandler.playerScriptState[ctx.sender!!.uniqueID]!!.script) }
                    ServerDataHandler.playerScriptState.remove(ctx.sender!!.uniqueID)
                    ServerDataHandler.unsubscribeDynamicValue(ctx.sender!!, *values.keys.toTypedArray())
                }
            }
        }

        @JvmStatic
        fun decode(buf: PacketBuffer) = CloseGuiNetworkHandler()
    }

    fun encode(buf: PacketBuffer) = buf
}