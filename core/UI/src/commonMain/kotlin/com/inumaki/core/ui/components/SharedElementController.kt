package com.inumaki.core.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.InternalComposeApi
import androidx.compose.runtime.MovableContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Rect

class SharedElementController {

    var active by mutableStateOf<Transition?>(null)
        private set

    val animatingKeys = mutableStateListOf<String>()

    private var pendingKey by mutableStateOf<String?>(null)

    private var navId by mutableStateOf(0)
    private val lastNavHandledPerKey = mutableMapOf<String, Int>()

    private val startRects = mutableMapOf<String, Rect>()
    private val startContents = mutableMapOf<String, @Composable (() -> Unit)>()

    private val endRects = mutableMapOf<String, Rect>()
    private val endContents = mutableMapOf<String, @Composable (() -> Unit)>()

    fun registerStart(key: String, rect: Rect, content: @Composable (() -> Unit)) {
        startRects[key] = rect
        startContents[key] = content
    }

    fun registerEnd(key: String, rect: Rect, content: @Composable (() -> Unit)) {
        endRects[key] = rect
        endContents[key] = content

        val pending = pendingKey ?: return
        if (pending != key) return
        if (active != null) return

        // 🔒 session lock — prevents second fire
        if (lastNavHandledPerKey[key] == navId) return
        lastNavHandledPerKey[key] = navId

        val startRect = startRects[key] ?: return
        val startContent = startContents[key] ?: return
        val endRect = endRects[key] ?: return
        val endContent = endContents[key] ?: return

        active = Transition(
            key = key,
            start = startRect,
            end = endRect,
            startContent = startContent,
            endContent = endContent
        )
    }

    fun startTransition(key: String) {
        navId++                     // new navigation session
        pendingKey = key

        if (!animatingKeys.contains(key)) {
            animatingKeys.add(key)
        }
    }

    fun finish(key: String) {
        animatingKeys.remove(key)
        if (active?.key == key) active = null
    }

    /**
     * Starts a reverse transition for the given key.
     * Reuses the current Transition, swapping start and end.
     */
    fun reverseTransition(key: String) {
        navId++                     // new navigation session
        pendingKey = key

        if (!animatingKeys.contains(key)) {
            animatingKeys.add(key)
        }

        val pending = pendingKey ?: return
        if (pending != key) return
        if (active != null) return

        // 🔒 session lock — prevents second fire
        if (lastNavHandledPerKey[key] == navId) return
        lastNavHandledPerKey[key] = navId

        val startRect = endRects[key] ?: return
        val startContent = endContents[key] ?: return
        val endRect = startRects[key] ?: return
        val endContent = startContents[key] ?: return

        active = Transition(
            key = key,
            start = startRect,
            end = endRect,
            startContent = startContent,
            endContent = endContent
        )
    }
}


data class Transition(
    val key: String,
    val start: Rect,
    var end: Rect?,
    val startContent: @Composable (() -> Unit),
    val endContent: @Composable (() -> Unit)?,
)
