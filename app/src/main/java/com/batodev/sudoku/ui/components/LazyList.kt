package com.batodev.sudoku.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Divider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.batodev.sudoku.ui.util.drawVerticalScrollbar
import com.batodev.sudoku.ui.util.isScrolledToEnd
import com.batodev.sudoku.ui.util.isScrolledToStart

@Composable
fun ScrollbarLazyColumn(
    modifier: Modifier = Modifier,
    state: LazyListState = rememberLazyListState(),
    behavior: LazyColumnBehavior = LazyColumnBehavior(),
    content: LazyListScope.() -> Unit,
) {
    LazyColumn(
        modifier =
            modifier
                .drawVerticalScrollbar(
                    state = state,
                    reverseScrolling = behavior.reverseLayout,
                ),
        state = state,
        contentPadding = behavior.contentPadding,
        reverseLayout = behavior.reverseLayout,
        verticalArrangement = behavior.verticalArrangement,
        horizontalAlignment = behavior.horizontalAlignment,
        userScrollEnabled = behavior.userScrollEnabled,
        content = content,
    )
}

/**
 * A [ScrollbarLazyColumn] with top/bottom [Divider]s that only show while the list is scrolled
 * past its start/end edge, used inside dialogs presenting a scrollable list of options.
 */
@Composable
fun EdgeIndicatedLazyColumn(
    modifier: Modifier = Modifier,
    state: LazyListState = rememberLazyListState(),
    content: LazyListScope.() -> Unit,
) {
    Box(modifier = modifier) {
        if (!state.isScrolledToStart()) Divider(Modifier.align(Alignment.TopCenter))
        if (!state.isScrolledToEnd()) Divider(Modifier.align(Alignment.BottomCenter))

        ScrollbarLazyColumn(state = state, content = content)
    }
}
