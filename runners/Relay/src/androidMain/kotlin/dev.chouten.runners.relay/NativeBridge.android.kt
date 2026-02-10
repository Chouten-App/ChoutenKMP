package dev.chouten.runners.relay



actual object NativeBridge {
    init { System.loadLibrary("relay") }

    actual fun request(url: String, method: Int): Int {
        println("Requesting url: $url, method: $method")
        return 0
    }

    external fun nativeLoadWasm(bytes: ByteArray)
    actual external fun initLogger(logger: Any)

    actual fun load(bytes: ByteArray) {
        nativeLoadWasm(bytes)
    }
    actual external fun add(a: Int, b: Int): Int
    actual external fun callMethod(name: String): String
}