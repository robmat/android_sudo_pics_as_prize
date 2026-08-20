package com.batodev.sudoku.ui.gameshistory.savedgame

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material3.AssistChip
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.batodev.sudoku.R
import com.batodev.sudoku.core.PreferencesConstants
import com.batodev.sudoku.core.utils.toFormattedString
import java.time.format.DateTimeFormatter
import kotlin.time.toKotlinDuration

private fun savedGameStatusStringRes(
    mistakes: Int,
    giveUp: Boolean,
    completed: Boolean,
    canContinue: Boolean
): Int {
    val reachedMistakesLimit = mistakes >= PreferencesConstants.MISTAKES_LIMIT
    return when {
        reachedMistakesLimit -> R.string.saved_game_mistakes_limit
        giveUp -> R.string.saved_game_give_up
        completed && !canContinue -> R.string.saved_game_completed
        else -> R.string.saved_game_in_progress
    }
}

@Composable
internal fun SavedGameDetails(
    viewModel: SavedGameViewModel,
    dateTimeFormatter: DateTimeFormatter,
    navigateToFolder: (Long) -> Unit,
    navigatePlayGame: (Long) -> Unit
) {
    Column(
        modifier = Modifier
            .padding(horizontal = 12.dp)
            .fillMaxWidth()
    ) {
        SavedGameFolderChip(viewModel, navigateToFolder)

        val textStyle = MaterialTheme.typography.bodyLarge
        SavedGameProgressText(viewModel, textStyle)
        SavedGameStartedRow(viewModel, dateTimeFormatter)
        SavedGameStatusText(viewModel)
        SavedGameSummaryTexts(viewModel, textStyle)
        SavedGameContinueButton(viewModel, navigatePlayGame)
    }
}

@Composable
private fun SavedGameFolderChip(viewModel: SavedGameViewModel, navigateToFolder: (Long) -> Unit) {
    val gameFolder by viewModel.gameFolder.collectAsStateWithLifecycle()
    gameFolder?.let {
        AssistChip(
            leadingIcon = {
                Icon(
                    Icons.Outlined.Folder,
                    contentDescription = null
                )
            },
            onClick = { navigateToFolder(it.uid) },
            label = { Text(it.name) }
        )
    }
}

@Composable
private fun SavedGameProgressText(viewModel: SavedGameViewModel, textStyle: TextStyle) {
    val progressPercentage by viewModel.gameProgressPercentage.collectAsStateWithLifecycle()
    LaunchedEffect(viewModel.parsedCurrentBoard) { viewModel.countProgressFilled() }

    Text(
        text = stringResource(
            R.string.saved_game_progress_percentage,
            progressPercentage
        ),
        style = textStyle
    )
}

@Composable
private fun SavedGameStartedRow(viewModel: SavedGameViewModel, dateTimeFormatter: DateTimeFormatter) {
    viewModel.savedGame?.let { savedGame ->
        if (savedGame.startedAt != null) {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                val startedAtDate by remember(savedGame) {
                    mutableStateOf(
                        savedGame.startedAt.format(dateTimeFormatter)
                    )
                }
                val startedAtTime by remember(savedGame) {
                    mutableStateOf(
                        savedGame.startedAt.format(DateTimeFormatter.ofPattern("HH:mm"))
                    )
                }
                Text(startedAtDate)
                Text(startedAtTime)
            }
        }
    }
}

@Composable
private fun SavedGameStatusText(viewModel: SavedGameViewModel) {
    Text(
        text = viewModel.savedGame?.let {
            stringResource(savedGameStatusStringRes(it.mistakes, it.giveUp, it.completed, it.canContinue))
        } ?: ""
    )
}

@Composable
private fun SavedGameSummaryTexts(viewModel: SavedGameViewModel, textStyle: TextStyle) {
    Text(
        text = stringResource(
            R.string.saved_game_difficulty,
            stringResource(viewModel.boardEntity!!.difficulty.resName)
        ),
        style = textStyle
    )
    Text(
        text = stringResource(
            R.string.saved_game_type,
            stringResource(viewModel.boardEntity!!.type.resName)
        ),
        style = textStyle
    )
    Text(
        text = stringResource(
            R.string.saved_game_time,
            viewModel.savedGame!!.timer
                .toKotlinDuration()
                .toFormattedString()
        )
    )
}

@Composable
private fun ColumnScope.SavedGameContinueButton(viewModel: SavedGameViewModel, navigatePlayGame: (Long) -> Unit) {
    if (viewModel.savedGame!!.canContinue) {
        FilledTonalButton(
            modifier = Modifier.align(Alignment.CenterHorizontally),
            onClick = { navigatePlayGame(viewModel.savedGame!!.uid) }
        ) {
            Text(stringResource(R.string.action_continue))
        }
    }
}
