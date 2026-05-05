package com.inumaki.core.ui.components.alert

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.inumaki.core.ui.theme.LocalAlertManager

@Composable
fun Modifier.alert(
    title: String,
    isPresented: Boolean,
    content: AlertScope.() -> Unit
): Modifier {
    val manager = LocalAlertManager.current

    return this.then(
        AlertElement(title, isPresented, content, manager)
    )
}