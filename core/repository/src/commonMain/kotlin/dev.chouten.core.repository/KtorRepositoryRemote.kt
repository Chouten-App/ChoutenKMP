package dev.chouten.core.repository

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.contentType
import kotlinx.serialization.json.*

class KtorRepositoryRemote(
    private val client: HttpClient
) : RepositoryRemote {

    private val json = Json {
        ignoreUnknownKeys = true
    }

    override suspend fun fetchRepository(url: String): Repository {
        val response: String = client.get(url).body()

        val parsed = json.decodeFromString(RepoResponse.serializer(), response)

        return Repository(
            url = url,
            lastUpdated = currentTimeMillis(),
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
}