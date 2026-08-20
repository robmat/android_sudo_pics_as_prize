package com.batodev.sudoku.ui.game

import android.view.HapticFeedbackConstants
import android.view.View
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.batodev.sudoku.R
import com.batodev.sudoku.core.PreferencesConstants
import com.batodev.sudoku.ui.game.components.DefaultGameKeyboard
import com.batodev.sudoku.ui.game.components.KeyboardState
import com.batodev.sudoku.ui.game.components.ToolBarItem
import com.batodev.sudoku.ui.game.components.ToolbarItem
import com.batodev.sudoku.ui.game.components.keyboardClickHandlers
import com.batodev.sudoku.ui.util.ReverseArrangement

@Composable
internal fun GamePlayingContent(viewModel: GameViewModel, renderNotesState: MutableState<Boolean>, localView: View) {
    val funKeyboardOverNum by viewModel.funKeyboardOverNum.collectAsStateWithLifecycle(
        initialValue = PreferencesConstants.DEFAULT_FUN_KEYBOARD_OVER_NUM
    )
    Column(
        verticalArrangement = if (funKeyboardOverNum) ReverseArrangement else Arrangement.Top
    ) {
        val remainingUse by viewModel.remainingUse.collectAsStateWithLifecycle(
            initialValue = PreferencesConstants.DEFAULT_REMAINING_USES
        )
        DefaultGameKeyboard(
            size = viewModel.size,
            state = KeyboardState(
                remainingUses = if (remainingUse) viewModel.remainingUsesList else null,
                selected = viewModel.digitFirstNumber,
                handlers = keyboardClickHandlers(viewModel::processInputKeyboard)
            )
        )
        GameToolbarRow(viewModel, renderNotesState, localView)
    }
}

@Composable
internal fun GameToolbarRow(viewModel: GameViewModel, renderNotesState: MutableState<Boolean>, localView: View) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(vertical = 8.dp)
    ) {
        UndoRedoSection(viewModel)
        HintSection(viewModel)
        NotesSection(viewModel, renderNotesState, localView)
        EraseSection(viewModel, localView)
    }
}

@Composable
private fun RowScope.UndoRedoSection(viewModel: GameViewModel) {
    Box(
        modifier = Modifier.weight(1f)
    ) {
        UndoRedoMenu(
            expanded = viewModel.showUndoRedoMenu,
            onDismiss = { viewModel.showUndoRedoMenu = false },
            onRedoClick = { viewModel.toolbarClick(ToolBarItem.Redo) }
        )
        ToolbarItem(
            modifier = Modifier.testTag("game_undo"),
            painter = painterResource(R.drawable.ic_round_undo_24),
            onClick = { viewModel.toolbarClick(ToolBarItem.Undo) },
            onLongClick = { viewModel.showUndoRedoMenu = true }
        )
    }
}

@Composable
private fun RowScope.HintSection(viewModel: GameViewModel) {
    val hintsDisabled by viewModel.disableHints.collectAsStateWithLifecycle(
        initialValue = PreferencesConstants.DEFAULT_HINTS_DISABLED
    )
    if (!hintsDisabled) {
        ToolbarItem(
            modifier = Modifier.weight(1f),
            painter = painterResource(R.drawable.ic_lightbulb_stars_24),
            onClick = { viewModel.toolbarClick(ToolBarItem.Hint) }
        )
    }
}

@Composable
private fun RowScope.NotesSection(viewModel: GameViewModel, renderNotesState: MutableState<Boolean>, localView: View) {
    var renderNotes by renderNotesState
    Box(
        modifier = Modifier.weight(1f)
    ) {
        NotesMenu(
            expanded = viewModel.showNotesMenu,
            renderNotes = renderNotes,
            actions = NotesMenuActions(
                onDismiss = { viewModel.showNotesMenu = false },
                onComputeNotesClick = { viewModel.computeNotes() },
                onClearNotesClick = { viewModel.clearNotes() },
                onRenderNotesClick = { renderNotes = !renderNotes }
            )
        )
        ToolbarItem(
            painter = painterResource(R.drawable.ic_round_edit_24),
            toggled = viewModel.notesToggled,
            onClick = { viewModel.toolbarClick(ToolBarItem.Note) },
            onLongClick = {
                if (viewModel.gamePlaying) {
                    localView.performHapticFeedback(
                        HapticFeedbackConstants.VIRTUAL_KEY
                    )
                    viewModel.showNotesMenu = true
                }
            }
        )
    }
}

@Composable
private fun RowScope.EraseSection(viewModel: GameViewModel, localView: View) {
    ToolbarItem(
        modifier = Modifier.weight(1f),
        painter = painterResource(R.drawable.ic_eraser_24),
        toggled = viewModel.eraseButtonToggled,
        onClick = {
            viewModel.toolbarClick(ToolBarItem.Remove)
        },
        onLongClick = {
            if (viewModel.gamePlaying) {
                localView.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                viewModel.toggleEraseButton()
            }
        }
    )
}

@Composable
internal fun GameCompletedStats(viewModel: GameViewModel, mistakesLimit: Boolean) {
    val allRecords by viewModel.allRecords.collectAsStateWithLifecycle(
        initialValue = emptyList()
    )

    AfterGameStats(
        modifier = Modifier.fillMaxWidth(),
        info = AfterGameStatsInfo(
            difficulty = viewModel.gameDifficulty,
            type = viewModel.gameType,
            hintsUsed = viewModel.hintsUsed,
            mistakesMade = viewModel.mistakesMade,
            mistakesLimit = mistakesLimit,
            mistakesLimitCount = viewModel.mistakesCount,
            giveUp = viewModel.giveUp,
            notesTaken = viewModel.notesTaken,
            records = allRecords,
            timeText = viewModel.timeText
        )
    )
}
