package me.ddayo.customscript.commands

import com.mojang.brigadier.CommandDispatcher
import net.minecraft.command.CommandSource

object CommandHandler {
    private val commands = listOf(UpdateDynamicValueCommand, OpenScriptCommand)
    fun register(dispatcher: CommandDispatcher<CommandSource>) {
        commands.forEach { it.register(dispatcher) }
    }

    interface ICommand {
        fun register(dispatcher: CommandDispatcher<CommandSource>)
    }
}