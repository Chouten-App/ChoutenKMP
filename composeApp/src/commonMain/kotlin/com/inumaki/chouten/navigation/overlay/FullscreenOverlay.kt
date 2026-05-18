package com.inumaki.chouten.navigation.overlay

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.inumaki.chouten.dev.DevClientManager
import com.inumaki.core.ui.model.AppRoute
import com.inumaki.core.ui.model.ChoutenError
import com.inumaki.core.ui.model.ChoutenErrorSerializer
import com.inumaki.core.ui.model.DiscoverRoute
import com.inumaki.core.ui.model.HomeRoute
import com.inumaki.core.ui.model.NavigationScope
import com.inumaki.core.ui.model.RepoRoute
import com.inumaki.core.ui.model.Result
import com.inumaki.core.ui.model.ResultSerializer
import com.inumaki.core.ui.model.onOk
import com.inumaki.features.discover.DiscoverView
import com.inumaki.features.discover.DiscoverViewModel
import dev.chouten.core.repository.DiscoverList
import dev.chouten.core.repository.Runtime
import com.inumaki.features.home.HomeView
import com.inumaki.features.repo.RepoView
import dev.chouten.core.repository.RepositoryManager
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonContentPolymorphicSerializer
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonEncoder
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject



/**
 * Renders fullscreen overlays for specific routes.
 *
 * Fullscreen routes are displayed on top of the main navigation host
 * and typically represent the main screens of the app (Discover, Home, Repo).
 *
 * ViewModels are scoped to the navigation scope to survive configuration changes.
 */
@Composable
fun FullscreenOverlay(
    route: AppRoute,
    navScope: NavigationScope,
    devClientManager: DevClientManager,
    runtime: Runtime,
    repositoryManager: RepositoryManager
) {
    when (route) {
        is DiscoverRoute -> {
            val viewModel = navScope.viewModelStore.get("discover") {
                DiscoverViewModel(runtime)
            }

            DiscoverView(viewModel)
        }

        is HomeRoute -> {
            HomeView()
        }

        is RepoRoute -> {
            RepoView(
                repositoryManager
            )
        }

        else -> {
            // No fullscreen overlay for this route
        }
    }
}