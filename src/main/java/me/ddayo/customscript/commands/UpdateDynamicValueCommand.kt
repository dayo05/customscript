package me.ddayo.customscript.commands

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.StringArgumentType
import me.ddayo.customscript.StringUtil
import net.minecraft.command.CommandSource
import net.minecraft.command.Commands
import net.minecraft.util.text.StringTextComponent

object UpdateDynamicValueCommand: CommandHandler.ICommand {
    override fun register(dispatcher: CommandDispatcher<CommandSource>) {
        dispatcher.register(Commands.literal("csvalue")
                .then(Commands.literal("set")
                        .then(Commands.argument("key", StringArgumentType.word())
                                .then(Commands.argument("value", StringArgumentType.string())
                                        .executes {
                                            StringUtil.updateValue(it.getArgument("key", String::class.java), it.getArgument("value", String::class.java))
                                            1
                                        })))
                .then(Commands.literal("get")
                        .then(Commands.argument("key", StringArgumentType.word())
                                .executes {
                                    it.source.sendFeedback(StringTextComponent(StringUtil.getValueIn(it.getArgument("key", String::class.java)) ?: ""), true)
                                    1
                                }))
        )
    }
}
