package dev.chouten.runners.relay

import com.github.michaelbull.result.Err
import dev.chouten.core.repository.ChoutenError
import dev.chouten.core.repository.ChoutenErrorSerializer
import dev.chouten.core.repository.DiscoverList
import dev.chouten.core.repository.HostEnvironment
import dev.chouten.core.repository.HttpMethod
import dev.chouten.core.repository.PosterData
import dev.chouten.core.repository.Result
import dev.chouten.core.repository.ResultSerializer
import dev.chouten.core.repository.InstalledModule
import dev.chouten.core.repository.Runtime
import dev.chouten.core.repository.SourceModule
import io.github.charlietap.chasm.embedding.dsl.functionImport
import io.github.charlietap.chasm.embedding.instance
import io.github.charlietap.chasm.embedding.invoke
import io.github.charlietap.chasm.embedding.memory.readBytes
import io.github.charlietap.chasm.embedding.memory.readUtf8String
import io.github.charlietap.chasm.embedding.memory.writeBytes
import io.github.charlietap.chasm.embedding.memory.writeInt
import io.github.charlietap.chasm.embedding.module
import io.github.charlietap.chasm.embedding.shapes.Import
import io.github.charlietap.chasm.embedding.shapes.Instance
import io.github.charlietap.chasm.embedding.shapes.Memory
import io.github.charlietap.chasm.embedding.shapes.Wasm32Allocator
import io.github.charlietap.chasm.embedding.shapes.getOrElse
import io.github.charlietap.chasm.embedding.shapes.getOrNull
import io.github.charlietap.chasm.embedding.shapes.onError
import io.github.charlietap.chasm.embedding.store
import io.github.charlietap.chasm.runtime.value.NumberValue
import kotlinx.serialization.builtins.ListSerializer

class RelayRuntime : Runtime {
    val store = store()
    var instance: Instance? = null
    var hostEnvironment: HostEnvironment? = null

    lateinit var allocator: Wasm32Allocator

    override suspend fun load(module: SourceModule) {
        module.binary?.let { wasmBinary ->
            val wasmModule = module(wasmBinary).getOrNull() ?: run {
                println("RelayRuntime -> Failed to load WASM module")
                return
            }

            val imports = buildImports()
            val instantiationResult = instance(store, wasmModule, imports)
            instantiationResult.onError {
                println("RelayRuntime -> Instantiation failed: $it")
            }
            instance = instantiationResult.getOrNull() ?: return
            println("RelayRuntime -> Module loaded successfully")

            allocator = Wasm32Allocator(instance!!, store, "alloc", "free")
        }
    }

