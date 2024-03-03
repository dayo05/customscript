package me.ddayo.customscript.client.gui.script.blocks

import me.ddayo.customscript.CustomScript
import me.ddayo.customscript.network.ServerSideCommandNetworkHandler
import me.ddayo.customscript.util.js.AbstractCalculable
import me.ddayo.customscript.util.js.ICalculableHolder
import me.ddayo.customscript.util.js.StringCalculable
import me.ddayo.customscript.util.options.CompileError
import me.ddayo.customscript.util.options.Option
import me.ddayo.customscript.util.options.Option.Companion.string
import net.minecraft.client.Minecraft

class RunCommandBlock: BlockBase(), ICalculableHolder {
    private lateinit var command: StringCalculable
    private var side = 0
    override fun parseContext(context: Option) {
        command = StringCalculable(context["Command"].string!!)
        side = when(context["Side"].string) {
            "Client" -> 0
            "Server" -> 1
            else -> throw CompileError("Not supported value on RunCommandBlock")
        }
    }

    override fun onEnter() {
        if(side == 0) Minecraft.getInstance().player?.sendChatMessage("/" + command.get)
        else CustomScript.network.sendToServer(ServerSideCommandNetworkHandler(command.get))
        super.onEnter()
    }

    override val calculable by lazy { listOf(command) }
}