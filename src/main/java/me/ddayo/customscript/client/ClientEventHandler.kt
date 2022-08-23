package me.ddayo.customscript.client

import com.mojang.blaze3d.matrix.MatrixStack
import net.minecraftforge.client.event.RenderGameOverlayEvent
import net.minecraftforge.eventbus.api.SubscribeEvent

object ClientEventHandler {
    @SubscribeEvent
    public fun onRenderHud(event: RenderGameOverlayEvent.Post) {
        if(event.type != RenderGameOverlayEvent.ElementType.ALL) return
        ClientDataHandler.enabledHud.forEach {
            it.value.render(MatrixStack(), 0, 0, 0.0f)
        }
    }
}