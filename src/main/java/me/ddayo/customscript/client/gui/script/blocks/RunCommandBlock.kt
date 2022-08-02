package me.ddayo.customscript.client.gui.script.blocks

import me.ddayo.customscript.CustomScript
import me.ddayo.customscript.client.ClientDataHandler
import me.ddayo.customscript.network.ServerSideCommandNetworkHandler
import me.ddayo.customscript.util.options.CompileError
import me.ddayo.customscript.util.options.Option
import me.ddayo.customscript.util.options.Option.Companion.string
import net.minecraft.client.Minecraft

class RunCommandBlock: BlockBase() {
    var command = ""
        private set
    var side = 0
    override fun parseContext(context: Option) {
        command = context["Command"].string!!
        side = when(context["Side"].string) {
            "Client" -> 0
            "Server" -> 1
            else -> throw CompileError("Not supported value on RunCommandBlock")
        }
    }

    override fun onEnter() {
        if(side == 0) Minecraft.getInstance().player?.sendChatMessage(ClientDataHandler.decodeDynamicValue("/$command"))
        else CustomScript.network.sendToServer(ServerSideCommandNetworkHandler(command))
        super.onEnter()
    }
}