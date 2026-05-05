package dev.chouten.core.repository

expect fun currentTimeMillis(): Long

expect object FileStore {
    suspend fun write(
        path: String,
        bytes: ByteArray
    )

    suspend fun read(
        path: String
    ): ByteArray?

    suspend fun delete(path: String)

    suspend fun exists(path: String): Boolean

    suspend fun createDirectories(path: String)

    suspend fun move(from: String, to: String)
}