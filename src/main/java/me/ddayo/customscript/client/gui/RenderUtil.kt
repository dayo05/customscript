package me.ddayo.customscript.client.gui

import com.mojang.blaze3d.systems.RenderSystem
import me.ddayo.customscript.CustomScript
import me.ddayo.customscript.client.utils.Resource
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.screen.Screen
import net.minecraft.util.ResourceLocation
import org.apache.logging.log4j.LogManager
import org.lwjgl.BufferUtils
import org.lwjgl.opengl.GL21
import org.lwjgl.stb.STBImage
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder

object RenderUtil {
    /**
     * Bind mod-embed texture
     * @param tex location of texture, this will convert as ResourceLocation
     * @see net.minecraft.client.renderer.texture.TextureManager.bindTexture
     */
    private fun bindTexture(tex: String) = Minecraft.getInstance().textureManager.bindTexture(ResourceLocation(CustomScript.MOD_ID, "textures/$tex"))

    /**
     * Cached image GL IDs
     */
    private val images = emptyMap<String, Int>().toMutableMap()

    private var iab = false

    /**
     * Make safe call to prevent loading other resource on one scope. RenderUtil::iab will set as true when this called.
     * @see useTexture
     * @see useExtTexture
     * @see iab
     */
    private fun withValidate(x: () -> Unit) {
        if(iab) throw IllegalStateException("Trying to overwrite texture")
        iab = true
        x()
        iab = false
    }

    /**
     * Bind loading texture, resource textures/loading.png must exist on resource directory. If not, error texture or black texture will bind.
     * @see bindTexture
     */
    private fun bindLoadingTexture() = bindTexture("loading.png")

    /**
     * Safe bind texture by GL ID
     * @see withValidate
     */
    fun useTexture(tex: Int, x: () -> Unit) {
        GL21.glBindTexture(GL21.GL_TEXTURE_2D, tex)
        withValidate(x)
        GL21.glBindTexture(GL21.GL_TEXTURE_2D, 0)
    }

    /**
     * Safe bind external texture with external directory or link.
     * If you want to bind image which exists on internet, the link must starts with https:// or http://
     * @see withValidate
     */
    fun useExtTexture(str: String, f: () -> Unit) {
        if(!images.containsKey(str)) {
            images[str] = -1
            Resource.from(Resource.Image, str) {
                LogManager.getLogger().info("$str loaded")
                val buf = BufferUtils.createByteBuffer(it.size)
                buf.put(it)
                buf.flip()

                val x = IntArray(1)
                val y = IntArray(1)
                val c = IntArray(1)
                val img = STBImage.stbi_load_from_memory(buf, x, y, c, 0)

                RenderSystem.recordRenderCall {
                    val tex = GL21.glGenTextures()
                    useTexture(tex) {
                        GL21.glEnable(GL21.GL_BLEND)
                        GL21.glTexParameteri(GL21.GL_TEXTURE_2D, GL21.GL_TEXTURE_WRAP_S, GL21.GL_CLAMP_TO_EDGE)
                        GL21.glTexParameteri(GL21.GL_TEXTURE_2D, GL21.GL_TEXTURE_WRAP_T, GL21.GL_CLAMP_TO_EDGE)
                        GL21.glTexParameteri(GL21.GL_TEXTURE_2D, GL21.GL_TEXTURE_MIN_FILTER, GL21.GL_LINEAR)
                        GL21.glTexParameteri(GL21.GL_TEXTURE_2D, GL21.GL_TEXTURE_MAG_FILTER, GL21.GL_LINEAR)

                        if (c[0] == 4)
                            GL21.glTexImage2D(
                                GL21.GL_TEXTURE_2D,
                                0,
                                GL21.GL_RGBA,
                                x[0],
                                y[0],
                                0,
                                GL21.GL_RGBA,
                                GL21.GL_UNSIGNED_BYTE,
                                img
                            )
                        else GL21.glTexImage2D(
                            GL21.GL_TEXTURE_2D,
                            0,
                            GL21.GL_RGB,
                            x[0],
                            y[0],
                            0,
                            GL21.GL_RGB,
                            GL21.GL_UNSIGNED_BYTE,
                            img
                        )
                        images[str] = tex
                    }
                }
            }
            bindLoadingTexture()
        }
        else if(images[str] != -1) useTexture(images[str]!!, f)
        else bindLoadingTexture()
    }

    /**
     * Safe bind internal texture.
     * @see bindTexture
     * @see withValidate
     */
    fun useTexture(str: String, x: () -> Unit) {
        bindTexture(str)
        withValidate(x)
    }

