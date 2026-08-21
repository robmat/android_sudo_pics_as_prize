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
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import com.batodev.sudoku.R
import com.batodev.sudoku.core.Cell
import com.batodev.sudoku.core.PreferencesConstants
import com.batodev.sudoku.core.utils.toFormattedString
import com.batodev.sudoku.data.database.model.Folder
import com.batodev.sudoku.data.database.model.SavedGame
import com.batodev.sudoku.data.database.model.SudokuBoard
import java.time.format.DateTimeFormatter
import kotlin.time.toKotlinDuration

private fun savedGameStatusStringRes(
    mistakes: Int,
    giveUp: Boolean,
    completed: Boolean,
    canContinue: Boolean,
): Int {
    val reachedMistakesLimit = mistakes >= PreferencesConstants.MISTAKES_LIMIT
    return when {
        reachedMistakesLimit -> R.string.saved_game_mistakes_limit
        giveUp -> R.string.saved_game_give_up
        completed && !canContinue -> R.string.saved_game_completed
        else -> R.string.saved_game_in_progress
    }
}

/** The values [SavedGameDetails] and its sub-sections need; read once from [SavedGameViewModel] by [SavedGameScreen]. */
internal data class SavedGameDetailsState(
    val savedGame: SavedGame?,
    val boardEntity: SudokuBoard?,
    val parsedCurrentBoard: List<List<Cell>>,
    val gameFolder: Folder?,
    val gameProgressPercentage: Int,
)

/** The callbacks [SavedGameDetails] needs; constructed once by [SavedGameScreen]. */
internal data class SavedGameDetailsActions(
    val onCountProgressFill: () -> Unit,
    val navigateToFolder: (Long) -> Unit,
    val navigatePlayGame: (Long) -> Unit,
)

@Composable
internal fun SavedGameDetails(
    state: SavedGameDetailsState,
    actions: SavedGameDetailsActions,
    dateTimeFormatter: DateTimeFormatter,
) {
    Column(
        modifier =
            Modifier
                .padding(horizontal = 12.dp)
                .fillMaxWidth(),
    ) {
        SavedGameFolderChip(state.gameFolder, actions.navigateToFolder)

        val textStyle = MaterialTheme.typography.bodyLarge
        SavedGameProgressText(
            state.gameProgressPercentage,
            state.parsedCurrentBoard,
            actions.onCountProgressFill,
            textStyle,
        )
        SavedGameStartedRow(state.savedGame, dateTimeFormatter)
        SavedGameStatusText(state.savedGame)
        SavedGameSummaryTexts(state.savedGame, state.boardEntity, textStyle)
        SavedGameContinueButton(state.savedGame, actions.navigatePlayGame)
    }
}

@Composable
private fun SavedGameFolderChip(
    gameFolder: Folder?,
    navigateToFolder: (Long) -> Unit,
) {
    gameFolder?.let {
        AssistChip(
            leadingIcon = {
                Icon(
                    Icons.Outlined.Folder,
                    contentDescription = null,
                )
            },
            onClick = { navigateToFolder(it.uid) },
            label = { Text(it.name) },
        )
    }
}

@Composable
private fun SavedGameProgressText(
    progressPercentage: Int,
    parsedCurrentBoard: List<List<Cell>>,
    onCountProgressFill: () -> Unit,
    textStyle: TextStyle,
) {
    val currentOnCountProgressFill by rememberUpdatedState(onCountProgressFill)
    LaunchedEffect(parsedCurrentBoard) { currentOnCountProgressFill() }

    Text(
        text =
            stringResource(
                R.string.saved_game_progress_percentage,
                progressPercentage,
            ),
        style = textStyle,
    )
}

@Composable
private fun SavedGameStartedRow(
    savedGame: SavedGame?,
    dateTimeFormatter: DateTimeFormatter,
) {
    savedGame?.let {
        if (it.startedAt != null) {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                val startedAtDate by remember(it) {
                    mutableStateOf(
                        it.startedAt.format(dateTimeFormatter),
                    )
                }
                val startedAtTime by remember(it) {
                    mutableStateOf(
                        it.startedAt.format(DateTimeFormatter.ofPattern("HH:mm")),
                    )
                }
                Text(startedAtDate)
                Text(startedAtTime)
            }
        }
    }
}

@Composable
private fun SavedGameStatusText(savedGame: SavedGame?) {
    Text(
        text =
            savedGame?.let {
                stringResource(savedGameStatusStringRes(it.mistakes, it.giveUp, it.completed, it.canContinue))
            } ?: "",
    )
}

@Composable
private fun SavedGameSummaryTexts(
    savedGame: SavedGame?,
    boardEntity: SudokuBoard?,
    textStyle: TextStyle,
) = Column {
    Text(
        text =
            stringResource(
                R.string.saved_game_difficulty,
                stringResource(boardEntity!!.difficulty.resName),
            ),
        style = textStyle,
    )
    Text(
        text =
            stringResource(
                R.string.saved_game_type,
                stringResource(boardEntity.type.resName),
            ),
        style = textStyle,
    )
    Text(
        text =
            stringResource(
                R.string.saved_game_time,
                savedGame!!
                    .timer
                    .toKotlinDuration()
                    .toFormattedString(),
            ),
    )
}

@Composable
private fun ColumnScope.SavedGameContinueButton(
    savedGame: SavedGame?,
    navigatePlayGame: (Long) -> Unit,
) {
    if (savedGame!!.canContinue) {
        FilledTonalButton(
            modifier = Modifier.align(Alignment.CenterHorizontally),
            onClick = { navigatePlayGame(savedGame.uid) },
        ) {
            Text(stringResource(R.string.action_continue))
        }
    }
}
