package dev.chouten.core.repository

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Repository(
    val url: String,
    val lastUpdated: Long,
    val modules: List<RemoteModule>
)

@Serializable
data class RemoteModule(
    val id: String,
    val name: String,
    val version: String,
    val manifestUrl: String,
    val wasmUrl: String
)

@Serializable
data class InstalledModule(
    val id: String,
    val version: String,
    val localPath: String,
    val sourceRepo: String
)

@Serializable
data class RepoResponse(
    val modules: List<ModuleDto>
)

@Serializable
data class ModuleDto(
    val id: String,
    val name: String,
    val version: String,
    val downloads: DownloadsDto
)

@Serializable
data class DownloadsDto(
    val wasm: String,
    val manifest: String
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
}

interface ModuleSource {
    suspend fun list(): List<RemoteModule>
}