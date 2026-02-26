package dev.chouten.runners.relay



actual object NativeBridge {
    init { System.loadLibrary("relay") }

    actual fun request(url: String, method: Int): HttpResponse {
        println("Requesting url: $url, method: $method")
        return HttpResponse(
            statusCode = 200,
            body = "TEMP",
            headers = mapOf(),
        )
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