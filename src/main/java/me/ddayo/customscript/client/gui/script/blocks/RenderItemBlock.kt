package me.ddayo.customscript.client.gui.script.blocks

import me.ddayo.customscript.client.gui.RenderUtil
import me.ddayo.customscript.client.gui.script.ScriptGui
import me.ddayo.customscript.util.js.*
import me.ddayo.customscript.util.options.Option
import me.ddayo.customscript.util.options.Option.Companion.string
import net.minecraft.client.Minecraft
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.util.ResourceLocation
import net.minecraftforge.registries.ForgeRegistries
import org.apache.logging.log4j.LogManager

class RenderItemBlock: BlockBase() {
    private lateinit var item: StringCalculable
    private lateinit var x: IntCalculable
    private lateinit var y: IntCalculable
    private lateinit var size: DoubleCalculable
    override fun parseContext(context: Option) {
        item = StringCalculable(context["Item"].string!!)

        x = IntCalculable(context["X"].string!!)
        y = IntCalculable(context["Y"].string!!)
        size = DoubleCalculable(context["Size"].string!!)
    }

    class RenderItemRenderer(private val item: StringCalculable, private val x: IntCalculable, private val y: IntCalculable, private val size: DoubleCalculable): ScriptRenderer(), ICalculableHolder {
        override fun RenderUtil.renderInternal() {
            Minecraft.getInstance().itemRenderer.renderItemIntoGUI(ForgeRegistries.ITEMS.getValue(ResourceLocation(item.get))
                ?.let { ItemStack(it) } ?: run {
                LogManager.getLogger().error("Item ${item.get} not found")
                ItemStack(Items.ITEM_FRAME)
            }, x.get, y.get)
        }

        override val renderParse: ScriptGui.RenderParse
            get() = ScriptGui.RenderParse.Main
        override val calculable by lazy { listOf(item, x, y, size) }
        override val isLoading = false
    }

    override val rendererInstance
        get() = RenderItemRenderer(item, x, y, size)
}