package me.ddayo.customscript.util.options

import jdk.nashorn.api.scripting.NashornScriptEngineFactory
import org.apache.logging.log4j.LogManager
import javax.script.ScriptException
import kotlin.math.roundToInt
import kotlin.math.roundToLong

class CalculableValue(private val original: String, private val stringMode: Boolean = false) {
    companion object {
        private val js = NashornScriptEngineFactory().getScriptEngine("-scripting")
        fun setValue(varName: String, value: String) {
            js.put(
                varName,
                value.toIntOrNull() ?: value.lowercase().toBooleanStrictOrNull() ?: value.toDoubleOrNull()
                ?: value.toBigIntegerOrNull() ?: value
            )
        }
    }

    val double: Double
        get() = when (calculated) {
            is Double -> calculated as Double
            is Int -> (calculated as Int).toDouble()
            is Long -> (calculated as Long).toDouble()
            else -> 0.0
        }
    val float get() = double.toFloat()

    val int: Int
        get() = when (calculated) {
            is Int -> calculated as Int
            is Double -> (calculated as Double).roundToInt()
            is Long -> (calculated as Long).toInt()
            else -> 0
        }
    val long: Long
        get() = when (calculated) {
            is Int -> (calculated as Int).toLong()
            is Double -> (calculated as Double).roundToLong()
            is Long -> calculated as Long
            else -> 0L
        }

    val string: String
        get() = when (calculated) {
            is String -> calculated as String
            else -> calculated.toString()
        }

    val bool: Boolean
        get() = when (calculated) {
            is Boolean -> calculated as Boolean
            is Int -> calculated != 0
            is Long -> calculated != 0L
            is Double -> calculated != 0.0
            else -> false
        }

    private val calculated
        get() = try {
            if (stringMode) js.eval(
                "\"${
                    original.replace("\\", "\\\\")
                        .replace("\"", "\\\"")
                }\""
            ) else js.eval(original)
        } catch (e: ScriptException) {
            0
        }
}