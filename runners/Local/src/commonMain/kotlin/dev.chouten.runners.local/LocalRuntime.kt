package dev.chouten.runners.local

import dev.chouten.core.repository.ChoutenError
import dev.chouten.core.repository.DiscoverList
import dev.chouten.core.repository.HostEnvironment
import dev.chouten.core.repository.InstalledModule
import dev.chouten.core.repository.PosterData
import dev.chouten.core.repository.Result
import dev.chouten.core.repository.Runtime
import dev.chouten.core.repository.SourceModule

class LocalRuntime: Runtime {
    override suspend fun load(module: SourceModule) {
        // Nothing to do
    }

    override suspend fun initialize(host: HostEnvironment?) {
        // Nothing to do
    }

    override fun discover(): Result<List<DiscoverList>, ChoutenError> {
        // Get local files
        return Result.Ok(listOf(
                DiscoverList(
                    title = "Local files",
                    section_type = "GRID",
                    list = emptyList()
                )
            )
        )
    }

    override fun search(
        query: String,
        filters: List<String>
    ): List<PosterData> {
        TODO("Not yet implemented")
    }

    override fun supports(module: InstalledModule): Boolean {
        return false
    }

}