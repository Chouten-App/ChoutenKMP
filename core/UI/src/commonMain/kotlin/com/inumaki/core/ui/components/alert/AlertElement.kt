package com.inumaki.core.ui.components.alert

import androidx.compose.ui.node.ModifierNodeElement

data class AlertElement(
    val title: String,
    val isPresented: Boolean,
    val builder: AlertScope.() -> Unit,
    val manager: AlertManager
) : ModifierNodeElement<AlertNode>() {

    override fun create() = AlertNode(title, isPresented, builder, manager)

    override fun update(node: AlertNode) {
        node.title = title
        node.isPresented = isPresented
        node.builder = builder
        node.manager = manager
        node.update()
    }
}