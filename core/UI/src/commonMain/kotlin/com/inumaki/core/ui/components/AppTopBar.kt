package com.inumaki.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.inumaki.core.ui.components.alert.AlertScope
import com.inumaki.core.ui.components.alert.alert
import com.inumaki.core.ui.model.TopBarConfig
import com.inumaki.core.ui.theme.AppTheme
import com.inumaki.core.ui.theme.LocalToolbarItems


@Composable
fun AppTopBar(topBarConfig: TopBarConfig?, angle: Float, modifier: Modifier = Modifier) {
    var showAlert by remember { mutableStateOf(false) }
    var input by remember { mutableStateOf("") }

    val items = LocalToolbarItems.current

    topBarConfig?.let { config ->
        val statusInsets = WindowInsets.statusBars.asPaddingValues()

        val topPadding = if (statusInsets.calculateTopPadding() > 44.dp)
            statusInsets.calculateTopPadding()
        else statusInsets.calculateTopPadding() + AppTheme.layout.screenEdgePadding.calculateTopPadding()

        Row(
            modifier = modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        0.0f to AppTheme.colors.background,
                        1.0f to AppTheme.colors.background.copy(0f),
                    )
                )
                .padding(
                    start = AppTheme.layout.screenEdgePadding.calculateLeftPadding(LayoutDirection.Ltr),
                    top = topPadding,
                    end = AppTheme.layout.screenEdgePadding.calculateRightPadding(LayoutDirection.Ltr)
                )
                .height(AppTheme.layout.iconSize.height),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(config.title, style = AppTheme.typography.headline, fontWeight = FontWeight.SemiBold)

            items.forEach { item ->
                item()
            }

            /*
            config.actions.forEach { action ->
                AppButton(
                    "drawable/${action.icon}",
                    angle,
                    modifier = Modifier
                        .alert("Add Repository", isPresented = showAlert) {
                            message { "Enter the json URL of the repository" }

                            textField(
                                value = input,
                                onChange = { input = it },
                                placeholder = "https://sample.com/repo.json"
                            )

                            button("Cancel", role = AlertScope.Role.Cancel) {
                                showAlert = false
                            }
                            button("Add") {
                                println("Ok")
                                showAlert = false
                            }
                        }
                        .clickable {
                            showAlert = true
                        },
                )
            }

             */
        }
    }
}