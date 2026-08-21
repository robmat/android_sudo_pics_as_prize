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
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.batodev.sudoku.R
import com.batodev.sudoku.ui.game.components.DefaultGameKeyboard
import com.batodev.sudoku.ui.game.components.KeyboardState
import com.batodev.sudoku.ui.game.components.ToolBarItem
import com.batodev.sudoku.ui.game.components.ToolbarItem
import com.batodev.sudoku.ui.util.ReverseArrangement

/** The state [GameToolbarRow] and its sections need; read once from [GameViewModel] by [GameScreen]. */
internal data class GameToolbarState(
    val showUndoRedoMenu: Boolean,
    val hintsDisabled: Boolean,
    val showNotesMenu: Boolean,
    val renderNotes: Boolean,
    val notesToggled: Boolean,
    val eraseButtonToggled: Boolean,
    val gamePlaying: Boolean,
)

/** The callbacks [GameToolbarRow] and its sections need; constructed once by [GameScreen]. */
internal data class GameToolbarActions(
    val onUndoClick: () -> Unit,
    val onDismissUndoRedoMenu: () -> Unit,
    val onShowUndoRedoMenu: () -> Unit,
    val onRedoClick: () -> Unit,
    val onHintClick: () -> Unit,
    val onNoteClick: () -> Unit,
    val onShowNotesMenu: () -> Unit,
    val onDismissNotesMenu: () -> Unit,
    val onComputeNotesClick: () -> Unit,
    val onClearNotesClick: () -> Unit,
    val onToggleRenderNotes: () -> Unit,
    val onEraseClick: () -> Unit,
    val onToggleEraseButton: () -> Unit,
)

/** The params [GamePlayingContent] needs; assembled once by [GameScreen]. */
internal data class GamePlayingContentParams(
    val funKeyboardOverNum: Boolean,
    val keyboardSize: Int,
    val keyboardState: KeyboardState,
    val toolbarState: GameToolbarState,
    val toolbarActions: GameToolbarActions,
    val localView: View,
)

@Composable
internal fun GamePlayingContent(params: GamePlayingContentParams) {
    Column(
        verticalArrangement = if (params.funKeyboardOverNum) ReverseArrangement else Arrangement.Top,
    ) {
        DefaultGameKeyboard(
            size = params.keyboardSize,
            state = params.keyboardState,
        )
        GameToolbarRow(params.toolbarState, params.toolbarActions, params.localView)
    }
}

@Composable
internal fun GameToolbarRow(
    state: GameToolbarState,
    actions: GameToolbarActions,
    localView: View,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(vertical = 8.dp),
    ) {
        UndoRedoSection(state, actions)
        HintSection(state, actions)
        NotesSection(state, actions, localView)
        EraseSection(state, actions, localView)
    }
}

@Composable
private fun RowScope.UndoRedoSection(
    state: GameToolbarState,
    actions: GameToolbarActions,
) {
    Box(
        modifier = Modifier.weight(1f),
    ) {
        UndoRedoMenu(
            expanded = state.showUndoRedoMenu,
            onDismiss = actions.onDismissUndoRedoMenu,
            onRedoClick = actions.onRedoClick,
        )
        ToolbarItem(
            modifier = Modifier.testTag("game_undo"),
            painter = painterResource(R.drawable.ic_round_undo_24),
            onClick = actions.onUndoClick,
            onLongClick = actions.onShowUndoRedoMenu,
        )
    }
}

@Composable
private fun RowScope.HintSection(
    state: GameToolbarState,
    actions: GameToolbarActions,
) {
    if (!state.hintsDisabled) {
        ToolbarItem(
            modifier = Modifier.weight(1f),
            painter = painterResource(R.drawable.ic_lightbulb_stars_24),
            onClick = actions.onHintClick,
        )
    }
}

@Composable
private fun RowScope.NotesSection(
    state: GameToolbarState,
    actions: GameToolbarActions,
    localView: View,
) {
    Box(
        modifier = Modifier.weight(1f),
    ) {
        NotesMenu(
            expanded = state.showNotesMenu,
            renderNotes = state.renderNotes,
            actions =
                NotesMenuActions(
                    onDismiss = actions.onDismissNotesMenu,
                    onComputeNotesClick = actions.onComputeNotesClick,
                    onClearNotesClick = actions.onClearNotesClick,
                    onRenderNotesClick = actions.onToggleRenderNotes,
                ),
        )
        ToolbarItem(
            painter = painterResource(R.drawable.ic_round_edit_24),
            toggled = state.notesToggled,
            onClick = actions.onNoteClick,
            onLongClick = {
                if (state.gamePlaying) {
                    localView.performHapticFeedback(
                        HapticFeedbackConstants.VIRTUAL_KEY,
                    )
                    actions.onShowNotesMenu()
                }
            },
        )
    }
}

@Composable
private fun RowScope.EraseSection(
    state: GameToolbarState,
    actions: GameToolbarActions,
    localView: View,
) {
    ToolbarItem(
        modifier = Modifier.weight(1f),
        painter = painterResource(R.drawable.ic_eraser_24),
        toggled = state.eraseButtonToggled,
        onClick = actions.onEraseClick,
        onLongClick = {
            if (state.gamePlaying) {
                localView.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                actions.onToggleEraseButton()
            }
        },
    )
}

@Composable
internal fun GameCompletedStats(info: AfterGameStatsInfo) {
    AfterGameStats(
        modifier = Modifier.fillMaxWidth(),
        info = info,
    )
}
