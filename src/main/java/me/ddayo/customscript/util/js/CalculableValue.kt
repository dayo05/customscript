package me.ddayo.customscript.util.js

import jdk.nashorn.api.scripting.NashornScriptEngineFactory
import net.minecraft.client.Minecraft
import net.minecraft.util.Util
import net.minecraft.util.text.StringTextComponent
import org.apache.logging.log4j.LogManager
import java.io.PrintWriter
import java.io.StringWriter
import java.lang.IllegalStateException
import javax.script.Compilable
import javax.script.ScriptException

object CalculableValueManager {
    private val js = NashornScriptEngineFactory().getScriptEngine("-scripting")
    fun getValue(varName: String) = js.get(varName)
    fun setValue(varName: String, value: String) {
        setValueQuiet(varName, value)
        onUpdate()
    }

    // This method is only for to set frame-related data(mouse location or etc.)
    fun setValueQuiet(varName: String, value: String) {
        js.put(
            varName,
            value.toIntOrNull() ?: value.lowercase().toBooleanStrictOrNull() ?: value.toDoubleOrNull()
            ?: value.toBigIntegerOrNull() ?: value
        )
    }

    fun setValueQuiet(varName: String, value: Number) = js.put(varName, value)

    fun execute(script: String) = js.eval(script)
    fun compile(script: String) = (js as Compilable).compile(script)

    val dynamicValueHolder = mutableListOf<ICalculableHolder>()
    fun tick() {
        dynamicValueHolder.forEach { it.calculable.filter { it.isCallEveryTick }.forEach { it.calculate() } }
    }

    fun frame() {
        dynamicValueHolder.forEach { it.calculable.filter { it.isCallEveryFrame }.forEach { it.calculate() } }
    }

    fun onUpdate() {
        LogManager.getLogger().info("Update q")
        dynamicValueHolder.forEach { it.calculable.filter { it.isCallOnUpdate }.forEach { it.calculate() } }
    }
}

interface ICalculableHolder {
    val calculable: List<AbstractCalculable<out Any>>

    fun recalculateAll() {
        calculable.forEach {
            it.calculate()
        }
    }
}

class StringCalculable(script: String, info: String? = null) : AbstractCalculable<String>(script, info) {
    override fun convert(output: Any) = when (output) {
        is String -> output
        else -> output.toString()
    }

    override val defaultValue = "null"
    override fun convertDirect(value: String) = CalculableValueManager.getValue(value)?.toString() ?: value
}

class DoubleCalculable(script: String, info: String? = null) : AbstractCalculableValue<Double>(script, info) {
    override val typeName = Double::class.simpleName!!
    override fun fromNumber(output: Number) = output.toDouble()
    override fun fromString(output: String) = output.toDoubleOrNull()
}

class IntCalculable(script: String, info: String? = null) : AbstractCalculableValue<Int>(script, info) {
    override val typeName = Double::class.simpleName!!
    override fun fromNumber(output: Number) = output.toInt()
    override fun fromString(output: String) = output.toIntOrNull()
}

class FloatCalculable(script: String, info: String? = null) : AbstractCalculableValue<Float>(script, info) {
    override val typeName = Double::class.simpleName!!
    override fun fromNumber(output: Number) = output.toFloat()
    override fun fromString(output: String) = output.toFloatOrNull()
}

class LongCalculable(script: String, info: String? = null) : AbstractCalculableValue<Long>(script, info) {
    override val typeName = Double::class.simpleName!!
    override fun fromNumber(output: Number) = output.toLong()
    override fun fromString(output: String) = output.toLongOrNull()
}

class ShortCalculable(script: String, info: String? = null) : AbstractCalculableValue<Short>(script, info) {
    override val typeName = Short::class.simpleName!!
    override fun fromNumber(output: Number) = output.toShort()
    override fun fromString(output: String) = output.toShortOrNull()
}

class ByteCalculable(script: String, info: String? = null) : AbstractCalculableValue<Byte>(script, info) {
    override val typeName = Byte::class.simpleName!!
    override fun fromNumber(output: Number) = output.toByte()
    override fun fromString(output: String) = output.toByteOrNull()
}

