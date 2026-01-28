package dev.chouten.features.settings

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.RenderEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.inumaki.core.ui.components.AppButton
import com.inumaki.core.ui.components.AppToggle
import com.inumaki.core.ui.components.LiquidToggle
import com.inumaki.core.ui.model.AppConfig
import com.inumaki.core.ui.modifiers.shiningBorder
import com.inumaki.core.ui.theme.AppTheme
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.drawPlainBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.effect
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.effects.vibrancy

@Composable
fun SettingsView(viewModel: SettingsViewModel, appConfig: AppConfig, modifier: Modifier = Modifier) {
    val useBlur by viewModel.useBlur.collectAsState()
    val useLiquidGlass by viewModel.useLiquidGlass.collectAsState()
    val controller = AppTheme.controller

    var selected by rememberSaveable { mutableStateOf(false) }

    val barHeight = 48f


    val backgroundColor = AppTheme.colors.container
    val backdrop = rememberLayerBackdrop {
        drawRect(backgroundColor)
        drawContent()
    }

    Box(
        modifier = modifier // <-- anchor to bottom
            .shiningBorder(0f, 40.dp)
            .background(AppTheme.colors.container)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 90.dp)
        ) {
            // Sections
            item {
                Text(
                    "General",
                    style = AppTheme.typography.caption1,
                    modifier = Modifier.padding(start = 20.dp, bottom = 2.dp)
                )
                Box(

                ) {
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .shiningBorder(0f, 20.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(AppTheme.colors.overlay)
                    )
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Use Progressive Blur",
                                style = AppTheme.typography.subheadline,
                                fontWeight = FontWeight.Medium
                            )

                            AppToggle(useBlur ?: false, onChange = { newValue ->
                                viewModel.setUseBlur(newValue)
                            }, backdrop, isLiquid = useLiquidGlass ?: false)
                        }

                        HorizontalDivider(
                            thickness = 1.dp,
                            color = AppTheme.colors.border,
                            modifier = Modifier.padding(vertical = 12.dp)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth().alpha(0.5f),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Use Liquid Glass",
                                style = AppTheme.typography.subheadline,
                                fontWeight = FontWeight.Medium
                            )


                            AppToggle(false, onChange = { newValue ->
                                // viewModel.setUseLiquidGlass(newValue)
                            }, backdrop, isLiquid = useLiquidGlass ?: false)
                        }
                    }
                }
            }

            item {
                Box(
                    modifier = Modifier.fillMaxWidth().height(2000.dp)
                )
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        0.0f to AppTheme.colors.container,
                        1.0f to Color(0x00FFFFFF and AppTheme.colors.container.toArgb()),
                        start = Offset(0f, 0f),
                        end = Offset(0f, 200f)
                    )
                )
                .padding(16.dp)
        ) {
            Text(
                "Settings",
                style = AppTheme.typography.body,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.align(Alignment.Center)
            )

            AppButton(
                "drawable/xmark-solid-full.svg",
                angle = 0f,
                background = AppTheme.colors.overlay,
                modifier = Modifier.align(Alignment.CenterEnd),
                onClick = {
                    controller.reverseTransition("settings_morph")
                    appConfig.navController.popBackStack()
                }
            )
        }
    }
}
