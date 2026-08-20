package com.batodev.sudoku.ui.game

import android.graphics.BitmapFactory
import android.os.Build
import android.view.HapticFeedbackConstants
import android.view.View
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableFloatState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.batodev.sudoku.LocalBoardColors
import com.batodev.sudoku.R
import com.batodev.sudoku.core.PreferencesConstants
import com.batodev.sudoku.core.qqwing.GameType
import com.batodev.sudoku.ui.components.board.Board
import com.batodev.sudoku.ui.components.board.BoardData
import com.batodev.sudoku.ui.components.board.BoardDisplayOptions
import com.batodev.sudoku.ui.components.board.BoardInteraction
import com.batodev.sudoku.ui.components.board.BoardStyle
import com.batodev.sudoku.ui.components.board.BoardTextSizes
import com.batodev.sudoku.ui.gallery.PRIZE_IMAGES

private const val PRIZE_IMAGE_PADDING_TOP = 105
private const val PRIZE_IMAGE_BACKGROUND_ALPHA = 0.2f
private const val BOARD_DIM_SCALE = 0.90f

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun GameTopBar(
    viewModel: GameViewModel,
    navigateBack: () -> Unit,
    navigateSettings: () -> Unit,
    restartButtonAnimation: Float,
) {
    TopAppBar(
        title = { },
        navigationIcon = {
            IconButton(onClick = navigateBack, modifier = Modifier.testTag("game_back")) {
                Icon(
                    painter = painterResource(R.drawable.ic_round_arrow_back_24),
                    contentDescription = null,
                )
            }
        },
        actions = {
            ShowSolutionAction(viewModel)
            PlayPauseAction(viewModel)
            RestartAction(viewModel, restartButtonAnimation)
            GameMenuAction(viewModel, navigateSettings)
        },
    )
}

@Composable
internal fun GamePrizeImage(viewModel: GameViewModel) {
    if (viewModel.endGame) return
    val context = LocalContext.current
    Image(
        bitmap =
            BitmapFactory
                .decodeStream(
                    context.assets.open("$PRIZE_IMAGES/${viewModel.prizeImageName()}"),
                )!!
                .asImageBitmap(),
        contentScale = ContentScale.FillWidth,
        contentDescription = stringResource(id = R.string.app_name),
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(8.dp, PRIZE_IMAGE_PADDING_TOP.dp, 8.dp, 8.dp)
                .clip(RoundedCornerShape(8.dp))
                .border(
                    1.dp,
                    LocalBoardColors.current.thickLineColor,
                    RoundedCornerShape(8.dp),
                ).alpha(PRIZE_IMAGE_BACKGROUND_ALPHA),
    )
}

@Composable
internal fun GameStatsRow(
    viewModel: GameViewModel,
    mistakesLimit: Boolean,
) {
    val errorHighlight by viewModel.mistakesMethod.collectAsStateWithLifecycle(
        initialValue = PreferencesConstants.DEFAULT_HIGHLIGHT_MISTAKES,
    )
    AnimatedVisibility(visible = !viewModel.endGame) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            TopBoardSection(stringResource(viewModel.gameDifficulty.resName))

            if (mistakesLimit && errorHighlight != 0) {
                TopBoardSection(
                    stringResource(
                        R.string.mistakes_number_out_of,
                        viewModel.mistakesCount,
                        PreferencesConstants.MISTAKES_LIMIT,
                    ),
                )
            }

            val timerEnabled by viewModel.timerEnabled.collectAsStateWithLifecycle(
                initialValue = PreferencesConstants.DEFAULT_SHOW_TIMER,
            )
            AnimatedVisibility(visible = timerEnabled || viewModel.endGame) {
                TopBoardSection(viewModel.timeText)
            }
        }
    }
}

private data class GameBoardPrefs(
    val remainingUse: Boolean,
    val highlightIdentical: Boolean,
    val positionLines: Boolean,
    val boardBlur: Dp,
    val scale: Float,
    val crossHighlight: Boolean,
    val errorHighlight: Int,
    val fontSizeValue: TextUnit,
)

@Composable
private fun rememberGameBoardPrefs(viewModel: GameViewModel): GameBoardPrefs {
    val remainingUse by viewModel.remainingUse.collectAsStateWithLifecycle(
        initialValue = PreferencesConstants.DEFAULT_REMAINING_USES,
    )
    val highlightIdentical by viewModel.identicalHighlight.collectAsStateWithLifecycle(
        initialValue = PreferencesConstants.DEFAULT_HIGHLIGHT_IDENTICAL,
    )
    val positionLines by viewModel.positionLines.collectAsStateWithLifecycle(
        initialValue = PreferencesConstants.DEFAULT_POSITION_LINES,
    )
    val boardBlur by animateDpAsState(
        targetValue = if (viewModel.gamePlaying || viewModel.endGame) 0.dp else 10.dp,
    )
    val scale by animateFloatAsState(
        targetValue = if (viewModel.gamePlaying || viewModel.endGame) 1f else BOARD_DIM_SCALE,
    )
    val crossHighlight by viewModel.crossHighlight.collectAsStateWithLifecycle(
        initialValue = PreferencesConstants.DEFAULT_BOARD_CROSS_HIGHLIGHT,
    )
    val errorHighlight by viewModel.mistakesMethod.collectAsStateWithLifecycle(
        initialValue = PreferencesConstants.DEFAULT_HIGHLIGHT_MISTAKES,
    )
    val fontSizeFactor by viewModel.fontSize.collectAsStateWithLifecycle(
        initialValue = PreferencesConstants.DEFAULT_FONT_SIZE_FACTOR,
    )
    val fontSizeValue by remember(fontSizeFactor, viewModel.gameType) {
        mutableStateOf(
            viewModel.getFontSize(factor = fontSizeFactor),
        )
    }
    return GameBoardPrefs(
        remainingUse,
        highlightIdentical,
        positionLines,
        boardBlur,
        scale,
        crossHighlight,
        errorHighlight,
        fontSizeValue,
    )
}

