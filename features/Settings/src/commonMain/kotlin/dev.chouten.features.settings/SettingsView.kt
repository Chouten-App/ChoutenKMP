package dev.chouten.features.settings

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.inumaki.core.ui.components.AppButton
import com.inumaki.core.ui.components.AppImage
import com.inumaki.core.ui.components.AppTintedButton
import com.inumaki.core.ui.components.AppToggle
import com.inumaki.core.ui.components.LiquidToggle
import com.inumaki.core.ui.model.AppConfig
import com.inumaki.core.ui.modifiers.reportScrollGate
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
    val listState = rememberLazyListState()

    val useBlur by viewModel.useBlur.collectAsState()
    val useLiquidGlass by viewModel.useLiquidGlass.collectAsState()
    val cliIP by viewModel.cliIP.collectAsState()

    var cliIPField by remember {
        mutableStateOf(TextFieldValue(cliIP))
    }

    val controller = AppTheme.controller

    var selected by rememberSaveable { mutableStateOf(false) }

    val barHeight = 48f


    val backgroundColor = AppTheme.colors.container
    val backdrop = rememberLayerBackdrop {
        drawRect(backgroundColor)
        drawContent()
    }

    LaunchedEffect(cliIP) {
        if (cliIP != cliIPField.text) {
            cliIPField = cliIPField.copy(text = cliIP)

        }
    }

    Box(
        modifier = modifier // <-- anchor to bottom
            .shiningBorder(0f, 38.dp)
            .background(AppTheme.colors.container)
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .reportScrollGate(listState)
                .fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 90.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(AppTheme.colors.overlay)
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AsyncImage(
                        "https://i.pinimg.com/1200x/7b/1d/dc/7b1ddcab5e7fccfb8a00ca680f4a24c3.jpg",
                        contentDescription = "pfp",
                        modifier = Modifier
                            .width(52.dp)
                            .clip(CircleShape)
                    )

                    Column {
                        Text("Inumaki", style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = AppTheme.colors.fg))
                        Text("Developer", style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Medium, color = AppTheme.colors.fg.copy(0.7f)))
                    }
                }
            }

            item {
                Text(
                    "General",
                    style = AppTheme.typography.caption1,
                    modifier = Modifier.padding(start = 20.dp, bottom = 2.dp)
                )
                Box {
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .clip(RoundedCornerShape(20.dp))
                            .background(AppTheme.colors.overlay)
                    )
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp)
                    ) {
                        SettingsLink("drawable/safari-solid-full.svg", "Appearance")

                        HorizontalDivider(
                            thickness = 1.dp,
                            color = AppTheme.colors.border,
                            modifier = Modifier.padding(vertical = 12.dp)
                        )

                        SettingsLink("drawable/safari-solid-full.svg", "Library")

                        HorizontalDivider(
                            thickness = 1.dp,
                            color = AppTheme.colors.border,
                            modifier = Modifier.padding(vertical = 12.dp)
                        )

                        SettingsLink("drawable/safari-solid-full.svg", "Reader")
                    }
                }
            }

            item {
                Text(
                    "Advanced",
                    style = AppTheme.typography.caption1,
                    modifier = Modifier.padding(start = 20.dp, bottom = 2.dp)
                )
                Box {
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .clip(RoundedCornerShape(20.dp))
                            .background(AppTheme.colors.overlay)
                    )
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp)
                    ) {
                        SettingsLink("drawable/safari-solid-full.svg", "Repositories")

                        HorizontalDivider(
                            thickness = 1.dp,
                            color = AppTheme.colors.border,
                            modifier = Modifier.padding(vertical = 12.dp)
                        )

                        SettingsLink("drawable/safari-solid-full.svg", "Developer")
                    }
                }
            }

            /*
            item {
                Text(
                    "Debug",
                    style = AppTheme.typography.caption1,
                    modifier = Modifier.padding(start = 20.dp, bottom = 2.dp)
                )
                Box {
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
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "CLI",
                                style = AppTheme.typography.subheadline,
                                fontWeight = FontWeight.Medium
                            )

                            BasicTextField(
                                cliIPField,
                                onValueChange = { newValue ->
                                    cliIPField = newValue
                                },
                                textStyle = AppTheme.typography.subheadline.copy(color = AppTheme.colors.fg),
                                singleLine = true,
                                cursorBrush = SolidColor(AppTheme.colors.accent),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(AppTheme.colors.border)
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }

                        Text(
                            "Connect",
                            color = AppTheme.colors.accent,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                                .clickable {
                                    viewModel.setChoutenCLI(cliIPField.text)
                                }
                        )
                    }
                }
            }

            item {
                Box(
                    modifier = Modifier.fillMaxWidth().height(2000.dp)
                )
            }
             */
        }

        /*
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

         */
    }
}

@Composable
fun SettingsLink(icon: String, title: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AppImage(
                icon,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(AppTheme.colors.border)
                    .padding(4.dp)
                    .width(20.dp)
            )

            Text(
                title,
                style = AppTheme.typography.subheadline,
                fontWeight = FontWeight.Medium
            )
        }

        AppImage(
            "drawable/chevron-right-solid-full.svg",
            modifier = Modifier
                .width(20.dp),
            color = AppTheme.colors.border
        )
    }
}