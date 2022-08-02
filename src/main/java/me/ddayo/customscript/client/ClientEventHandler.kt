package me.ddayo.customscript.client

import me.ddayo.customscript.CustomScript
import me.ddayo.customscript.client.gui.script.ScriptGui
import net.minecraft.client.Minecraft
import net.minecraftforge.client.event.RenderGameOverlayEvent
import net.minecraftforge.eventbus.api.SubscribeEvent
import java.io.File

object ClientEventHandler {
    private var renderScript: ScriptGui? = null

    fun initialize() {
        val scriptFileRoot =
            if (CustomScript.isTest) File("dummy") else File(Minecraft.getInstance().gameDir, CustomScript.MOD_ID)
        val scFile = if (CustomScript.isTest) File("hud.sc") else File(scriptFileRoot, "hud.sc")
        if(scFile.exists()) renderScript = ScriptGui("hud.sc", "default")
    }

    @SubscribeEvent
    public fun onRenderHud(event: RenderGameOverlayEvent.Post) {
        if(event.type != RenderGameOverlayEvent.ElementType.ALL) return
        renderScript?.render(event.matrixStack, 0, 0, 0.0f)
    }
}