package com.batodev.sudoku.ui.components

import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.batodev.sudoku.core.qqwing.GameDifficulty

/**
 * The dropdown menu offering every [GameDifficulty] option, used both when creating/editing a
 * sudoku and when picking the difficulty to assign to an imported puzzle.
 */
@Composable
fun DifficultyDropdownMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    onClick: (GameDifficulty) -> Unit,
    modifier: Modifier = Modifier,
) {
    RoundedDropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
        modifier = modifier,
    ) {
        listOf(
            GameDifficulty.Easy,
            GameDifficulty.Moderate,
            GameDifficulty.Hard,
            GameDifficulty.Challenge,
            GameDifficulty.Custom,
        ).forEach {
            DropdownMenuItem(
                text = {
                    Text(stringResource(it.resName))
                },
                onClick = {
                    onClick(it)
                    onDismissRequest()
                },
            )
        }
    }
}
