package me.ddayo.customscript.network

import me.ddayo.customscript.client.gui.FontResource
import me.ddayo.customscript.client.gui.ImageResource
import net.minecraft.network.PacketBuffer
import net.minecraftforge.fml.network.NetworkEvent
import java.util.function.Supplier


class ClearCacheNetworkHandler() {
    var type = CacheType.IMAGE
    constructor(cacheType: CacheType): this() {
        type = cacheType
    }

    companion object {
        fun onMessageReceived(message: ClearCacheNetworkHandler, ctxSuf: Supplier<NetworkEvent.Context>) = message.run {
            val ctx = ctxSuf.get()
            ctx.packetHandled = true
            ctx.enqueueWork {
                when(type) {
                    CacheType.IMAGE -> ImageResource.clearResource()
                    CacheType.FONT -> FontResource.clearResource()
                }
            }
        }


        @JvmStatic
        fun decode(buf: PacketBuffer) = ClearCacheNetworkHandler(CacheType.valueOf(buf.readString()))
    }

    fun encode(buf: PacketBuffer) = buf.writeString(type.name)

    enum class CacheType {
        IMAGE, FONT
    }
}
