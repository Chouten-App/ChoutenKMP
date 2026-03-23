package com.inumaki.features.discover

import com.inumaki.core.ui.model.ViewModel
import com.inumaki.features.discover.model.DiscoverList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

sealed interface DiscoverUiState {
    object Loading : DiscoverUiState
    data class Success(val items: List<DiscoverList>) : DiscoverUiState
    data class Error(val message: String) : DiscoverUiState
}

data class DiscoverItem(
    val id: String,
    val title: String
)

class DiscoverViewModel : ViewModel() {
    private val _state = MutableStateFlow<DiscoverUiState>(DiscoverUiState.Loading)
    val state: StateFlow<DiscoverUiState> = _state

    fun setDiscoverData(data: List<DiscoverList>) {
        _state.value = DiscoverUiState.Success(data)
    }

    fun setError(message: String) {
        _state.value = DiscoverUiState.Error(message)
    }

    fun setLoading() {
        _state.value = DiscoverUiState.Loading
    }
}
