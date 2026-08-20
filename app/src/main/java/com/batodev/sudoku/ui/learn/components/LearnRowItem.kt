package com.batodev.sudoku.ui.learn.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import com.batodev.sudoku.R
import com.batodev.sudoku.ui.components.PreferenceRow
import com.batodev.sudoku.ui.components.PreferenceRowInfo
import com.batodev.sudoku.ui.components.PreferenceRowInteractions

/**
 * A row in the "learn" list. This is a thin wrapper around the shared [PreferenceRow] (with no
 * clip shape, so it keeps its original unclipped appearance) that always shows an icon,
 * defaulting to a help icon.
 */
@Composable
fun LearnRowItem(
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null
) {
    PreferenceRow(
        modifier = modifier,
        info = PreferenceRowInfo(
            title = title,
            subtitle = subtitle,
            painter = painterResource(R.drawable.ic_outline_help_outline_24)
        ),
        interactions = PreferenceRowInteractions(onClick = onClick),
        shape = null
    )
}
