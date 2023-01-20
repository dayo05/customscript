package me.ddayo.customscript.server.commands

import com.mojang.brigadier.CommandDispatcher
import net.minecraft.command.CommandSource

object CommandHandler {
    private val commands = listOf(CustomScriptCommand)
    fun register(dispatcher: CommandDispatcher<CommandSource>) {
        commands.forEach { it.register(dispatcher) }
    }

    interface ICommand {
        fun register(dispatcher: CommandDispatcher<CommandSource>)
    }
}