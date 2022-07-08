package me.ddayo.customscript.commands

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.StringArgumentType
import me.ddayo.customscript.CustomScript
import me.ddayo.customscript.network.OpenScriptNetworkHandler
import net.minecraft.command.CommandSource
import net.minecraft.command.Commands
import net.minecraftforge.fml.network.NetworkDirection

object OpenScriptCommand: CommandHandler.ICommand {
    override fun register(dispatcher: CommandDispatcher<CommandSource>) {
        dispatcher.register(Commands.literal("open")
            .then(Commands.argument("script", StringArgumentType.string())
                .then(Commands.argument("begin", StringArgumentType.string())
                    .executes {
                        CustomScript.network.sendTo(
                            OpenScriptNetworkHandler(
                                it.getArgument(
                                    "script",
                                    String::class.java
                                ), it.getArgument("begin", String::class.java)
                            ), it.source.asPlayer().connection.networkManager, NetworkDirection.PLAY_TO_CLIENT
                        )
                        1
                    })
                .executes {
                    CustomScript.network.sendTo(
                        OpenScriptNetworkHandler(
                            it.getArgument(
                                "script",
                                String::class.java
                            )
                        ), it.source.asPlayer().connection.networkManager, NetworkDirection.PLAY_TO_CLIENT
                    )
                    1
                }))
    }
}