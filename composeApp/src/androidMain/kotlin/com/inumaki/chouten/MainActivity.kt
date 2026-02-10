package com.inumaki.chouten

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.inumaki.core.ui.createDataStore
import dev.chouten.runners.relay.NativeBridge
import dev.chouten.runners.relay.RelayLogger

class MainActivity : ComponentActivity() {
    private lateinit var headingProvider: GyroProvider

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        headingProvider = GyroProvider(this)

        setContent {
            App(headingProvider, createDataStore(applicationContext))
        }
    }

    override fun onStart() {
        super.onStart()
        headingProvider.start()
    }

    override fun onStop() {
        super.onStop()
        headingProvider.stop()
    }
}