package dev.chouten.runners.relay

import kotlinx.serialization.json.Json.Default.encodeToString


actual object NativeBridge {
    init { System.loadLibrary("relay") }

    actual fun request(url: String, method: Int): String {
        println("Requesting url: $url, method: $method")
        val response = HttpResponse(
            statusCode = 200,
            body = "TEMP",
            headers = mapOf(),
        )

        return encodeToString(response)
    }

    external fun nativeLoadWasm(bytes: ByteArray)
    actual external fun initLogger(logger: Any)
    actual external fun initNativeBridge(nativeBridge: NativeBridge)

    actual fun load(bytes: ByteArray) {
        nativeLoadWasm(bytes)
    }
    actual external fun add(a: Int, b: Int): Int
    actual external fun callMethod(name: String): String
}