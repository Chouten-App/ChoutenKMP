package com.inumaki.core.ui.modifiers

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import com.inumaki.core.ui.model.ScrollGate

fun Modifier.reportScrollGate(listState: LazyListState): Modifier =
    this.then(
        Modifier.onGloballyPositioned {
            val info = listState.layoutInfo

            val isEmpty = info.totalItemsCount == 0
            val atTop =
                listState.firstVisibleItemIndex == 0 &&
                        listState.firstVisibleItemScrollOffset == 0

            println("$atTop")
            ScrollGate.update(atTop, isEmpty)
        }
    )