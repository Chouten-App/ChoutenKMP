package com.inumaki.features.discover

import com.inumaki.core.ui.model.ViewModel
import dev.chouten.core.repository.DiscoverList
import dev.chouten.core.repository.FileStore
import dev.chouten.core.repository.ModuleManager
import dev.chouten.core.repository.Result
import dev.chouten.core.repository.Runtime
import dev.chouten.core.repository.SourceModule
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch

sealed interface DiscoverUiState {
    object Loading : DiscoverUiState
    data class Success(val items: List<DiscoverList>) : DiscoverUiState
    data class Error(val message: String) : DiscoverUiState
}

data class DiscoverItem(
    val id: String,
    val title: String
)

class DiscoverViewModel(
    private val runtime: Runtime,
    private val moduleManager: ModuleManager
) : ViewModel() {
    private val _state = MutableStateFlow<DiscoverUiState>(DiscoverUiState.Loading)
    val state: StateFlow<DiscoverUiState> = _state



    init {
        scope.launch {
           moduleManager.activeModule
                .filterNotNull()
                .collect {
                    println("Loading module")
                    val sourceModule = SourceModule(
                        it.id,
                        binary = FileStore.read(it.localPath)
                    )
                    println(sourceModule)
                    runtime.load(sourceModule)
                    setLoading()
                    discover()
                }
        }
    }

    fun discover() {
        println("Running discover")
        val data = runtime.discover()
        // TODO: Add error support
        when (data) {
           is Result.Ok -> setDiscoverData(data.value)
           is Result.Err -> setError(data.error.toString())
        }
    }

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
