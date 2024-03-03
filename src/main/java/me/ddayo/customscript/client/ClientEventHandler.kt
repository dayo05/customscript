package me.ddayo.customscript.client

import com.mojang.blaze3d.matrix.MatrixStack
import com.mojang.blaze3d.systems.RenderSystem
import me.ddayo.customscript.client.gui.FontResource
import me.ddayo.customscript.client.gui.ImageResource
import me.ddayo.customscript.util.js.CalculableValueManager
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.screen.MainMenuScreen
import net.minecraft.client.gui.screen.MultiplayerScreen
import net.minecraft.client.gui.screen.WorldSelectionScreen
import net.minecraft.client.resources.ReloadListener
import net.minecraft.profiler.IProfiler
import net.minecraft.resources.IResourceManager
import net.minecraftforge.client.event.GuiOpenEvent
import net.minecraftforge.client.event.RenderGameOverlayEvent
import net.minecraftforge.event.AddReloadListenerEvent
import net.minecraftforge.event.TickEvent
import net.minecraftforge.eventbus.api.SubscribeEvent
import org.apache.logging.log4j.LogManager
import java.util.concurrent.CompletableFuture

object ClientEventHandler {
    @SubscribeEvent
    public fun onRenderHud(event: RenderGameOverlayEvent.Post) {
        if(event.type != RenderGameOverlayEvent.ElementType.ALL) return
        ClientDataHandler.enabledHud.forEach {
            it.value.render(MatrixStack(), 0, 0, 0.0f)
        }
    }

    /*
    @SubscribeEvent
    public fun mainSc(event: GuiOpenEvent) {
        if(event.gui is WorldSelectionScreen)
            event.gui = MultiplayerScreen(event.gui)
    }
     */

    @SubscribeEvent
    public fun tick(event: TickEvent.ClientTickEvent) {
        CalculableValueManager.tick()
    }
}