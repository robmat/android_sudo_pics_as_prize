package com.batodev.sudoku.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.batodev.sudoku.R

/** The small pill badge shown on a saved game's card/row when it can be continued. */
@Composable
fun CanContinueBadge(modifier: Modifier = Modifier) {
    Box(
        modifier =
            modifier
                .background(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = MaterialTheme.shapes.large,
                ).padding(vertical = 4.dp, horizontal = 8.dp),
    ) {
        Text(
            text = stringResource(R.string.can_continue_label),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
        )
    }
}
