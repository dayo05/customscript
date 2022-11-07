package me.ddayo.customscript.network

import me.ddayo.customscript.client.ClientDataHandler
import me.ddayo.customscript.client.event.OnHudStateChangedEvent
import me.ddayo.customscript.client.gui.script.ScriptGui
import me.ddayo.customscript.client.gui.script.ScriptMode
import net.minecraft.client.Minecraft
import net.minecraft.network.PacketBuffer
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.network.NetworkEvent
import java.util.function.Supplier

class HudNetworkHandler() {
    var script = ""
    var isEnable = false

    constructor(script: String, isEnable: Boolean) : this() {
        this.script = script
        this.isEnable = isEnable
    }

    companion object {
        fun onMessageReceived(message: HudNetworkHandler, ctxSuf: Supplier<NetworkEvent.Context>) =
            message.run {
                val ctx = ctxSuf.get()
                ctx.packetHandled = true
                ctx.enqueueWork {
                    if(isEnable)
                        Minecraft.getInstance().let {
                            ClientDataHandler.enabledHud[script] = ScriptGui(ScriptMode.Hud, script, "hud").apply {
                                init(it, it.mainWindow.scaledWidth, it.mainWindow.scaledHeight)
                            }
                        }
                    else {
                        ClientDataHandler.enabledHud[script]?.run {
                            finish()
                        }
                        ClientDataHandler.enabledHud.remove(script)
                    }
                    MinecraftForge.EVENT_BUS.post(OnHudStateChangedEvent(script, isEnable))
                }
            }

        @JvmStatic
        fun decode(buf: PacketBuffer) = HudNetworkHandler(buf.readString(), buf.readBoolean())
    }

    fun encode(buf: PacketBuffer) = buf.writeString(script)
        .writeBoolean(isEnable)
}