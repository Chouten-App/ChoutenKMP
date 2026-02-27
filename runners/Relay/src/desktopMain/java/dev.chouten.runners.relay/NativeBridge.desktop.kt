package dev.chouten.runners.relay

import kotlinx.serialization.json.Json.Default.encodeToString
import java.io.File

@Suppress("UnsafeDynamicallyLoadedCode")
actual object NativeBridge {
    init {
        val libName = System.mapLibraryName("relay")
        // → linux: librealy.so
        // → mac: librelay.dylib
        // → windows: relay.dll

        val path = File("../runners/Relay/src/main/build/desktop/$libName")
        System.load(path.absolutePath)
    }

    actual fun request(url: String, method: Int): String {
        println("Requesting url: $url, method: $method")
        val response = HttpResponse(
            statusCode = 200,
            body = "TEMP",
            headers = mapOf(),
        )
        return encodeToString(response)
    }

    private external fun gHostLogSet(fn: (bytes: ByteArray, len: Int) -> Unit)

    external fun nativeLoadWasm(bytes: ByteArray)
    actual external fun initLogger(logger: Any)
    actual external fun initNativeBridge(nativeBridge: NativeBridge)

    actual fun load(bytes: ByteArray) {
        nativeLoadWasm(bytes)
    }
    actual external fun add(a: Int, b: Int): Int
    actual external fun callMethod(name: String): String
}
