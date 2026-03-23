package com.inumaki.core.ui.model

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel

abstract class ViewModel {
    protected val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    open fun onCleared() {
        scope.cancel()
    }
}

class ViewModelStore {
    private val map = mutableMapOf<String, ViewModel>()

    fun <T : ViewModel> get(key: String, factory: () -> T): T {
        return map.getOrPut(key, factory) as T
    }

    fun clear(key: String) {
        map.remove(key)?.onCleared()
    }
}

class NavigationScope {
    val viewModelStore = ViewModelStore()
}