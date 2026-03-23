package com.inumaki.core.ui.model

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder


@Serializable
sealed interface Result<out T, out E> {
    @Serializable
    @SerialName("Ok")
    data class Ok<T>(val value: T) : Result<T, Nothing>

    @Serializable
    @SerialName("Err")
    data class Err<E>(val error: E) : Result<Nothing, E>
}

class ResultSerializer<T, E>(
    private val okSerializer: KSerializer<T>,
    private val errSerializer: KSerializer<E>
) : KSerializer<Result<T, E>> {

    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("Result") {
        element("Ok", okSerializer.descriptor, isOptional = true)
        element("Err", errSerializer.descriptor, isOptional = true)
    }

    override fun serialize(encoder: Encoder, value: Result<T, E>) {
        val composite = encoder.beginStructure(descriptor)
        when (value) {
            is Result.Ok -> composite.encodeSerializableElement(descriptor, 0, okSerializer, value.value)
            is Result.Err -> composite.encodeSerializableElement(descriptor, 1, errSerializer, value.error)
        }
        composite.endStructure(descriptor)
    }

    override fun deserialize(decoder: Decoder): Result<T, E> {
        val composite = decoder.beginStructure(descriptor)
        var result: Result<T, E>? = null
        loop@ while (true) {
            when (val index = composite.decodeElementIndex(descriptor)) {
                0 -> result = Result.Ok(composite.decodeSerializableElement(descriptor, 0, okSerializer))
                1 -> result = Result.Err(composite.decodeSerializableElement(descriptor, 1, errSerializer))
                CompositeDecoder.DECODE_DONE -> break@loop
            }
        }
        composite.endStructure(descriptor)
        return result ?: throw SerializationException("Neither Ok nor Err found")
    }
}

inline fun <T, E> Result<T, E>.map(transform: (T) -> T): Result<T, E> =
    when (this) {
        is Result.Ok -> Result.Ok(transform(value))
        is Result.Err -> this
    }

inline fun <T, E> Result<T, E>.mapErr(transform: (E) -> E): Result<T, E> =
    when (this) {
        is Result.Ok -> this
        is Result.Err -> Result.Err(transform(error))
    }

inline fun <T, E> Result<T, E>.getOrElse(fallback: (E) -> T): T =
    when (this) {
        is Result.Ok -> value
        is Result.Err -> fallback(error)
    }

fun <T, E> Result<T, E>.getOrNull(): T? =
    when (this) {
        is Result.Ok -> value
        is Result.Err -> null
    }

inline fun <T, E> Result<T, E>.onOk(block: (T) -> Unit): Result<T, E> =
    also { if (it is Result.Ok) block(it.value) }

inline fun <T, E> Result<T, E>.onErr(block: (E) -> Unit): Result<T, E> =
    also { if (it is Result.Err) block(it.error) }