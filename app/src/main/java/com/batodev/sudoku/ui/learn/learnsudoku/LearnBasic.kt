package com.batodev.sudoku.ui.learn.learnsudoku

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import com.batodev.sudoku.R
import com.batodev.sudoku.core.Cell
import com.batodev.sudoku.core.qqwing.GameType
import com.batodev.sudoku.core.utils.SudokuParser
import com.batodev.sudoku.ui.learn.components.TutorialBase
import com.batodev.sudoku.ui.learn.components.TutorialBoardStepContent
import com.batodev.sudoku.ui.learn.components.TutorialStepData

private const val STEP_INITIAL_BOARD = 0
private const val STEP_EIGHT_PLACED = 3
private const val STEP_FOUR_PLACED = 5

private const val LEARN_BASIC_BOARD_INITIAL =
    "2...7..38.....6.7.3...4.6....8.2.7..1.......6..7.3.4....4.8...9.6.4.....91..6...2"
private const val LEARN_BASIC_BOARD_EIGHT_PLACED =
    "2...7..38.....6.7.3...4.6....8.2.7..1.......6..7.3.4....4.8...986.4.....91..6...2"
private const val LEARN_BASIC_BOARD_FOUR_PLACED =
    "24..7..38.....6.7.3...4.6....8.2.7..1.......6..7.3.4....4.8...986.4.....91..6...2"

private fun learnBasicBoardStringFor(step: Int): String? = when (step) {
    STEP_INITIAL_BOARD -> LEARN_BASIC_BOARD_INITIAL
    STEP_EIGHT_PLACED -> LEARN_BASIC_BOARD_EIGHT_PLACED
    STEP_FOUR_PLACED -> LEARN_BASIC_BOARD_FOUR_PLACED
    else -> null
}

private fun learnBasicStepCells(): List<List<Cell>> = listOf(
    listOf(
        Cell(row = 6, col = 0),
        Cell(row = 6, col = 1),
        Cell(row = 6, col = 2),
        Cell(row = 7, col = 0),
        Cell(row = 7, col = 1),
        Cell(row = 7, col = 2),
        Cell(row = 8, col = 0),
        Cell(row = 8, col = 1),
        Cell(row = 8, col = 2),
    ),
    listOf(Cell(row = 3, col = 2), Cell(row = 7, col = 2), Cell(row = 8, col = 2)),
    listOf(Cell(row = 6, col = 4), Cell(row = 6, col = 0), Cell(row = 6, col = 1)),
    listOf(Cell(row = 7, col = 0)),
    listOf(
        Cell(row = 6, col = 2),
        Cell(row = 2, col = 4),
        Cell(row = 5, col = 6),
        Cell(row = 0, col = 2),
        Cell(row = 0, col = 3),
        Cell(row = 0, col = 5),
        Cell(row = 0, col = 6),
    ),
    listOf(Cell(row = 0, col = 1))
)

@Composable
fun LearnBasic(
    helpNavController: NavController
) {
    TutorialBase(
        title = stringResource(R.string.learn_basic_title),
        helpNavController = helpNavController
    ) {
        val sudokuParser = SudokuParser()
        var board by remember {
            mutableStateOf(
                sudokuParser.parseBoard(
                    board = LEARN_BASIC_BOARD_INITIAL,
                    gameType = GameType.Default9x9,
                    emptySeparator = '.'
                ).toList()
            )
        }
        val steps = listOf(
            stringResource(R.string.learn_basic_1),
            stringResource(R.string.learn_basic_2),
            stringResource(R.string.learn_basic_3),
            stringResource(R.string.learn_basic_4),
            stringResource(R.string.learn_basic_5),
            stringResource(R.string.learn_basic_6),
        )
        val stepsCell = learnBasicStepCells()
        var step by remember { mutableIntStateOf(0) }
        LaunchedEffect(key1 = step) {
            learnBasicBoardStringFor(step)?.let {
                board = sudokuParser.parseBoard(it, GameType.Default9x9, emptySeparator = '.')
            }
        }

        TutorialBoardStepContent(
            data = TutorialStepData(board = board, steps = steps, stepsCell = stepsCell),
            step = step,
            onStepChange = { step = it }
        )
    }
}
