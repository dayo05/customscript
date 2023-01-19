package me.ddayo.customscript.client.utils

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.core.tools.picocli.CommandLine.InitializationException
import java.io.File
import java.net.URL

abstract class Resource {
    companion object {
        fun fromInternet(url: String, callback: ((b: ByteArray) -> Unit)? = null) =
            resourceMap.getOrPut("I$url") { HttpResource(url, callback) }

        fun fromServer(resourceType: ResourceType, serverAssetUri: String, callback: ((b: ByteArray) -> Unit)? = null) =
            resourceMap.getOrPut("S$resourceType|$serverAssetUri") { ServerResource(resourceType, serverAssetUri, callback) }

        fun fromLocal(resourceType: ResourceType, uri: String, callback: ((b: ByteArray) -> Unit)? = null) =
            resourceMap.getOrPut("L$resourceType|$uri") { LocalResource(resourceType, uri, callback) }

        fun fromJar(resourceType: ResourceType, uri: String, callback: ((b: ByteArray) -> Unit)? = null) =
            resourceMap.getOrPut("J$resourceType|$uri") { JarResource(resourceType, uri, callback) }

        fun from(resourceType: ResourceType, uri: String, callback: ((b: ByteArray) -> Unit)? = null) =
            if (uri.startsWith("http:") || uri.startsWith("https:")) fromInternet(uri, callback)
            else if (uri.startsWith("server:")) fromServer(resourceType, uri, callback)
            else if(uri.startsWith("jar:")) fromJar(resourceType, uri, callback)
            else fromLocal(resourceType, uri, callback)

        private val resourceMap = mutableMapOf<String, Resource>()

        val Image = ResourceType.Images
        val Video = ResourceType.Videos
        val Font = ResourceType.Fonts
        val Script = ResourceType.Scripts
        val Misc = ResourceType.Misc
    }

    protected lateinit var bytes: ByteArray
    fun getBuffer() = if (this::bytes.isInitialized) bytes else null

    fun suspend(): ByteArray {
        var test = getBuffer()
        while(test == null) {
            Thread.yield()
            if(::errorThrown.isInitialized) throw InitializationException("Cannot load Error: ", errorThrown)
            test = getBuffer()
        }
        return test
    }

    enum class ResourceType {
        Images,
        Videos,
        Fonts,
        Scripts,
        Misc
    }

    abstract fun load()
    private lateinit var errorThrown: Exception

    init {
        Thread {
            try {
                load()
            } catch(e: Exception) {
                errorThrown = e
            }
        }.start()
    }
}

class LocalResource(val resourceType: ResourceType, val uri: String, val callback: ((b: ByteArray) -> Unit)? = null): Resource() {
    override fun load() {
        bytes = File("assets/${resourceType.name.lowercase()}", uri).readBytes()
        callback?.invoke(bytes)
    }
}

class JarResource(val resourceType: ResourceType, val uri: String, val callback: ((b: ByteArray) -> Unit)? = null): Resource() {
    override fun load() {
        bytes = javaClass.getResourceAsStream("assets/${resourceType.name.lowercase()}/${uri.substring(4)}")!!.readBytes()
        callback?.invoke(bytes)
    }
}

class ServerResource(resourceType: ResourceType, serverAssetUri: String, callback: ((b: ByteArray) -> Unit)? = null): Resource() {
    override fun load() {
        TODO("Not yet implemented")
    }
}

class HttpResource(val url: String, val callback: ((b: ByteArray) -> Unit)? = null): Resource() {
    override fun load() {
        bytes = URL(url).readBytes()
        callback?.invoke(bytes)
    }
}