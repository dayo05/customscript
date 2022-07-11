package me.ddayo.customscript.commands

import com.mojang.authlib.GameProfile
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import me.ddayo.customscript.CustomScript
import me.ddayo.customscript.network.OpenScriptNetworkHandler
import net.minecraft.command.CommandSource
import net.minecraft.command.Commands
import net.minecraft.command.arguments.ArgumentTypes
import net.minecraft.command.arguments.GameProfileArgument
import net.minecraftforge.fml.network.NetworkDirection
import net.minecraftforge.fml.server.ServerLifecycleHooks

object OpenScriptCommand: CommandHandler.ICommand {
    override fun register(dispatcher: CommandDispatcher<CommandSource>) {
        dispatcher.register(Commands.literal("open")
                .then(Commands.argument("script", StringArgumentType.string())
                        .then(Commands.argument("player", GameProfileArgument.gameProfile())
                                .then(Commands.argument("begin position", StringArgumentType.string())
                                        .executes { execute(it) })
                                .executes { execute(it) })
                        .executes { execute(it) }))
    }

    private fun execute(it: CommandContext<CommandSource>): Int {
        val script = it.getArgument("script", String::class.java)
        val players = try { GameProfileArgument.getGameProfiles(it, "player").map { profile -> ServerLifecycleHooks.getCurrentServer().playerList.players.first { it.uniqueID == profile.id} } }
            catch (_: IllegalArgumentException) { listOf(it.source.asPlayer()) }
        val begin = try { it.getArgument("begin position", String::class.java) }
            catch(_: IllegalArgumentException) { "default" }

        for(p in players)
            CustomScript.network.sendTo(
                    OpenScriptNetworkHandler(
                            script, begin
                    ), p.connection.networkManager, NetworkDirection.PLAY_TO_CLIENT
            )
        return 1
    }
}