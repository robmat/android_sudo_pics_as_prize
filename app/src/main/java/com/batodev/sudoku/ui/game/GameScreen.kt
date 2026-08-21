package com.batodev.sudoku.ui.game

import android.view.HapticFeedbackConstants
import android.view.View
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Redo
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.batodev.sudoku.R
import com.batodev.sudoku.core.Cell
import com.batodev.sudoku.core.PreferencesConstants
import com.batodev.sudoku.ui.components.RoundedDropdownMenu
import com.batodev.sudoku.ui.game.components.KeyboardState
import com.batodev.sudoku.ui.game.components.ToolBarItem
import com.batodev.sudoku.ui.game.components.keyboardClickHandlers
import com.batodev.sudoku.ui.onboarding.FirstGameDialog
import kotlinx.coroutines.flow.StateFlow

private const val RESTART_BUTTON_ANIMATION_DURATION_MS = 250

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(
    navigateBack: () -> Unit,
    navigateSettings: () -> Unit,
    viewModel: GameViewModel,
    modifier: Modifier = Modifier,
) {
    val localView = LocalView.current // vibration

    val firstGame by viewModel.firstGame.collectAsStateWithLifecycle(initialValue = false)
    GameFirstGameOverlay(
        firstGame = firstGame,
        onPauseTimer = viewModel::pauseTimer,
        onFirstGameFinish = {
            viewModel.setFirstGameFalse()
            viewModel.startTimer()
        },
    )

    val restartButtonAngleState = remember { mutableFloatStateOf(0f) }
    val restartButtonAnimation: Float by animateFloatAsState(
        targetValue = restartButtonAngleState.floatValue,
        animationSpec = tween(durationMillis = RESTART_BUTTON_ANIMATION_DURATION_MS),
        label = "this_label_makes_no_sense_to_me_but_i_added_to_overcome_a_warning",
    )

    val resetTimer by viewModel.resetTimerOnRestart.collectAsStateWithLifecycle(
        initialValue = PreferencesConstants.DEFAULT_GAME_RESET_TIMER,
    )

    val mistakesLimit by viewModel.mistakesLimit.collectAsStateWithLifecycle(
        initialValue = PreferencesConstants.DEFAULT_MISTAKES_LIMIT,
    )

    var renderNotes by remember { mutableStateOf(true) }

    val prefs = rememberGameBoardPrefs(viewModel)

    Scaffold(
        modifier = modifier,
        topBar = {
            GameTopBar(
                state =
                    GameTopBarState(
                        endGame = viewModel.endGame,
                        reachedMistakesLimitOrGaveUp =
                            viewModel.mistakesCount >= PreferencesConstants.MISTAKES_LIMIT || viewModel.giveUp,
                        showSolution = viewModel.showSolution,
                        gamePlaying = viewModel.gamePlaying,
                        showMenu = viewModel.showMenu,
                        restartButtonAnimation = restartButtonAnimation,
                    ),
                actions =
                    GameTopBarActions(
                        onToggleShowSolution = { viewModel.showSolution = !viewModel.showSolution },
                        onPlayPauseToggle = {
                            if (!viewModel.gamePlaying) viewModel.startTimer() else viewModel.pauseTimer()
                            viewModel.currCell = Cell(-1, -1, 0)
                        },
                        onRestartClick = { viewModel.restartDialog = true },
                        onToggleMenu = { viewModel.showMenu = !viewModel.showMenu },
                        onDismissMenu = { viewModel.showMenu = false },
                        onGiveUpClick = {
                            viewModel.pauseTimer()
                            viewModel.giveUpDialog = true
                        },
                        onSettingsClick = {
                            navigateSettings()
                            viewModel.showMenu = false
                        },
                    ),
                navigateBack = navigateBack,
            )
        },
    ) { scaffoldPaddings ->
        Box {
            GamePrizeImage(
                endGame = viewModel.endGame,
                prizeImageName = if (!viewModel.endGame) viewModel.prizeImageName() else null,
            )
            Column(
                modifier =
                    Modifier
                        .padding(scaffoldPaddings)
                        .padding(horizontal = 12.dp),
                verticalArrangement = Arrangement.SpaceEvenly,
            ) {
                val errorHighlight = prefs.errorHighlight
                val timerEnabled by viewModel.timerEnabled.collectAsStateWithLifecycle(
                    initialValue = PreferencesConstants.DEFAULT_SHOW_TIMER,
                )
                GameStatsRow(
                    endGame = viewModel.endGame,
                    gameDifficultyRes = viewModel.gameDifficulty.resName,
                    mistakesLimit = mistakesLimit,
                    errorHighlight = errorHighlight,
                    mistakesCount = viewModel.mistakesCount,
                    timerEnabled = timerEnabled,
                    timeText = viewModel.timeText,
                )
                GameBoardArea(
                    boardState =
                        GameBoardState(
                            gameBoard = viewModel.gameBoard,
                            solvedBoard = viewModel.solvedBoard,
                            showSolution = viewModel.showSolution,
                            size = viewModel.size,
                            notes = viewModel.notes,
                            currCell = viewModel.currCell,
                            gameType = viewModel.gameType,
                            gamePlaying = viewModel.gamePlaying,
                            endGame = viewModel.endGame,
                        ),
                    boardActions =
                        GameBoardActions(
                            onCellClick = { cell ->
                                viewModel.processInput(cell = cell, remainingUse = prefs.remainingUse)
                            },
                            onCellLongClick = { cell ->
                                if (viewModel.processInput(cell, prefs.remainingUse, longTap = true)) {
                                    localView.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                                }
                            },
                        ),
                    prefs = prefs,
                    renderNotes = renderNotes,
                )
                val funKeyboardOverNum by viewModel.funKeyboardOverNum.collectAsStateWithLifecycle(
                    initialValue = PreferencesConstants.DEFAULT_FUN_KEYBOARD_OVER_NUM,
                )
                val hintsDisabled by viewModel.disableHints.collectAsStateWithLifecycle(
                    initialValue = PreferencesConstants.DEFAULT_HINTS_DISABLED,
                )
                GameBottomContent(
                    endGame = viewModel.endGame,
                    playingContentParams =
                        GamePlayingContentParams(
                            funKeyboardOverNum = funKeyboardOverNum,
                            keyboardSize = viewModel.size,
                            keyboardState =
                                KeyboardState(
                                    remainingUses = if (prefs.remainingUse) viewModel.remainingUsesList else null,
                                    selected = viewModel.digitFirstNumber,
                                    handlers = keyboardClickHandlers(viewModel::processInputKeyboard),
                                ),
                            toolbarState =
                                GameToolbarState(
                                    showUndoRedoMenu = viewModel.showUndoRedoMenu,
                                    hintsDisabled = hintsDisabled,
                                    showNotesMenu = viewModel.showNotesMenu,
                                    renderNotes = renderNotes,
                                    notesToggled = viewModel.notesToggled,
                                    eraseButtonToggled = viewModel.eraseButtonToggled,
                                    gamePlaying = viewModel.gamePlaying,
                                ),
                            toolbarActions =
                                GameToolbarActions(
                                    onUndoClick = { viewModel.toolbarClick(ToolBarItem.Undo) },
                                    onDismissUndoRedoMenu = { viewModel.showUndoRedoMenu = false },
                                    onShowUndoRedoMenu = { viewModel.showUndoRedoMenu = true },
                                    onRedoClick = { viewModel.toolbarClick(ToolBarItem.Redo) },
                                    onHintClick = { viewModel.toolbarClick(ToolBarItem.Hint) },
                                    onNoteClick = { viewModel.toolbarClick(ToolBarItem.Note) },
                                    onShowNotesMenu = { viewModel.showNotesMenu = true },
                                    onDismissNotesMenu = { viewModel.showNotesMenu = false },
                                    onComputeNotesClick = { viewModel.computeNotes() },
                                    onClearNotesClick = { viewModel.clearNotes() },
                                    onToggleRenderNotes = { renderNotes = !renderNotes },
                                    onEraseClick = { viewModel.toolbarClick(ToolBarItem.Remove) },
                                    onToggleEraseButton = { viewModel.toggleEraseButton() },
                                ),
                            localView = localView,
                        ),
                    completedStatsInfo = {
                        val allRecords by viewModel.allRecords.collectAsStateWithLifecycle(
                            initialValue = emptyList(),
                        )
                        AfterGameStatsInfo(
                            difficulty = viewModel.gameDifficulty,
                            type = viewModel.gameType,
                            hintsUsed = viewModel.hintsUsed,
                            mistakesMade = viewModel.mistakesMade,
                            mistakesLimit = mistakesLimit,
                            mistakesLimitCount = viewModel.mistakesCount,
                            giveUp = viewModel.giveUp,
                            notesTaken = viewModel.notesTaken,
                            records = allRecords,
                            timeText = viewModel.timeText,
                        )
                    },
                )
            }
        }
    }

    val keepScreenOn by viewModel.keepScreenOn.collectAsStateWithLifecycle(
        initialValue = PreferencesConstants.DEFAULT_KEEP_SCREEN_ON,
    )
    if (keepScreenOn) {
        KeepScreenOn()
    }

    // dialogs
    GameDialogs(
        state =
            GameDialogsState(
                restartDialog = viewModel.restartDialog,
                giveUpDialog = viewModel.giveUpDialog,
                resetTimer = resetTimer,
            ),
        actions =
            GameDialogsActions(
                onDismissRestartDialog = {
                    viewModel.restartDialog = false
                    viewModel.startTimer()
                },
                onConfirmRestart = {
                    restartButtonAngleState.floatValue -= ROTATE_ICON_FULL_DEGREES
                    viewModel.resetGame(resetTimer)
                    viewModel.restartDialog = false
                    viewModel.startTimer()
                },
                onDismissGiveUpDialog = {
                    viewModel.giveUpDialog = false
                    viewModel.startTimer()
                },
                onConfirmGiveUp = {
                    viewModel.giveUp()
                    viewModel.giveUpDialog = false
                    viewModel.pauseTimer()
                },
            ),
    )

    GameLifecycleEffects(
        endGame = viewModel.endGame,
        gameCompleted = viewModel.gameCompleted,
        gamePlaying = viewModel.gamePlaying,
        onStartTimer = viewModel::startTimer,
        onPauseTimer = viewModel::pauseTimer,
        onGameComplete = viewModel::onGameComplete,
        onEndGame = { viewModel.endGame = true },
        onCurrCellReset = { viewModel.currCell = Cell(-1, -1, 0) },
        onCheckMistakesAll = viewModel::checkMistakesAll,
        mistakesMethodChanged = viewModel.mistakesMethod,
    )
}

