package me.ddayo.customscript.network

import net.minecraft.network.PacketBuffer
import net.minecraftforge.fml.network.NetworkEvent
import net.minecraftforge.fml.server.ServerLifecycleHooks
import java.util.function.Supplier

class ServerSideCommandNetworkHandler() {
    private var command = ""

    constructor(command: String): this() {
        this.command = command
    }

    companion object {
        fun onMessageReceived(message: ServerSideCommandNetworkHandler, ctxSuf: Supplier<NetworkEvent.Context>) = message.run {
            val ctx = ctxSuf.get()
            ctx.packetHandled = true
            ctx.enqueueWork {
               ServerLifecycleHooks.getCurrentServer().commandManager.handleCommand(ServerLifecycleHooks.getCurrentServer().commandSource, message.command)
            }
        }


        @JvmStatic
        fun decode(buf: PacketBuffer) = ServerSideCommandNetworkHandler(buf.readString())
    }

    fun encode(buf: PacketBuffer)
            = buf.writeString(command)
}