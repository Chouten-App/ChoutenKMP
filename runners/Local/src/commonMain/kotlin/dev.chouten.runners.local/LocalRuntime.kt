package dev.chouten.runners.local

import com.inumaki.core.ui.model.DiscoverList
import com.inumaki.core.ui.model.HostEnvironment
import com.inumaki.core.ui.model.PosterData
import com.inumaki.core.ui.model.Runtime
import com.inumaki.core.ui.model.SourceModule

class LocalRuntime: Runtime {
    override suspend fun load(module: SourceModule) {
        // Nothing to do
    }

    override suspend fun initialize(host: HostEnvironment?) {
        // Nothing to do
    }

    override fun discover(): List<DiscoverList> {
        // Get local files
        return listOf(
            DiscoverList(
                title = "Local files",
                section_type = "GRID",
                list = emptyList()
            ),
        )
    }

    override fun search(
        query: String,
        filters: List<String>
    ): List<PosterData> {
        TODO("Not yet implemented")
    }

}