package me.ddayo.customscript.client.gui.script.blocks

import me.ddayo.customscript.CustomScript
import me.ddayo.customscript.network.ServerSideCommandNetworkHandler
import me.ddayo.customscript.util.options.CalculableValue
import me.ddayo.customscript.util.options.CompileError
import me.ddayo.customscript.util.options.Option
import me.ddayo.customscript.util.options.Option.Companion.string
import net.minecraft.client.Minecraft

class RunCommandBlock: BlockBase(), ISubscribeDynamicValueBlock {
    private lateinit var command: CalculableValue
    private var side = 0
    override fun parseContext(context: Option) {
        command = CalculableValue(context["Command"].string!!, true)
        side = when(context["Side"].string) {
            "Client" -> 0
            "Server" -> 1
            else -> throw CompileError("Not supported value on RunCommandBlock")
        }
    }

    override fun onEnter() {
        if(side == 0) Minecraft.getInstance().player?.sendChatMessage("/" + command.string)
        else CustomScript.network.sendToServer(ServerSideCommandNetworkHandler(command.string))
        super.onEnter()
    }

    override fun onUpdateValue() {
        super.onUpdateValue()
        command.updateValue()
    }
}