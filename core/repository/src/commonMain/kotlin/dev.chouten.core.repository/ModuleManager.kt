package dev.chouten.core.repository

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ModuleManager(
    private val repositoryManager: RepositoryManager,
    private val runtimes: List<Runtime>  // injected via Koin, ordered by priority
) {
    private val _activeModule = MutableStateFlow<LoadedModule?>(null)
    val activeModule: StateFlow<LoadedModule?> = _activeModule.asStateFlow()

    private val moduleCache = mutableMapOf<String, LoadedModule>()

    suspend fun loadModule(moduleId: String): LoadedModule {
        moduleCache[moduleId]?.let { return it }

        val installed = repositoryManager.getInstalledModules()
            .mapNotNull { (local, _) -> local }
            .firstOrNull { it.id == moduleId }
            ?: error("Module $moduleId is not installed")

        val runtime = resolveRuntime(installed)
            ?: error("No runtime available for module $moduleId")

        val loaded = LoadedModule(
            id = installed.id,
            version = installed.version,
            runtime = runtime,
            localPath = installed.localPath,
            localImagePath = installed.imagePath
        )

        moduleCache[moduleId] = loaded
        return loaded
    }

    suspend fun activateModule(moduleId: String) {
        _activeModule.value = loadModule(moduleId)
    }

    fun deactivateModule() {
        _activeModule.value = null
    }

    suspend fun unloadModule(moduleId: String) {
        //moduleCache.remove(moduleId)?.runtime?.unload()
        if (_activeModule.value?.id == moduleId) deactivateModule()
    }

    fun getActiveRuntime(): Runtime? = _activeModule.value?.runtime

    private fun resolveRuntime(module: InstalledModule): Runtime? {
        return runtimes.firstOrNull { it.supports(module) }
    }
}

data class LoadedModule(
    val id: String,
    val version: String,
    val runtime: Runtime,
    val localPath: String,
    val localImagePath: String
)