@Composable
private fun GameFirstGameOverlay(
    firstGame: Boolean,
    onPauseTimer: () -> Unit,
    onFirstGameFinish: () -> Unit,
) {
    if (firstGame) {
        onPauseTimer()
        FirstGameDialog(
            onFinish = onFirstGameFinish,
        )
    }
}

@Composable
private fun GameLifecycleEffects(
    endGame: Boolean,
    gameCompleted: Boolean,
    gamePlaying: Boolean,
    onStartTimer: () -> Unit,
    onPauseTimer: () -> Unit,
    onGameComplete: () -> Unit,
    onEndGame: () -> Unit,
    onCurrCellReset: () -> Unit,
    onCheckMistakesAll: () -> Unit,
    mistakesMethodChanged: StateFlow<Int>,
) {
    val currentOnStartTimer by rememberUpdatedState(onStartTimer)
    val currentOnPauseTimer by rememberUpdatedState(onPauseTimer)
    val currentOnGameComplete by rememberUpdatedState(onGameComplete)
    val currentOnEndGame by rememberUpdatedState(onEndGame)
    val currentOnCurrCellReset by rememberUpdatedState(onCurrCellReset)
    val currentOnCheckMistakesAll by rememberUpdatedState(onCheckMistakesAll)

    LaunchedEffect(Unit) {
        if (!endGame && !gameCompleted) {
            currentOnStartTimer()
        }
    }

    LaunchedEffect(gameCompleted) {
        if (gameCompleted) {
            currentOnGameComplete()
            currentOnEndGame()
        }
    }

    LaunchedEffect(mistakesMethodChanged) {
        currentOnCheckMistakesAll()
    }

    // so that the timer doesn't run in the background
    // https://stackoverflow.com/questions/66546962/jetpack-compose-how-do-i-refresh-a-screen-when-app-returns-to-foreground/66807899#66807899
    OnLifecycleEvent { _, event ->
        when (event) {
            Lifecycle.Event.ON_RESUME -> {
                if (gamePlaying) currentOnStartTimer()
            }

            Lifecycle.Event.ON_PAUSE -> {
                currentOnPauseTimer()
                currentOnCurrCellReset()
            }

            Lifecycle.Event.ON_DESTROY -> {
                currentOnPauseTimer()
            }

            else -> {}
        }
    }
}

