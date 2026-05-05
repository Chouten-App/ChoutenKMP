package com.inumaki.core.ui.components.alert

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.inumaki.core.ui.theme.LocalAlertManager
import kotlin.time.Clock

class AlertNode(
    var title: String,
    var isPresented: Boolean,
    var builder: AlertScope.() -> Unit,
    var manager: AlertManager
) : Modifier.Node() {
    override fun onAttach() = update()

    override fun onDetach() {
        manager.dismiss(title)
    }

    fun update() {
        if (isPresented) {
            manager.present(
                AlertData(title) {
                    val scope = AlertScope().apply(builder)
                    val useHorizontal =
                        scope.buttons.size == 2

                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        scope.content.forEach { it() }

                        if (scope.buttons.size >= 3 || !useHorizontal) {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                scope.buttons.forEach { AlertButton(it) }
                            }
                        } else {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                scope.buttons.forEach { AlertButton(it, Modifier.weight(1f)) }
                            }
                        }
                    }
                }
            )
        } else {
            manager.dismiss(title)
        }
    }
}