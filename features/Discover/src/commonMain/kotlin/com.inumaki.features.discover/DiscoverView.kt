package com.inumaki.features.discover

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.inumaki.core.ui.MatchedElement
import com.inumaki.core.ui.components.AppImageButton
import com.inumaki.core.ui.model.GlobalState
import dev.chouten.core.repository.Runtime
import com.inumaki.core.ui.model.toolbar
import com.inumaki.core.ui.theme.AppTheme
import com.kyant.backdrop.backdrops.rememberLayerBackdrop

@Composable
fun DiscoverView(viewModel: DiscoverViewModel) {
    val state by viewModel.state.collectAsState()
    val angle by GlobalState.angle.collectAsState()

    val bg = AppTheme.colors.background

    val backdrop = rememberLayerBackdrop {
        drawRect(bg)
        drawContent()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .toolbar {
                MatchedElement(
                    "ModuleSelector",
                    modifier = Modifier,
                    alignment = Alignment.BottomStart,
                ) {
                    AppImageButton(
                        "https://raw.githubusercontent.com/celymyst/debug_repo/main/modules/weeb-central/icon.png",
                        angle,
                        44.dp,
                        44.dp
                    )
                }
            }
    ) {
        when (state) {
            is DiscoverUiState.Loading -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("Loading...")
                }
            }

            is DiscoverUiState.Success -> {
                DiscoverViewSuccess((state as DiscoverUiState.Success).items, angle, backdrop)
            }

            is DiscoverUiState.Error -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text((state as DiscoverUiState.Error).message)
                }
            }
        }
    }
}