package com.inumaki.core.ui.components.alert

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.inumaki.core.ui.theme.AppTheme

data class ButtonSpec(
    val title: String,
    val role: AlertScope.Role?,
    val onClick: () -> Unit,
)

@Composable
fun AlertButton(
    spec: ButtonSpec,
    modifier: Modifier = Modifier
) {
    val color = when (spec.role) {
        AlertScope.Role.Destructive -> AppTheme.colors.error
        else -> AppTheme.colors.fg
    }
    Text(
        spec.title,
        style = TextStyle(
            fontWeight = FontWeight.SemiBold,
            color = color,
            fontSize = 16.sp
        ),
        textAlign = TextAlign.Center,
        modifier = modifier
            .padding(top = 8.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(50))
            .clickable {
                spec.onClick()
            }
            .background(AppTheme.colors.overlay)
            .padding(14.dp)
    )
}