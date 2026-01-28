package com.inumaki.chouten

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.inumaki.core.ui.theme.AppTheme

@Composable
fun SplashScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppTheme.colors.background),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Splash")
    }
}