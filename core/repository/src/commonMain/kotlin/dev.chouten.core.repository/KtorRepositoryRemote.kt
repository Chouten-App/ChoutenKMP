package dev.chouten.core.repository

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.contentType
import io.ktor.util.encodeBase64
import kotlinx.serialization.json.*
import kotlin.io.encoding.Base64

class KtorRepositoryRemote(
    private val client: HttpClient
) : RepositoryRemote {

    private val json = Json {
        ignoreUnknownKeys = true
    }

    override suspend fun fetchRepository(url: String): Repository {
        val response: String = client.get(url).body()

        val parsed = json.decodeFromString(RepoResponse.serializer(), response)
        val icon = downloadImage(parsed.iconUrl)
        val iconPath = saveImageToDisk(Base64.encode(url.encodeToByteArray()), icon)

        return Repository(
            url = url,
            lastUpdated = currentTimeMillis(),
            name = parsed.name,
            description = parsed.description,
            iconPath = iconPath,
            modules = parsed.modules.map {
                RemoteModule(
                    id = it.id,
                    name = it.name,
                    version = it.version,
                    description = it.description,
                    author = it.author.name,
                    iconUrl = it.icon,
                    manifestUrl = it.downloads.manifest,
                    wasmUrl = it.downloads.wasm
                )
            }
        )
    }

    override suspend fun downloadModule(url: String): ByteArray {
        return client.get(url).body()
    }

    override suspend fun downloadImage(url: String): ByteArray {
        val response = httpClient.get(url)
        val contentType = response.contentType()?.toString() ?: ""
        require(contentType.startsWith("image/")) {
            "Expected image content type, got $contentType for $url"
        }
        return response.body()
    }

    fun detectImageExtension(bytes: ByteArray): String = when {
        bytes.size >= 4 &&
                bytes[0] == 0x89.toByte() &&
                bytes[1] == 0x50.toByte() &&
                bytes[2] == 0x4E.toByte() &&
                bytes[3] == 0x47.toByte() -> "png"

        bytes.size >= 3 &&
                bytes[0] == 0xFF.toByte() &&
                bytes[1] == 0xD8.toByte() &&
                bytes[2] == 0xFF.toByte() -> "jpg"

        bytes.size >= 4 &&
                bytes[0] == 0x52.toByte() &&
                bytes[1] == 0x49.toByte() &&
                bytes[2] == 0x46.toByte() &&
                bytes[3] == 0x46.toByte() -> "webp"

        else -> "png" // safe fallback
    }

    suspend fun saveImageToDisk(repoId: String, bytes: ByteArray): String {
        val ext = detectImageExtension(bytes)
        val dir = "/repositories/$repoId/"
        val final = "${dir}icon.$ext"
        FileStore.createDirectories(dir)
        FileStore.write(final, bytes)
        return final
    }
}