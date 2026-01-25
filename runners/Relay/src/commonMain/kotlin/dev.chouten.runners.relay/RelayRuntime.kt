package dev.chouten.runners.relay

// TODO: Move out into core
sealed interface RtValue {
    data class RtObject(val fields: Map<String, RtValue>) : RtValue
    data class RtArray(val items: List<RtValue>) : RtValue
    data class RtString(val value: String) : RtValue
    data class RtNumber(val value: Double) : RtValue
    data class RtBool(val value: Boolean) : RtValue
    data object RtNull : RtValue
}

sealed class SourceOperation(val functionName: String) {
    data object Search : SourceOperation("search")
    data object Details : SourceOperation("details")
    data object Chapters : SourceOperation("chapters")
    data object Pages : SourceOperation("pages")
    data object Stream : SourceOperation("stream")
}

data class ExecutionContext(
    val timeoutMs: Long = 15_000,
    val traceId: String? = null
)

data class RuntimeCapabilities(
    val supportsCancellation: Boolean,
    val supportsStreaming: Boolean,
    val supportsParallelCalls: Boolean
)

data class SourceModule(
    val id: String,
    val binary: ByteArray? = null,
    val script: String? = null
)

interface HostEnvironment {
    suspend fun http(request: RtValue): RtValue
    suspend fun storageGet(key: String): RtValue
    suspend fun storageSet(key: String, value: RtValue)
    fun log(level: String, message: String)
}

interface SourceRuntime {

    val capabilities: RuntimeCapabilities

    suspend fun load(module: SourceModule)

    suspend fun initialize(host: HostEnvironment)

    suspend fun execute(
        operation: SourceOperation,
        payload: RtValue,
        context: ExecutionContext = ExecutionContext()
    ): RtValue

    suspend fun close()
}

class RelayRuntime: SourceRuntime {
    override val capabilities = RuntimeCapabilities(false, false, false)

    override suspend fun load(module: SourceModule) {

    }

    override suspend fun initialize(host: HostEnvironment) {

    }

    override suspend fun execute(
        operation: SourceOperation,
        payload: RtValue,
        context: ExecutionContext
    ): RtValue {
        return RtValue.RtString("Executed.")
    }

    override suspend fun close() {

    }
}