    private fun buildImports(): List<Import> = listOf(
        functionImport(store) {
            moduleName = "env"
            entityName = "log_host"
            type {
                params {
                    i32()
                    i32()
                }
                results {

                }
            }
            reference { args ->
                val ptr = (args[0] as NumberValue.I32).value
                val len = (args[1] as NumberValue.I32).value

                val memory = instance.exports.firstNotNullOf { it.value as? Memory }
                val string = readUtf8String(store, memory, ptr, len).getOrElse("")

                println("[Logs] $string")
                emptyList()
            }
        },
        functionImport(store) {
            moduleName = "env"
            entityName = "request_host"
            type {
                params {
                    i32()
                    i32()
                    i32()
                }
                results {
                    i32()
                }
            }
            reference { args ->
                val ptr = (args[0] as NumberValue.I32).value
                val len = (args[1] as NumberValue.I32).value
                val method = (args[2] as NumberValue.I32).value

                val memory = instance.exports.firstNotNullOf { it.value as? Memory }
                val url = readUtf8String(store, memory, ptr, len).getOrElse("")

                val id = hostEnvironment?.request(url, HttpMethod.fromInt(method)) ?: 0

                listOf(NumberValue.I32(id))
            }
        },
        functionImport(store) {
            moduleName = "env"
            entityName = "response_get_body_as_doc_host"
            type {
                params {
                    i32()
                }
                results {
                    i32()
                }
            }
            reference { args ->
                val id = (args[0] as NumberValue.I32).value
                val docId = hostEnvironment?.responseBodyAsDoc(id) ?: 0
                listOf(NumberValue.I32(docId))
            }
        },
        functionImport(store) {
            moduleName = "env"
            entityName = "html_query_selector_host"
            type {
                params {
                    i32()
                    i32()
                    i32()
                }
                results {
                    i32()
                }
            }
            reference { args ->
                val id = (args[0] as NumberValue.I32).value
                val ptr = (args[1] as NumberValue.I32).value
                val len = (args[2] as NumberValue.I32).value

                val memory = instance.exports.firstNotNullOf { it.value as? Memory }
                val selector = readUtf8String(store, memory, ptr, len).getOrElse("")

                val docId = hostEnvironment?.querySelector(id, selector) ?: 0
                listOf(NumberValue.I32(docId))
            }
        },
        functionImport(store) {
            moduleName = "env"
            entityName = "html_query_selector_all_host"

            type {
                params {
                    i32() // doc_id
                    i32() // sel_ptr
                    i32() // sel_len
                    i32() // out_len
                }
                results {
                    i32() // return: array_ptr
                }
            }

            reference { args ->
                val docId     = (args[0] as NumberValue.I32).value
                val selPtr    = (args[1] as NumberValue.I32).value
                val selLen    = (args[2] as NumberValue.I32).value
                val outLenPtr = (args[3] as NumberValue.I32).value

                val memory = instance.exports.firstNotNullOf { it.value as? Memory }
                val selector = readUtf8String(store, memory, selPtr, selLen).getOrElse("")

                val results = hostEnvironment?.querySelectorAll(docId, selector).orEmpty()
                val count = results.size


                println("[querySelectorAll] Allocating space for ids")
                val arrayPtr = allocator.alloc(count * 4)

                results.forEachIndexed { i, value ->
                    println("[querySelectorAll] Writing $value to ${arrayPtr + i * 4}")
                    writeInt(store, memory, arrayPtr + i * 4, value)
                }

                println("[querySelectorAll] Writing $count to ${outLenPtr}")
                writeInt(store, memory, outLenPtr, count)

                listOf(NumberValue.I32(arrayPtr))
            }
        },
        functionImport(store) {
            moduleName = "env"
            entityName = "html_node_query_selector_host"
            type {
                params {
                    i32()
                    i32()
                    i32()
                }
                results {
                    i32()
                }
            }
            reference { args ->
                val id = (args[0] as NumberValue.I32).value
                val ptr = (args[1] as NumberValue.I32).value
                val len = (args[2] as NumberValue.I32).value

                val memory = instance.exports.firstNotNullOf { it.value as? Memory }
                val selector = readUtf8String(store, memory, ptr, len).getOrElse("")

                val nodeId = hostEnvironment?.nodeQuerySelector(id, selector) ?: 0
                listOf(NumberValue.I32(nodeId))
            }
        },
        functionImport(store) {
            moduleName = "env"
            entityName = "html_node_text_host"
            type {
                params {
                    i32()
                    i32()
                }
                results {
                    i32()
                }
            }
            reference { args ->
                val id        = (args[0] as NumberValue.I32).value
                val outLenPtr = (args[1] as NumberValue.I32).value

                val memory = instance.exports.firstNotNullOf { it.value as? Memory }
                val text = hostEnvironment?.nodeText(id) ?: ""

                val bytes = text.encodeToByteArray()
                val len = bytes.size

                println("[nodeText] Allocating space for text")
                val strPtr = allocator.alloc(len)

                println("[nodeText] Writing $text to $strPtr")
                writeBytes(store, memory, strPtr, bytes)
                println("[nodeText] Writing $len to $outLenPtr")
                writeInt(store, memory, outLenPtr, len)

                listOf(NumberValue.I32(strPtr))
            }
        },
        functionImport(store) {
            moduleName = "env"
            entityName = "html_node_attr_host"
            type {
                params {
                    i32()
                    i32()
                    i32()
                    i32()
                }
                results {
                    i32()
                }
            }
            reference { args ->
                val id        = (args[0] as NumberValue.I32).value
                val attrPtr   = (args[1] as NumberValue.I32).value
                val attrLen   = (args[2] as NumberValue.I32).value
                val outLenPtr = (args[3] as NumberValue.I32).value

                val memory = instance.exports.firstNotNullOf { it.value as? Memory }
                val attr = readUtf8String(store, memory, attrPtr, attrLen).getOrNull() ?: ""

                val text = hostEnvironment?.nodeAttr(id, attr) ?: ""

                val bytes = text.encodeToByteArray()
                val len = bytes.size

                val strPtr = allocator.alloc(len)

                writeBytes(store, memory, strPtr, bytes)
                writeInt(store, memory, outLenPtr, len)

                listOf(NumberValue.I32(strPtr))
            }
        }
    )

    override suspend fun initialize(host: HostEnvironment?) {
        hostEnvironment = host
    }

    override fun discover(): Result<List<DiscoverList>, ChoutenError> {
        if (instance != null) {
            val result = invoke(store, instance!!, "discover_impl")

            result.getOrNull()?.let { values ->
                val structPtr = (values.first() as NumberValue.I32).value

                val jsonPtr = readInt(structPtr)
                val jsonLen = readInt(structPtr + 4)

                val bytes = readBytes(jsonPtr, jsonLen)
                val json = bytes.decodeToString()

                if (json.isEmpty()) {
                    return Result.Err(ChoutenError.Host("discover", "Json return value is empty"))
                }
                val serializer = ResultSerializer(
                    ListSerializer(DiscoverList.serializer()),
                    ChoutenErrorSerializer
                )

                val result: Result<List<DiscoverList>, ChoutenError> = kotlinx.serialization.json.Json.decodeFromString(serializer, json)

                return result
            }
        }
        return Result.Err(ChoutenError.Host("discover", "No instance was found."))
    }

    private fun readBytes(offset: Int, length: Int): ByteArray {
        val memory = instance!!.exports.firstNotNullOf { it.value as? Memory }
        val buffer = ByteArray(length)
        return readBytes(
            store = store,
            memory = memory,
            buffer = buffer,
            memoryPointer = offset,
            bytesToRead = length,
        ).getOrElse(ByteArray(0))
    }

    private fun readInt(offset: Int): Int {
        val bytes = readBytes(offset, 4)
        return (bytes[0].toInt() and 0xFF) or
                ((bytes[1].toInt() and 0xFF) shl 8) or
                ((bytes[2].toInt() and 0xFF) shl 16) or
                ((bytes[3].toInt() and 0xFF) shl 24)
    }

    override fun search(
        query: String,
        filters: List<String>
    ): List<PosterData> {
        TODO("Not yet implemented")
    }

    override fun supports(module: InstalledModule): Boolean {
        return module.localPath.contains(".wasm")
    }
}





/*
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

 */