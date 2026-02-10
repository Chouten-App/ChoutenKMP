package dev.chouten.core.repository

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class RepositoryDetails(
    val id: String,
    val name: String,
    val description: String?,
    val baseUrl: String,
    val modules: List<ModuleDetails>,
    val freshnessPolicy: FreshnessPolicy,
    val lastFetched: Float
)

sealed class FreshnessPolicy {
    data class Timed(val intervalMs: Long) : FreshnessPolicy()
    object AlwaysFresh : FreshnessPolicy()        // dev repo
    object ManualOnly : FreshnessPolicy()         // future signed repo
}

data class ModuleDetails(
    val id: String,
    val name: String,
    val description: String?,
    val version: String,
)

object RepositoryManager {
    var repos: List<RepositoryDetails> = emptyList()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    fun startPolling() {
        scope.launch {
            while (isActive) {
                refreshRepositories()
                delay(5_000) // base tick
            }
        }
    }

    fun getDetails(id: String): RepositoryDetails? {
        return repos.firstOrNull { it.id == id }
    }

    fun fetchDetails(url: String, force: Boolean = false): RepositoryDetails? {
        return null
    }

    fun refreshRepositories() {
        val now = System.currentTimeMillis()

        repos.forEach { repo ->
            when (val policy = repo.freshnessPolicy) {

                is FreshnessPolicy.AlwaysFresh -> {
                    fetchDetails(repo.baseUrl, force = true)
                }

                is FreshnessPolicy.Timed -> {
                    if (now - repo.lastFetched >= policy.intervalMs) {
                        fetchDetails(repo.baseUrl)
                    }
                }

                FreshnessPolicy.ManualOnly -> Unit
            }
        }
    }


    fun addRepo(url: String) {

    }
    fun removeRepo(id: String) {}
}