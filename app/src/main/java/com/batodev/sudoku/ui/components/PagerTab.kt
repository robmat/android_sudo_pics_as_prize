package com.batodev.sudoku.ui.components

import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.style.TextOverflow

/**
 * A [Tab] labelled with [title] (truncated to two lines), used inside a [androidx.compose.material3.TabRow]
 * paired with a pager. Callers wire [onClick] to scroll their own pager state, so this works for
 * both the androidx and accompanist pager APIs.
 */
@Composable
fun PagerTab(
    selected: Boolean,
    title: String,
    onClick: () -> Unit
) {
    Tab(
        selected = selected,
        onClick = onClick,
        text = {
            Text(
                text = title,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    )
}
