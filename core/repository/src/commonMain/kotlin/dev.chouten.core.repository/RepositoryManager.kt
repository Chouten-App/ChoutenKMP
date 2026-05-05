package dev.chouten.core.repository


class RepositoryManager(
    private val storage: RepositoryStorage,
    private val remote: RepositoryRemote
) {
    suspend fun addRepository(url: String) {
        val repo = remote.fetchRepository(url)
        val current = storage.getRepositories()
        storage.saveRepositories(current + repo)
    }

    suspend fun removeRepository(url: String) {
        val updated = storage.getRepositories().filterNot { it.url == url }
        storage.saveRepositories(updated)
    }

    suspend fun refreshRepositories() {
        val repos = storage.getRepositories()

        val updated = repos.map { repo ->
            remote.fetchRepository(repo.url)
                .copy(lastUpdated = currentTimeMillis())
        }
        storage.saveRepositories(updated)
    }


    suspend fun installModule(moduleId: String) {
        val repos = storage.getRepositories()

        val module = repos
            .flatMap { it.modules }
            .first { it.id == moduleId }

        val wasm = remote.downloadModule(module.wasmUrl)

        val path = saveToDisk(module.id, module.version, wasm)

        val installed = storage.getInstalledModules()

        storage.saveInstalledModules(
            installed + InstalledModule(
                id = module.id,
                version = module.version,
                localPath = path,
                sourceRepo = findRepoOf(moduleId, repos)
            )
        )
    }

    suspend fun removeModule(moduleId: String) {
        val installed = storage.getInstalledModules()
        val target = installed.first { it.id == moduleId }

        deleteFromDisk(target.localPath)

        storage.saveInstalledModules(
            installed.filterNot { it.id == moduleId }
        )
    }

    suspend fun getUpdatableModules(): List<Pair<InstalledModule, RemoteModule>> {
        val installed = storage.getInstalledModules()
        val repos = storage.getRepositories()

        val remoteMap = repos.flatMap { it.modules }
            .associateBy { it.id }

        return installed.mapNotNull { local ->
            val remote = remoteMap[local.id] ?: return@mapNotNull null

            if (remote.version != local.version) {
                local to remote
            } else null
        }
    }

    suspend fun updateModule(moduleId: String) {
        removeModule(moduleId)
        installModule(moduleId)
    }


    /// Helper functions
    val base = ""
    fun findRepoOf(moduleId: String, repos: List<Repository>): String {
        return repos.firstOrNull { repo ->
            repo.modules.any { it.id == moduleId }
        }?.url ?: error("Module not found")
    }

    suspend fun saveToDisk(moduleId: String, version: String, bytes: ByteArray): String {
        val dir = "$base/modules/${moduleId}/$version/"
        val final = "$dir/artifact"

        FileStore.createDirectories(dir)
        FileStore.write(final, bytes)

        return final
    }

    suspend fun deleteFromDisk(path: String) {
        FileStore.delete(path)
    }
}
