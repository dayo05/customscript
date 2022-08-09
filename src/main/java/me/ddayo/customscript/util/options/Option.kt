package me.ddayo.customscript.util.options

class Option(bkey: String, bvalue: String) {
    private var _key = ""
    var key
        get() = _key.decodeOption
        set(value) {
            _key = value.encodeOption
        }

    init { key = bkey }

    private var _value = ""
    var value
        get() = _value.decodeOption
        set(value) {
            _value = value.encodeOption
        }

    init { value = bvalue }

    val subOptions = emptyList<Option>().toMutableList()

    constructor(key: String, value: Any): this(key, value.toString())

    fun append(opt: Option) = this.apply{ subOptions.add(opt) }
    fun append(key: String, value: String) = append(Option(key, value))
    fun append(key: String, value: Any) = append(Option(key, value))

    companion object {
        public val List<Option>.int
            get() = this.firstOrNull()?.value?.toInt()
        public val List<Option>.long
            get() = this.firstOrNull()?.value?.toLong()
        public val List<Option>.string
            get() = this.firstOrNull()?.value
        public val List<Option>.double
            get() = this.firstOrNull()?.value?.toDouble()

        private fun countStartRArrow(s: String): Pair<Int, String>
        {
            for(i in s.indices)
                if (s[i] != '>')
                    return Pair(i, s.substring(i until s.length))
            return Pair(s.length, "")
        }

        fun createRootOption() = Option("", "")

        fun readOption(str: String): Option {
            val root = createRootOption()
            var prv = -1
            var line = 0
            for(c in str.split("\n").map{it.trimStart()}) {
                line++
                if (c == "") continue
                if (c[0] == '#') continue
                val (d, cmd) = countStartRArrow(c)
                if(cmd == "") continue
                if (d > prv + 1) throw CompileError("Compile error on line $line")

                var o = root
                for (i in 0 until d)
                    o = o.subOptions.last()

                val splitPos = cmd.indexOf('=')
                o.append(cmd.substring(0, splitPos), cmd.substring((splitPos + 1) until cmd.length))
                prv = d
            }
            return root
        }
    }

    private val String.encodeOption
        get() = this.replace("\r", "").replace("\\", "\\\\").replace("\n", "\\n").replace(">", "\\>")

    private val String.decodeOption
        get() = this.replace("\r", "").replace("\\>", ">").replace("\\n", "\n").replace("\\\\", "\\")

    operator fun get(x: String) = subOptions.filter{ it.key == x }

    fun str(dim: Int = 0): String = ">".repeat(dim) + "$key=$value\n" + subOptions.joinToString("\n") { it.str(dim + 1) }
}