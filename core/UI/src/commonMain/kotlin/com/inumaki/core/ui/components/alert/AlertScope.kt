package com.inumaki.core.ui.components.alert

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import com.inumaki.core.ui.theme.AppTheme

@DslMarker
annotation class AlertDsl

@AlertDsl
class AlertScope {

    internal val content = mutableListOf<@Composable () -> Unit>()
    internal val buttons = mutableListOf<ButtonSpec>()

    fun message(content: () -> String) {
        this.content += {
            Text(
                content(),
                style = TextStyle(color = AppTheme.colors.fg.copy(0.7f)),
                modifier = Modifier
                    .padding(top = 4.dp)
                    .padding(horizontal = 10.dp)
            )
        }
    }

    fun textField(
        value: String,
        onChange: (String) -> Unit,
        placeholder: String
    ) {
        content += {
            BasicTextField(
                value = value,
                onValueChange = onChange,
                cursorBrush = SolidColor(AppTheme.colors.accent),
                textStyle = TextStyle(color = AppTheme.colors.fg),
                decorationBox = { inner ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(50))
                            .background(AppTheme.colors.overlay)
                            .padding(12.dp)
                    ) {
                        inner()
                        if (value.isEmpty()) {
                            Text(
                                placeholder,
                                style = TextStyle(color = AppTheme.colors.fg.copy(0.7f))
                            )
                        }
                    }
                },
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }

    fun button(
        title: String,
        role: Role? = null,
        onClick: () -> Unit
    ) {
        buttons += ButtonSpec(title, role, onClick)
    }

    enum class Role { Cancel, Destructive }
}