    /**
     * This method is only used for font rendering
     * @see <a href="https://cafe.naver.com/familyyd/2927617">Font rendering example</a>
     */
    fun bindGrayscaleBuffer(buf: ByteBuffer, width: Int, height: Int): Int {
        val tex = GL21.glGenTextures()
        useTexture(tex) {
            GL21.glTexParameteri(GL21.GL_TEXTURE_2D, GL21.GL_TEXTURE_WRAP_S, GL21.GL_CLAMP_TO_EDGE)
            GL21.glTexParameteri(GL21.GL_TEXTURE_2D, GL21.GL_TEXTURE_WRAP_T, GL21.GL_CLAMP_TO_EDGE)
            GL21.glTexParameteri(GL21.GL_TEXTURE_2D, GL21.GL_TEXTURE_MIN_FILTER, GL21.GL_LINEAR)
            GL21.glTexParameteri(GL21.GL_TEXTURE_2D, GL21.GL_TEXTURE_MAG_FILTER, GL21.GL_LINEAR)
            GL21.glPixelStorei(GL21.GL_UNPACK_ALIGNMENT, 1)

            GL21.glTexImage2D(GL21.GL_TEXTURE_2D, 0, GL21.GL_ALPHA, width, height, 0, GL21.GL_ALPHA, GL21.GL_UNSIGNED_BYTE, buf)
        }
        return tex
    }

    /**
     * Check is resource exists on internal part of mod.
     * @return Is resource exists
     */
    fun resourceExists(tex: String) = Minecraft.getInstance().resourceManager.hasResource(ResourceLocation(CustomScript.MOD_ID, tex))

    /**
     * Check is resource exists on external directory
     * @return Is resource exists
     */
    fun extResourceExists(tex: String) = File(Minecraft.getInstance().gameDir, "assets/images/$tex").exists()

    /**
     * Render resource on entire screen.
     * This requires FHDScale call
     */
    fun render() = render(0, 0, 1920, 1080)

    /**
     * Render rect resource.
     */
    fun render(x: Int, y: Int, w: Int, h: Int) = render(x.toDouble(), y.toDouble(), w.toDouble(), h.toDouble())

    /**
     * Render rect resource.
     */
    fun render(x: Double, y: Double, w: Double, h: Double) = render(x, y, w, h, 0.0, 1.0, 0.0, 1.0)

    /**
     * Render rect resource with some TexCoord point.
     */
    fun render(x: Double, y: Double, w: Double, h: Double, th1: Double, th2: Double, tv1: Double, tv2: Double) {
        GL21.glBegin(GL21.GL_QUADS)
        GL21.glTexCoord2d(th2, tv1)
        GL21.glVertex2d(x + w, y)
        GL21.glTexCoord2d(th1, tv1)
        GL21.glVertex2d(x, y)
        GL21.glTexCoord2d(th1, tv2)
        GL21.glVertex2d(x, y + h)
        GL21.glTexCoord2d(th2, tv2)
        GL21.glVertex2d(x + w, y + h)
        GL21.glEnd()
    }

    /**
     * Get width of bound resource.
     * @return width of bound resource
     */
    fun getWidth() = GL21.glGetTexLevelParameteri(GL21.GL_TEXTURE_2D, 0, GL21.GL_TEXTURE_WIDTH)

    /**
     * Get height of bound resource.
     * @return height of bound resource
     */
    fun getHeight() = GL21.glGetTexLevelParameteri(GL21.GL_TEXTURE_2D, 0, GL21.GL_TEXTURE_HEIGHT)

    /**
     * Safe call of glPushMatrix/glPopMatrix
     * @see org.lwjgl.opengl.GL21.glPushMatrix
     * @see org.lwjgl.opengl.GL21.glPopMatrix
     */
    fun push(x: () -> Unit) {
        GL21.glPushMatrix()
        x()
        GL21.glPopMatrix()
    }

    /**
     * Call GL21.glScaled to make rendering same on every type of screen
     * @param width Width of GUI
     * @see net.minecraft.client.gui.screen.Screen.width
     * @param height Height of GUI
     * @see net.minecraft.client.gui.screen.Screen.height
     */
    fun FHDScale(width: Int, height: Int, x: () -> Unit) = FHDScale(width.toDouble(), height.toDouble(), x)

    /**
     * Call GL21.glScaled to make rendering same on every type of screen
     * @param gui Target GUI
     * @see net.minecraft.client.gui.screen.Screen.width
     * @see net.minecraft.client.gui.screen.Screen.height
     */
    fun FHDScale(gui: Screen, x: () -> Unit) = FHDScale(gui.height, gui.width, x)

    /**
     * Call GL21.glScaled to make rendering same on every type of screen
     * @param width Width of GUI
     * @see net.minecraft.client.gui.screen.Screen.width
     * @param height Height of GUI
     * @see net.minecraft.client.gui.screen.Screen.height
     */
    fun FHDScale(width: Double, height: Double, x: () -> Unit) {
        push {
            GL21.glTranslated((width - height * 16.0 / 9) / 2, 0.0, 0.0)
            GL21.glScaled(height / 1080.0, height / 1080.0, height / 1080.0)
            x()
        }
    }

    /**
     * Clear all caches
     */
    fun removeAllTextures() {
        GL21.glDeleteTextures(images.values.toIntArray())
        images.clear()
    }

    /**
     * Clear selected cache
     * @param tex resource to remove cache
     * @see bindTexture
     */
    fun removeTexture(tex: String){
        GL21.glDeleteTextures(images[tex]!!)
        images.remove(tex)
    }
}