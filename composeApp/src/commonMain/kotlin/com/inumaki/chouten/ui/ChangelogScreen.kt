package com.inumaki.chouten.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.inumaki.core.ui.components.AppTintedButton
import com.inumaki.core.ui.theme.AppTheme

@Composable
fun ChangelogScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppTheme.colors.background),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom
    ) {
        AppTintedButton("Continue", modifier = Modifier.padding(34.dp, 34.dp).fillMaxWidth())
    }
}
