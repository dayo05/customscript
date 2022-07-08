package me.ddayo.customscript.network

import me.ddayo.customscript.CustomScript
import me.ddayo.customscript.StringUtil
import net.minecraft.network.PacketBuffer
import net.minecraftforge.fml.network.NetworkEvent
import org.apache.logging.log4j.LogManager
import java.util.function.Supplier

class StringDataNetworkHandler() {
    var key = ""
    var value = ""
    constructor(key: String, value: String): this() {
        this.key = key
        this.value = value
    }

    companion object {
        private val logger = LogManager.getLogger()

        fun onMessageReceived(message: StringDataNetworkHandler, ctxSuf: Supplier<NetworkEvent.Context>) {
            val ctx = ctxSuf.get()
            ctx.packetHandled = true
            ctx.enqueueWork {
                StringUtil.updateValue(message.key, message.value)
                logger.info("Update ${message.key} as ${message.value}")
            }
        }

        @JvmStatic
        fun decode(buf: PacketBuffer) = StringDataNetworkHandler(buf.readString(), buf.readString())
    }

    fun encode(buf: PacketBuffer)
            = buf.writeString(key)
            .writeString(value)
}