@Composable
internal fun GameBoardArea(
    viewModel: GameViewModel,
    renderNotesState: MutableState<Boolean>,
    localView: View,
) {
    val prefs = rememberGameBoardPrefs(viewModel)
    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
    ) {
        GameSudokuBoard(viewModel, prefs, renderNotesState, localView)
    }
}

@Composable
private fun GameSudokuBoard(
    viewModel: GameViewModel,
    prefs: GameBoardPrefs,
    renderNotesState: MutableState<Boolean>,
    localView: View,
) {
    val renderNotes = renderNotesState.value
    Board(
        modifier =
            Modifier
                .testTag("sudoku_board")
                .blur(prefs.boardBlur)
                .scale(prefs.scale, prefs.scale),
        data =
            BoardData(
                board = if (!viewModel.showSolution) viewModel.gameBoard else viewModel.solvedBoard,
                size = viewModel.size,
                notes = viewModel.notes,
            ),
        interaction =
            BoardInteraction(
                selectedCell = viewModel.currCell,
                onClick = { cell ->
                    viewModel.processInput(
                        cell = cell,
                        remainingUse = prefs.remainingUse,
                    )
                },
                onLongClick = { cell ->
                    if (viewModel.processInput(cell, prefs.remainingUse, longTap = true)) {
                        localView.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                    }
                },
            ),
        style =
            BoardStyle(
                boardColors = LocalBoardColors.current,
                textSizes = BoardTextSizes(mainTextSize = prefs.fontSizeValue),
                displayOptions =
                    BoardDisplayOptions(
                        identicalNumbersHighlight = prefs.highlightIdentical,
                        errorsHighlight = prefs.errorHighlight != 0,
                        positionLines = prefs.positionLines,
                        enabled = viewModel.gamePlaying && !viewModel.endGame,
                        questions =
                            !(viewModel.gamePlaying || viewModel.endGame) &&
                                Build.VERSION.SDK_INT < Build.VERSION_CODES.R,
                        renderNotes = renderNotes && !viewModel.showSolution,
                        zoomable = viewModel.gameType == GameType.Default12x12,
                        crossHighlight = prefs.crossHighlight,
                    ),
            ),
    )
}

@Composable
internal fun GameBottomContent(
    viewModel: GameViewModel,
    renderNotesState: MutableState<Boolean>,
    localView: View,
    mistakesLimit: Boolean,
) {
    AnimatedContent(
        !viewModel.endGame,
        label = "this_label_makes_no_sense_to_me_but_i_added_to_overcome_a_warning",
    ) { contentState ->
        if (contentState) {
            GamePlayingContent(viewModel, renderNotesState, localView)
        } else {
            GameCompletedStats(viewModel, mistakesLimit)
        }
    }
}

@Composable
internal fun GameDialogs(
    viewModel: GameViewModel,
    resetTimer: Boolean,
    restartButtonAngleState: MutableFloatState,
) {
    if (viewModel.restartDialog) {
        viewModel.pauseTimer()
        AlertDialog(
            title = { Text(stringResource(R.string.action_reset_game)) },
            text = { Text(stringResource(R.string.reset_game_text)) },
            dismissButton = {
                TextButton(onClick = {
                    viewModel.restartDialog = false
                    viewModel.startTimer()
                }) {
                    Text(stringResource(R.string.dialog_no))
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    restartButtonAngleState.floatValue -= ROTATE_ICON_FULL_DEGREES
                    viewModel.resetGame(resetTimer)
                    viewModel.restartDialog = false
                    viewModel.startTimer()
                }) {
                    Text(stringResource(R.string.dialog_yes))
                }
            },
            onDismissRequest = {
                viewModel.restartDialog = false
                viewModel.startTimer()
            },
        )
    } else if (viewModel.giveUpDialog) {
        viewModel.pauseTimer()
        AlertDialog(
            title = { Text(stringResource(R.string.action_give_up)) },
            text = { Text(stringResource(R.string.give_up_text)) },
            dismissButton = {
                TextButton(onClick = {
                    viewModel.giveUpDialog = false
                    viewModel.startTimer()
                }) {
                    Text(stringResource(R.string.dialog_no))
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.giveUp()
                    viewModel.giveUpDialog = false
                    viewModel.pauseTimer()
                }) {
                    Text(stringResource(R.string.dialog_yes))
                }
            },
            onDismissRequest = {
                viewModel.giveUpDialog = false
                viewModel.startTimer()
            },
        )
    }
}
