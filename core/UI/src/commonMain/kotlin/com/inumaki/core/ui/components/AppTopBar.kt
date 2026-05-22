package com.inumaki.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.inumaki.core.ui.components.alert.AlertScope
import com.inumaki.core.ui.components.alert.alert
import com.inumaki.core.ui.model.TopBarConfig
import com.inumaki.core.ui.modifiers.applyIf
import com.inumaki.core.ui.modifiers.shiningBorder
import com.inumaki.core.ui.theme.AppTheme
import com.inumaki.core.ui.theme.LocalToolbarItems
import dev.chouten.core.repository.InstalledModule
import dev.chouten.core.repository.ModuleManager
import dev.chouten.core.repository.RemoteModule
import dev.chouten.core.repository.RepositoryManager
import kotlinx.coroutines.launch


@Composable
fun AppTopBar(topBarConfig: TopBarConfig?, angle: Float, repositoryManager: RepositoryManager, moduleManager: ModuleManager, modifier: Modifier = Modifier) {
    val scope = rememberCoroutineScope()
    var showAlert by remember { mutableStateOf(false) }
    var input by remember { mutableStateOf("") }

    val items = LocalToolbarItems.current

    var allModules by remember { mutableStateOf<List<Pair<InstalledModule?, RemoteModule>>>(emptyList()) }
    val activeModule by moduleManager.activeModule.collectAsState()

    LaunchedEffect(Unit) {
        repositoryManager.refreshRepositories()
        allModules = repositoryManager.getAllModules()
        println(allModules)
    }

    topBarConfig?.let { config ->
        val statusInsets = WindowInsets.statusBars.asPaddingValues()

        val topPadding = if (statusInsets.calculateTopPadding() > 44.dp)
            statusInsets.calculateTopPadding()
        else statusInsets.calculateTopPadding() + AppTheme.layout.screenEdgePadding.calculateTopPadding()

        Column(
            modifier = modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        0.0f to AppTheme.colors.background,
                        1.0f to AppTheme.colors.background.copy(0f),
                    )
                ),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = AppTheme.layout.screenEdgePadding.calculateLeftPadding(LayoutDirection.Ltr),
                        top = topPadding,
                        end = AppTheme.layout.screenEdgePadding.calculateRightPadding(LayoutDirection.Ltr)
                    )
                    .height(AppTheme.layout.iconSize.height),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Box(
                    modifier = Modifier
                        .width(AppTheme.layout.iconSize.width)
                )
                Text(
                    config.title,
                    style = AppTheme.typography.subheadline,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center
                )

                items.forEach { item ->
                    item()
                }
            }

            LazyRow(
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                itemsIndexed(allModules) { index, (local, remote) ->
                    local?.let {
                        val isSelected = activeModule?.id == local.id
                        Row(
                            modifier = Modifier
                                .shiningBorder(
                                    0f,
                                    50.dp,
                                    if(isSelected) Color.Transparent else AppTheme.colors.container,
                                    if(isSelected) Color.Transparent else AppTheme.colors.border
                                )
                                .clip(RoundedCornerShape(50))
                                .background(if(isSelected) AppTheme.colors.accent else AppTheme.colors.container)
                                .padding(horizontal = 10.dp, vertical = 8.dp)
                                .clickable {
                                    scope.launch {
                                        moduleManager.activateModule(
                                            it.id
                                        )
                                    }
                                },
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            AppAsyncImage(
                                it.imagePath,
                                modifier = Modifier
                                    .width(16.dp)
                                    .height(16.dp)
                                    .clip(CircleShape)
                            )

                            Text(remote.name)
                        }
                    }
                }
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