package com.inumaki.chouten.navigation.overlay

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.inumaki.chouten.dev.DevClientManager
import com.inumaki.core.ui.model.AppRoute
import com.inumaki.core.ui.model.DiscoverRoute
import com.inumaki.core.ui.model.HomeRoute
import com.inumaki.core.ui.model.NavigationScope
import com.inumaki.core.ui.model.RepoRoute
import com.inumaki.core.ui.model.Result
import com.inumaki.core.ui.model.ResultSerializer
import com.inumaki.core.ui.model.onOk
import com.inumaki.features.discover.DiscoverView
import com.inumaki.features.discover.DiscoverViewModel
import com.inumaki.features.discover.model.DiscoverList
import com.inumaki.features.home.HomeView
import com.inumaki.features.repo.RepoView
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

@Serializable(with = ChoutenErrorSerializer::class)
sealed class ChoutenError {
    @Serializable
    data class Network(
        val url: String,
        val message: String
    ) : ChoutenError()

    @Serializable
    data class HtmlParse(
        val selector: String,
        val message: String
    ) : ChoutenError()

    @Serializable
    data class Host(
        val function: String,
        val message: String
    ) : ChoutenError()

    @Serializable
    data class Module(
        val message: String
    ) : ChoutenError()
}

object ChoutenErrorSerializer : KSerializer<ChoutenError> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("ChoutenError")

    override fun deserialize(decoder: Decoder): ChoutenError {
        val jsonDecoder = decoder as JsonDecoder
        val element = jsonDecoder.decodeJsonElement().jsonObject

        return when {
            "Network" in element -> jsonDecoder.json.decodeFromJsonElement(
                ChoutenError.Network.serializer(),
                element["Network"]!!
            )
            "HtmlParse" in element -> jsonDecoder.json.decodeFromJsonElement(
                ChoutenError.HtmlParse.serializer(),
                element["HtmlParse"]!!
            )
            "Host" in element -> jsonDecoder.json.decodeFromJsonElement(
                ChoutenError.Host.serializer(),
                element["Host"]!!
            )
            "Module" in element -> jsonDecoder.json.decodeFromJsonElement(
                ChoutenError.Module.serializer(),
                element["Module"]!!
            )
            else -> throw SerializationException("Unknown ChoutenError variant: ${element.keys}")
        }
    }

    override fun serialize(encoder: Encoder, value: ChoutenError) {
        val jsonEncoder = encoder as JsonEncoder
        val (key, serializer, data) = when (value) {
            is ChoutenError.Network -> Triple("Network", ChoutenError.Network.serializer(), value)
            is ChoutenError.HtmlParse -> Triple("HtmlParse", ChoutenError.HtmlParse.serializer(), value)
            is ChoutenError.Host -> Triple("Host", ChoutenError.Host.serializer(), value)
            is ChoutenError.Module -> Triple("Module", ChoutenError.Module.serializer(), value)
        }

        @Suppress("UNCHECKED_CAST")
        val element = jsonEncoder.json.encodeToJsonElement(serializer as KSerializer<ChoutenError>, data)
        val wrapped = buildJsonObject { put(key, element) }
        jsonEncoder.encodeJsonElement(wrapped)
    }
}

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
    devClientManager: DevClientManager
) {
    when (route) {
        is DiscoverRoute -> {
            val viewModel = navScope.viewModelStore.get("discover") {
                DiscoverViewModel()
            }

            LaunchedEffect(Unit) {
                devClientManager.discoverResult.collect { json ->
                    if (json.isNullOrEmpty()) {
                        viewModel.setLoading()
                        return@collect
                    }
                    val serializer = ResultSerializer(
                        ListSerializer(DiscoverList.serializer()),
                        ChoutenErrorSerializer
                    )

                    val result: Result<List<DiscoverList>, ChoutenError> = Json.decodeFromString(serializer, json)

                    when (result) {
                        is Result.Ok -> viewModel.setDiscoverData(result.value)
                        is Result.Err -> {

                            viewModel.setError(result.error.toString())
                        }
                    }
                }
            }

            DiscoverView(viewModel)
        }

        is HomeRoute -> {
            HomeView()
        }

        is RepoRoute -> {
            RepoView()
        }

        else -> {
            // No fullscreen overlay for this route
        }
    }
}