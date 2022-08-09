package me.ddayo.customscript.client.gui.font

import me.ddayo.customscript.client.gui.RenderUtil
import org.apache.logging.log4j.LogManager
import org.lwjgl.opengl.GL21

object FontManager {
    private val fontList = emptyMap<String, Font>().toMutableMap()

    fun getFont(font: String): Font {
        if(fontList.containsKey(font)) return fontList[font]!!
        fontList[font] = Font(font)
        return fontList[font]!!
    }

    fun useFontShader(f: () -> Unit) {
        if(fontShader == -1) initFontShader()
        RenderUtil.push {
            GL21.glUseProgram(fontShader)
            f()
            GL21.glUseProgram(0)
        }
    }

    private var fontShader = -1

    private fun initFontShader() {
        val vShader = GL21.glCreateShader(GL21.GL_VERTEX_SHADER);
        GL21.glShaderSource(vShader, """
            #version 120

            void main()
            {
                gl_Position = ftransform();
            }
        """.trimIndent())

        GL21.glCompileShader(vShader)
        val compilevState = listOf(0).toIntArray()
        GL21.glGetShaderiv(vShader, GL21.GL_COMPILE_STATUS, compilevState)
        if(compilevState[0] == GL21.GL_FALSE) {
            LogManager.getLogger().info("ERROR_VSHADER_COMPILE")
            LogManager.getLogger().info(GL21.glGetShaderInfoLog(vShader))
        }

        val fShader = GL21.glCreateShader(GL21.GL_FRAGMENT_SHADER)
        GL21.glShaderSource(fShader, """
            #version 120

            varying vec2      v_texCoord;
            uniform sampler2D s_texture;
            
            void main()
            {
                if(texture2D( s_texture, v_texCoord ).a < 0.5f)
                    gl_FragColor = vec4(1.0f, 1.0f, 1.0f, 0.0f);
                else gl_FragColor = vec4(1.0f, 1.0f, 1.0f, 1.0f);
                
                //gl_FragColor = texture2D(s_texture, v_texCoord);
            } 
        """.trimIndent())
        GL21.glCompileShader(fShader)
        val compilefState = listOf(0).toIntArray()
        GL21.glGetShaderiv(fShader, GL21.GL_COMPILE_STATUS, compilefState)
        if(compilefState[0] == GL21.GL_FALSE) {
            LogManager.getLogger().info("ERROR_FSHADER_COMPILE")
            LogManager.getLogger().info(GL21.glGetShaderInfoLog(fShader))
        }

        fontShader = GL21.glCreateProgram()
        GL21.glAttachShader(fontShader, vShader)
        GL21.glAttachShader(fontShader, fShader)
        GL21.glLinkProgram(fontShader)

        GL21.glDeleteShader(fShader)
        GL21.glDeleteShader(vShader)
    }
}