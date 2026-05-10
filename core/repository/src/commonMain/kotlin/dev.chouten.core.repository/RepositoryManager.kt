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
        println("[RepositoryManager] Updated repos -> $updated")
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

    suspend fun getInstalledModules() = getAllModules().filter { (local, _) -> local != null }
    suspend fun getUpdatableModules() = getAllModules().filter { (local, remote) ->
        local != null && local.version != remote.version
    }

    suspend fun getAllModules(): List<Pair<InstalledModule?, RemoteModule>> {
        val installed = storage.getInstalledModules()
        val repos = storage.getRepositories()
        println("[RepositoryManager] repos -> $repos")
        val installedMap = installed.associateBy { it.id }

        return repos.flatMap { it.modules }.map { remote ->
            installedMap[remote.id] to remote  // InstalledModule? — null means not installed
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
