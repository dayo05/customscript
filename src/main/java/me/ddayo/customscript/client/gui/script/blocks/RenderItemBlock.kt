package me.ddayo.customscript.client.gui.script.blocks

import me.ddayo.customscript.client.gui.script.ScriptGui
import me.ddayo.customscript.util.options.CalculableValue
import me.ddayo.customscript.util.options.Option
import me.ddayo.customscript.util.options.Option.Companion.string
import net.minecraft.client.Minecraft
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.util.ResourceLocation
import net.minecraftforge.registries.ForgeRegistries
import org.apache.logging.log4j.LogManager

class RenderItemBlock: BlockBase() {
    private lateinit var item: CalculableValue
    private lateinit var x: CalculableValue
    private lateinit var y: CalculableValue
    private lateinit var size: CalculableValue
    override fun parseContext(context: Option) {
        item = CalculableValue(context["Item"].string!!, true)

        x = CalculableValue(context["X"].string!!)
        y = CalculableValue(context["Y"].string!!)
        size = CalculableValue(context["Size"].string!!)
    }

    class RenderItemRenderer(private val item: CalculableValue, private val x: CalculableValue, private val y: CalculableValue, private val size: CalculableValue): ScriptRenderer() {
        override fun render() {
            Minecraft.getInstance().itemRenderer.renderItemIntoGUI(ForgeRegistries.ITEMS.getValue(ResourceLocation(item.string))
                ?.let { ItemStack(it) } ?: run {
                LogManager.getLogger().error("Item ${item.calculated} not found")
                ItemStack(Items.ITEM_FRAME)
            }, x.int, y.int
            )
        }

        override val renderParse: ScriptGui.RenderParse
            get() = ScriptGui.RenderParse.Main

    }
    override val rendererInstance
        get() = RenderItemRenderer(item, x, y, size)
}