package com.inumaki.features.discover

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.inumaki.core.ui.model.GlobalState

@Composable
fun DiscoverView(viewModel: DiscoverViewModel) {
    val state by viewModel.state.collectAsState()
    val angle by GlobalState.angle.collectAsState()

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
            DiscoverViewSuccess((state as DiscoverUiState.Success).items, angle)
        }
        is DiscoverUiState.Error -> {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("Error")
            }
        }
    }
}