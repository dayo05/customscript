package me.ddayo.customscript.commands

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import me.ddayo.customscript.CustomScript
import me.ddayo.customscript.client.gui.RenderUtil
import me.ddayo.customscript.client.gui.script.ScriptGui
import me.ddayo.customscript.network.ClearCacheNetworkHandler
import me.ddayo.customscript.network.OpenScriptNetworkHandler
import me.ddayo.customscript.network.UpdateValueNetworkHandler
import me.ddayo.customscript.server.ServerConfiguration
import me.ddayo.customscript.server.ServerDataHandler
import net.minecraft.client.Minecraft
import net.minecraft.command.CommandSource
import net.minecraft.command.Commands
import net.minecraft.command.arguments.GameProfileArgument
import net.minecraft.command.arguments.UUIDArgument
import net.minecraft.util.text.StringTextComponent
import net.minecraftforge.fml.network.NetworkDirection
import net.minecraftforge.fml.server.ServerLifecycleHooks
import java.util.*

object CustomScriptCommand: CommandHandler.ICommand {
    public val baseCommand = Commands.literal("cs")
        .then(Commands.literal("open")
            .requires {
                it.hasPermissionLevel(2)
            }
            .then(
                Commands.argument("script", StringArgumentType.string())
                    .then(Commands.argument("player", GameProfileArgument.gameProfile())
                        .then(Commands.argument("begin position", StringArgumentType.string())
                            .executes { execute(it) })
                        .executes { execute(it) })
            )
        )
        .then(
            Commands.literal("cache")
                .then(
                    Commands.literal("image")
                        .then(Commands.literal("clear")
                            .executes {
                                if (CustomScript.isClient)
                                    RenderUtil.removeAllTextures()
                                else CustomScript.network.sendTo(
                                    ClearCacheNetworkHandler(),
                                    it.source.asPlayer().connection.networkManager, NetworkDirection.PLAY_TO_CLIENT
                                )
                                1
                            })
                )
        )
        .then(Commands.literal("value")
            .requires {
                it.hasPermissionLevel(2)
            }
            .then(
                Commands.argument("player", GameProfileArgument.gameProfile())
                    .then(
                        Commands.argument("value name", StringArgumentType.string())
                            .then(Commands.argument("value", StringArgumentType.string())
                                .executes {
                                    GameProfileArgument.getGameProfiles(it, "player").map { profile ->
                                        ServerLifecycleHooks.getCurrentServer().playerList.getPlayerByUUID(
                                            profile.id
                                        )!!
                                    }.forEach { p ->
                                        ServerDataHandler.updateData(
                                            p,
                                            it.getArgument("value name", String::class.java),
                                            it.getArgument("value", String::class.java)
                                        )
                                    }
                                    1
                                })
                    )
            )
        )
        .then(Commands.literal("internal-value")
            .requires {
                it.hasPermissionLevel(2)
            }
            .then(
                Commands.argument("value name", StringArgumentType.string())
                    .then(Commands.argument("value", StringArgumentType.string())
                        .executes {
                            ServerDataHandler.updateInternalData(
                                it.source,
                                it.getArgument("value name", String::class.java),
                                it.getArgument("value", String::class.java)
                            )
                            1
                        })
            )
        )
        .then(
            Commands.literal("debug")
                .then(
                    Commands.literal("show-value")
                        .then(
                            Commands.argument("player", UUIDArgument.func_239194_a_())
                                .then(Commands.argument("value name", StringArgumentType.string())
                                    .executes {
                                        it.getArgument("value name", String::class.java).run {
                                            it.source.sendFeedback(
                                                StringTextComponent(
                                                    "Value $this: ${
                                                        ServerDataHandler.dynamicData[it.getArgument(
                                                            "player",
                                                            UUID::class.java
                                                        )]?.get(this)
                                                    }"
                                                ),
                                                true
                                            )
                                        }
                                        1
                                    })
                        )
                )
                .then(
                    Commands.literal("show-internal-value")
                        .then(Commands.argument("value name", StringArgumentType.string())
                            .executes {
                                it.getArgument("value name", String::class.java).run {
                                    it.source.sendFeedback(
                                        StringTextComponent("Internal value $this: ${ServerDataHandler.internalData[this]}"),
                                        true
                                    )
                                }
                                1
                            })
                )
        )

    override fun register(dispatcher: CommandDispatcher<CommandSource>) {
        dispatcher.register(baseCommand)
    }

    private fun execute(it: CommandContext<CommandSource>): Int {
        val script = it.getArgument("script", String::class.java)
        val players = try {
            GameProfileArgument.getGameProfiles(it, "player")
                .map { profile -> ServerLifecycleHooks.getCurrentServer().playerList.players.first { it.uniqueID == profile.id } }
        } catch (_: IllegalArgumentException) {
            listOf(it.source.asPlayer())
        }
        val begin = try {
            it.getArgument("begin position", String::class.java)
        } catch (_: IllegalArgumentException) {
            "default"
        }

        if (CustomScript.isClient)
            Minecraft.getInstance().displayGuiScreen(ScriptGui(script, begin))
        else {
            val values = ServerConfiguration.scriptValueUpdater.filter { it.value.contains(script) }
            for (p in players) {
                ServerDataHandler.playerScriptState[p.uniqueID] =
                    ServerDataHandler.ScriptStateData(script, p.posX, p.posY, p.posZ)
                values.forEach {
                    if (ServerDataHandler.dynamicData[p.uniqueID]!!.contains(it.key))
                        CustomScript.network.sendTo(
                            UpdateValueNetworkHandler(
                                it.key,
                                ServerDataHandler.dynamicData[p.uniqueID]!![it.key]!!
                            ), p.connection.networkManager, NetworkDirection.PLAY_TO_CLIENT
                        )
                    else if (ServerDataHandler.internalData.contains(it.key))
                        CustomScript.network.sendTo(
                            UpdateValueNetworkHandler(
                                it.key,
                                ServerDataHandler.internalData[it.key]!!
                            ), p.connection.networkManager, NetworkDirection.PLAY_TO_CLIENT
                        )
                }
                CustomScript.network.sendTo(
                    OpenScriptNetworkHandler(
                        script, begin
                    ), p.connection.networkManager, NetworkDirection.PLAY_TO_CLIENT
                )
            }
        }
        return 1
    }
}