data class NotesMenuActions(
    val onDismiss: () -> Unit,
    val onComputeNotesClick: () -> Unit,
    val onClearNotesClick: () -> Unit,
    val onRenderNotesClick: () -> Unit,
)

@Composable
fun NotesMenu(
    expanded: Boolean,
    renderNotes: Boolean,
    actions: NotesMenuActions,
    modifier: Modifier = Modifier,
) {
    RoundedDropdownMenu(
        modifier = modifier,
        expanded = expanded,
        onDismissRequest = { actions.onDismiss() },
    ) {
        DropdownMenuItem(
            text = { Text(stringResource(R.string.action_compute_notes)) },
            onClick = {
                actions.onComputeNotesClick()
                actions.onDismiss()
            },
        )
        DropdownMenuItem(
            text = {
                Text(stringResource(R.string.action_clear_notes))
            },
            onClick = {
                actions.onClearNotesClick()
                actions.onDismiss()
            },
        )
        DropdownMenuItem(
            text = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(stringResource(R.string.action_show_notes))
                    Checkbox(
                        checked = renderNotes,
                        onCheckedChange = { actions.onRenderNotesClick() },
                    )
                }
            },
            onClick = actions.onRenderNotesClick,
        )
    }
}

@Composable
fun UndoRedoMenu(
    expanded: Boolean,
    onDismiss: () -> Unit,
    onRedoClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    RoundedDropdownMenu(
        modifier = modifier,
        expanded = expanded,
        onDismissRequest = { onDismiss() },
    ) {
        DropdownMenuItem(
            text = { Text(stringResource(R.string.redo)) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Rounded.Redo,
                    contentDescription = null,
                )
            },
            onClick = {
                onRedoClick()
                onDismiss()
            },
        )
    }
}

