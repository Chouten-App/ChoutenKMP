package dev.chouten.core.repository

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Repository(
    val url: String,
    val lastUpdated: Long,
    val name: String,
    val description: String,
    val iconPath: String,
    val modules: List<RemoteModule>
)

@Serializable
data class RemoteModule(
    val id: String,
    val name: String,
    val version: String,
    val description: String,
    val author: String,
    val iconUrl: String? = null,
    val manifestUrl: String,
    val wasmUrl: String,
)

@Serializable
data class InstalledModule(
    val id: String,
    val version: String,
    val localPath: String,
    val imagePath: String,
    val sourceRepo: String,
)

@Serializable
data class RepoResponse(
    val name: String,
    val description: String,
    val iconUrl: String,
    val modules: List<ModuleDto>
)

@Serializable
data class ModuleDto(
    val id: String,
    val name: String,
    val version: String,
    val description: String,
    val author: AuthorDto,
    val license: String? = null,
    val homepage: String? = null,
    val icon: String? = null,
    val downloads: DownloadsDto,
    val capabilities: List<String> = emptyList(),
    val metadata: MetadataDto? = null,
    val changelog: Map<String, String> = emptyMap()
)

@Serializable
data class AuthorDto(
    val name: String,
    val url: String? = null
)

@Serializable
data class DownloadsDto(
    val wasm: String,
    val manifest: String
)

@Serializable
data class MetadataDto(
    val category: String? = null,
    val tags: List<String> = emptyList(),
    val language: String? = null
)

interface RepositoryStorage {
    suspend fun getRepositories(): List<Repository>
    suspend fun saveRepositories(repos: List<Repository>)

    suspend fun getInstalledModules(): List<InstalledModule>
    suspend fun saveInstalledModules(modules: List<InstalledModule>)
}

interface RepositoryRemote {
    suspend fun fetchRepository(url: String): Repository
    suspend fun downloadModule(url: String): ByteArray
    suspend fun downloadImage(url: String): ByteArray
}

interface ModuleSource {
    suspend fun list(): List<RemoteModule>
}