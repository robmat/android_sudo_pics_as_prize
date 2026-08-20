package com.batodev.sudoku.ui.components

import androidx.compose.foundation.gestures.FlingBehavior
import androidx.compose.foundation.gestures.ScrollableDefaults
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.batodev.sudoku.ui.util.drawVerticalScrollbar

/** The scroll/layout behavior options accepted by [LazyVerticalGrid], grouped to keep the wrapper's
 * own parameter list short. */
data class LazyGridBehavior(
    val contentPadding: PaddingValues = PaddingValues(0.dp),
    val reverseLayout: Boolean = false,
    val verticalArrangement: Arrangement.Vertical =
        if (!reverseLayout) Arrangement.Top else Arrangement.Bottom,
    val horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    val userScrollEnabled: Boolean = true
)

/** The [state] and [spanCount] needed to draw the scrollbar overlay on a lazy grid. */
data class LazyGridScrollbarState(
    val state: LazyGridState,
    val spanCount: Int = 1
)

@Composable
fun ScrollbarLazyVerticalGrid(
    columns: GridCells,
    modifier: Modifier = Modifier,
    scrollbarState: LazyGridScrollbarState = LazyGridScrollbarState(rememberLazyGridState()),
    behavior: LazyGridBehavior = LazyGridBehavior(),
    content: LazyGridScope.() -> Unit
) {
    val flingBehavior: FlingBehavior = ScrollableDefaults.flingBehavior()
    LazyVerticalGrid(
        columns = columns,
        modifier = modifier.drawVerticalScrollbar(
            state = scrollbarState.state,
            spanCount = scrollbarState.spanCount
        ),
        state = scrollbarState.state,
        contentPadding = behavior.contentPadding,
        reverseLayout = behavior.reverseLayout,
        verticalArrangement = behavior.verticalArrangement,
        horizontalArrangement = behavior.horizontalArrangement,
        flingBehavior = flingBehavior,
        userScrollEnabled = behavior.userScrollEnabled,
        content = content
    )
}