class BooleanCalculable(private val script: String, private val info: String? = null) :
    AbstractCalculableValue<Boolean>(script, info) {
    override val typeName = Boolean::class.simpleName!!
    override fun fromNumber(output: Number): Boolean {
        LogManager.getLogger()
            .warn("Implicit conversion number to boolean happened: `$script`${info?.let { " on $it" } ?: ""}")
        return output.toInt() != 0
    }

    override fun fromString(output: String) = output.toBooleanStrictOrNull()
    override val defaultValue = true
}

abstract class AbstractCalculableValue<T>(script: String, info: String? = null) : AbstractCalculable<T>(script, info) {
    override fun convert(output: Any) = when (output) {
        is Number -> fromNumber(output)
        is String -> fromString(output)!!
        else -> throw IllegalArgumentException("Cannot parse $output into valid type '$typeName'")
    }

    abstract val typeName: String
    abstract fun fromNumber(output: Number): T
    abstract fun fromString(output: String): T?
    override val defaultValue by lazy { fromNumber(0) }
    override fun convertDirect(value: String): T = (fromString(value) ?: CalculableValueManager.getValue(value)?.let { fromString(it.toString()) }) ?: throw IllegalStateException("Cannot convert $value to valid data")
}

abstract class AbstractCalculable<T>(private val rawScript: String, private val info: String? = null) {
    private val script: String

    protected val ctl = mutableListOf<String>()

    init {
        var sc = rawScript.trimEnd()
        while (true) {
            sc = sc.trimStart()
            if (sc[0] != '!') break
            val b = sc.indexOfFirst { it.isISOControl() || it == ' ' }
            ctl.add(sc.substring(1, b))
            sc = sc.substring(b + 1)
        }

        if (sc[0] == '\\' || sc[0] == '₩' || sc[0] == '￦' || sc[0] == '￥')
            sc = sc.substring(1)
        script = sc
    }

    val isCallEveryFrame by lazy { ctl.contains("calc_frame") }
    val isCallEveryTick by lazy { ctl.contains("calc_tick") }
    val isCallOnUpdate by lazy { ctl.contains("calc_update") }
    val isFixed by lazy { ctl.contains("calc_once") }
    val isAlwaysReCalculate by lazy { ctl.contains("calc_always") }
    val isJsValue by lazy { isCallEveryFrame || isCallEveryTick || isCallOnUpdate || isFixed || isAlwaysReCalculate }
    val isFixedConst by lazy { ctl.contains("const") || !editable }

    private var calculated: T? = null
    open val get: T
        get() {
            if (calculated == null || isAlwaysReCalculate || (!isJsValue && !isFixedConst))
                calculate()
            return calculated ?: defaultValue
        }

    abstract fun convert(output: Any): T

    abstract val defaultValue: T

    // private val compiled by lazy { CalculableValueManager.compile(script) }

    private var updateHook: ((T) -> Unit)? = null
    fun setUpdateHook(f: (value: T) -> Unit) {
        updateHook = f
    }

    abstract fun convertDirect(value: String): T

    private var editable = true
    fun markNotEditable() = this.apply { editable = false }

    fun calculate() {
        if(!isJsValue) {
            calculated = convertDirect(script)
            return
        }

        val prev = calculated
        try {
            // calculated = convert(compiled.eval())
            calculated = convert(CalculableValueManager.execute(script))
        } catch (e: ScriptException) {
            Minecraft.getInstance().player?.sendMessage(
                StringTextComponent("Failed to calculate `$rawScript`: ${e.message}"),
                Util.DUMMY_UUID
            )
            LogManager.getLogger().error("Error on script execution${info?.let { " at $info" } ?: ""}")
            LogManager.getLogger().error(e.message)
        } catch (e: Exception) {
            Minecraft.getInstance().player?.sendMessage(
                StringTextComponent("Failed to calculate `$rawScript`: ${e.message}"),
                Util.DUMMY_UUID
            )
            LogManager.getLogger().error("Error on parsing result${info?.let { " at $info" } ?: ""}")

            val sw = StringWriter()
            val pw = PrintWriter(sw)
            e.printStackTrace(pw)
            LogManager.getLogger().error(sw.toString())
        }
        if (prev != calculated)
            calculated?.let {
                updateHook?.invoke(it)
            }
    }
}