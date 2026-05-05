package dev.chouten.core.repository

import kotlinx.serialization.*
import kotlinx.serialization.json.*


@Serializable
data class StorageModel(
    val repositories: List<Repository> = emptyList(),
    val installed: List<InstalledModule> = emptyList()
)


class FileRepositoryStorage(
    private val basePath: String
) : RepositoryStorage {

    private val json = Json {
        ignoreUnknownKeys = true
        prettyPrint = false
    }

    private val filePath = "$basePath/repos.json"

    override suspend fun getRepositories(): List<Repository> {
        return read().repositories
    }

    override suspend fun saveRepositories(repos: List<Repository>) {
        val current = read()
        write(current.copy(repositories = repos))
    }

    override suspend fun getInstalledModules(): List<InstalledModule> {
        return read().installed
    }

    override suspend fun saveInstalledModules(modules: List<InstalledModule>) {
        val current = read()
        write(current.copy(installed = modules))
    }

    private suspend fun read(): StorageModel {
        if (!FileStore.exists(filePath)) {
            return StorageModel()
        }

        val bytes = readBytes(filePath)
        return json.decodeFromString(StorageModel.serializer(), bytes?.decodeToString() ?: "")
    }

    private suspend fun write(model: StorageModel) {
        val tmp = "$filePath.tmp"
        val data = json.encodeToString(StorageModel.serializer(), model).encodeToByteArray()

        FileStore.write(tmp, data)
        FileStore.move(tmp, filePath)
    }

    // minimal helper (same abstraction boundary)
    private suspend fun readBytes(path: String): ByteArray? {
        return FileStore.read(path)
    }
}