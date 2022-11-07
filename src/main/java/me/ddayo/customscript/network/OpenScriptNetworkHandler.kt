package me.ddayo.customscript.network

import me.ddayo.customscript.client.gui.script.ScriptGui
import me.ddayo.customscript.client.gui.script.ScriptMode
import net.minecraft.client.Minecraft
import net.minecraft.network.PacketBuffer
import net.minecraftforge.fml.network.NetworkEvent
import java.util.function.Supplier

class OpenScriptNetworkHandler() {
    var script = ""
    var begin = ""

    constructor(script: String, begin: String): this() {
        this.script = script
        this.begin = begin
    }

    constructor(script: String): this() {
        this.script = script
        this.begin = "default"
    }

    companion object {
        fun onMessageReceived(message: OpenScriptNetworkHandler, ctxSuf: Supplier<NetworkEvent.Context>) = message.run {
            val ctx = ctxSuf.get()
            ctx.packetHandled = true
            ctx.enqueueWork {
                Minecraft.getInstance().displayGuiScreen(ScriptGui(ScriptMode.Gui, script, begin))
            }
        }


        @JvmStatic
        fun decode(buf: PacketBuffer) = OpenScriptNetworkHandler(buf.readString(), buf.readString())
    }

    fun encode(buf: PacketBuffer)
            = buf.writeString(script)
        .writeString(begin)
}