@Composable
fun GameMenu(
    expanded: Boolean,
    onDismiss: () -> Unit,
    onGiveUpClick: () -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    RoundedDropdownMenu(
        modifier = modifier,
        expanded = expanded,
        onDismissRequest = onDismiss,
    ) {
        DropdownMenuItem(
            text = { Text(stringResource(R.string.action_give_up)) },
            onClick = {
                onGiveUpClick()
                onDismiss()
            },
        )
        DropdownMenuItem(
            text = { Text(stringResource(R.string.settings_title)) },
            onClick = {
                onSettingsClick()
                onDismiss()
            },
        )
    }
}

@Composable
fun TopBoardSection(
    text: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 4.dp),
        )
    }
}

@Composable
fun OnLifecycleEvent(onEvent: (owner: LifecycleOwner, event: Lifecycle.Event) -> Unit) {
    val eventHandler = rememberUpdatedState(onEvent)
    val lifecycleOwner = rememberUpdatedState(LocalLifecycleOwner.current)

    DisposableEffect(lifecycleOwner.value) {
        val lifecycle = lifecycleOwner.value.lifecycle
        val observer =
            LifecycleEventObserver { owner, event ->
                eventHandler.value(owner, event)
            }

        lifecycle.addObserver(observer)
        onDispose {
            lifecycle.removeObserver(observer)
        }
    }
}

@Composable
fun KeepScreenOn() = AndroidView({ View(it).apply { keepScreenOn = true } })
