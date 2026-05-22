package dev.chouten.core.repository

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonEncoder
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import kotlin.collections.contains


@Serializable(with = ChoutenErrorSerializer::class)
sealed class ChoutenError {
    @Serializable
    data class Network(
        val url: String,
        val message: String
    ) : ChoutenError()

    @Serializable
    data class HtmlParse(
        val selector: String,
        val message: String
    ) : ChoutenError()

    @Serializable
    data class Host(
        val function: String,
        val message: String
    ) : ChoutenError()

    @Serializable
    data class Module(
        val message: String
    ) : ChoutenError()
}

object ChoutenErrorSerializer : KSerializer<ChoutenError> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("ChoutenError")

    override fun deserialize(decoder: Decoder): ChoutenError {
        val jsonDecoder = decoder as JsonDecoder
        val element = jsonDecoder.decodeJsonElement().jsonObject

        return when {
            "Network" in element -> jsonDecoder.json.decodeFromJsonElement(
                ChoutenError.Network.serializer(),
                element["Network"]!!
            )
            "HtmlParse" in element -> jsonDecoder.json.decodeFromJsonElement(
                ChoutenError.HtmlParse.serializer(),
                element["HtmlParse"]!!
            )
            "Host" in element -> jsonDecoder.json.decodeFromJsonElement(
                ChoutenError.Host.serializer(),
                element["Host"]!!
            )
            "Module" in element -> jsonDecoder.json.decodeFromJsonElement(
                ChoutenError.Module.serializer(),
                element["Module"]!!
            )
            else -> throw SerializationException("Unknown ChoutenError variant: ${element.keys}")
        }
    }

    override fun serialize(encoder: Encoder, value: ChoutenError) {
        val jsonEncoder = encoder as JsonEncoder
        val (key, serializer, data) = when (value) {
            is ChoutenError.Network -> Triple("Network", ChoutenError.Network.serializer(), value)
            is ChoutenError.HtmlParse -> Triple("HtmlParse", ChoutenError.HtmlParse.serializer(), value)
            is ChoutenError.Host -> Triple("Host", ChoutenError.Host.serializer(), value)
            is ChoutenError.Module -> Triple("Module", ChoutenError.Module.serializer(), value)
        }

        @Suppress("UNCHECKED_CAST")
        val element = jsonEncoder.json.encodeToJsonElement(serializer as KSerializer<ChoutenError>, data)
        val wrapped = buildJsonObject { put(key, element) }
        jsonEncoder.encodeJsonElement(wrapped)
    }
}
