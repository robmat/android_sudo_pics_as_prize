package com.batodev.sudoku.ui.learn.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.batodev.sudoku.LocalBoardColors
import com.batodev.sudoku.R
import com.batodev.sudoku.core.Cell
import com.batodev.sudoku.ui.components.board.Board
import com.batodev.sudoku.ui.components.board.BoardData
import com.batodev.sudoku.ui.components.board.BoardInteraction
import com.batodev.sudoku.ui.components.board.BoardStyle

/**
 * The board + step navigation section shared by every step-by-step sudoku tutorial screen
 * (see the `LearnBasic`/`LearnHiddenPairs`/`LearnNakedPairs` screens): a [Board] highlighting the
 * cells relevant to the current [step], followed by [TutorialBottomContent] for navigating steps.
 */
@Composable
fun TutorialBoardStepContent(
    data: TutorialStepData,
    step: Int,
    onStepChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(horizontal = 12.dp),
    ) {
        Board(
            data = BoardData(board = data.board, notes = data.notes),
            interaction =
                BoardInteraction(
                    selectedCell = Cell(-1, -1),
                    onClick = { },
                    cellsToHighlight = if (step < data.stepsCell.size) data.stepsCell[step] else null,
                ),
            style = BoardStyle(boardColors = LocalBoardColors.current),
        )
        TutorialBottomContent(
            steps = data.steps,
            step = step,
            onPreviousClick = { if (step > 0) onStepChange(step - 1) },
            onNextClick = { if (step < (data.steps.size - 1)) onStepChange(step + 1) },
        )
    }
}

@Composable
@OptIn(ExperimentalAnimationApi::class)
fun TutorialBottomContent(
    steps: List<String>,
    step: Int,
    onPreviousClick: () -> Unit,
    onNextClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .padding(top = 8.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        AnimatedContent(targetState = steps[step]) { stepText ->
            Column {
                Text(stepText)
            }
        }
        Row(
            modifier =
                Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(bottom = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            AnimatedVisibility(
                visible = step > 0,
                enter = fadeIn() + slideInVertically(),
                exit = fadeOut() + slideOutVertically { it },
            ) {
                FilledTonalButton(onClick = onPreviousClick) {
                    Text(stringResource(R.string.page_previous))
                }
            }
            Spacer(modifier = Modifier.weight(1f))
            AnimatedVisibility(
                visible = step < steps.size - 1,
                enter = fadeIn() + slideInVertically(),
                exit = fadeOut() + slideOutVertically { it },
            ) {
                FilledTonalButton(onClick = onNextClick) {
                    Text(stringResource(R.string.page_next))
                }
            }
        }
    }
}
