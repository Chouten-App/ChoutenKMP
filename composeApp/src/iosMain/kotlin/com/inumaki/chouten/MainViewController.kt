package com.inumaki.chouten

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.window.ComposeUIViewController
import com.inumaki.core.ui.createDataStore
import dev.chouten.runners.relay.NativeBridge
import dev.chouten.runners.relay.RelayLogger
import org.jetbrains.compose.resources.ExperimentalResourceApi
import chouten.composeapp.generated.resources.Res

@OptIn(ExperimentalResourceApi::class)
fun MainViewController() = ComposeUIViewController {
    val headingProvider = remember { GyroProvider() }

    LaunchedEffect(Unit) {
        NativeBridge.initLogger(RelayLogger)
        val wasmBytes = Res.readBytes("files/add.wasm")
        NativeBridge.load(wasmBytes)
        val result = NativeBridge.add(7, 5)
        println("RelayTest: 7 + 5 = $result")
    }

    App(headingProvider, createDataStore())
}