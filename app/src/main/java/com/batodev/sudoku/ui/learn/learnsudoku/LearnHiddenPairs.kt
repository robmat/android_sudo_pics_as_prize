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

private const val LEARN_HIDDEN_PAIRS_BOARD =
    "..............................3.1.......2........................................"
private const val LEARN_HIDDEN_PAIRS_NOTES_INITIAL =
    "3,4,4;3,4,5;3,4,8;4,3,4;4,3,5;4,3,7;4,5,4;4,5,5;5,3,6;5,3,7;5,3,8;5,3,9;5,4,7;5,4,8;5,5,6;5,5,8;5,5,9;"
private const val LEARN_HIDDEN_PAIRS_NOTES_REDUCED =
    "3,4,4;3,4,5;3,4,8;4,3,4;4,3,5;4,3,7;4,5,4;4,5,5;5,3,6;5,3,9;5,4,7;5,4,8;5,5,6;5,5,9;"

@Composable
fun LearnHiddenPairs(helpNavController: NavController) {
    TutorialBase(
        title = stringResource(R.string.learn_hidden_pairs_title),
        helpNavController = helpNavController,
    ) {
        val sudokuParser = SudokuParser()
        val board by remember {
            mutableStateOf(
                sudokuParser
                    .parseBoard(
                        LEARN_HIDDEN_PAIRS_BOARD,
                        GameType.Default9x9,
                        emptySeparator = '.',
                    ).toList(),
            )
        }
        var notes by remember {
            mutableStateOf(sudokuParser.parseNotes(LEARN_HIDDEN_PAIRS_NOTES_INITIAL))
        }
        val steps =
            listOf(
                stringResource(R.string.learn_hidden_pairs_1),
                stringResource(R.string.learn_hidden_pairs_2),
            )
        val stepsCell =
            listOf(
                listOf(Cell(row = 5, col = 3), Cell(row = 5, col = 5)),
            )
        var step by remember { mutableIntStateOf(0) }
        LaunchedEffect(key1 = step) {
            when (step) {
                0 -> notes = sudokuParser.parseNotes(LEARN_HIDDEN_PAIRS_NOTES_INITIAL)
                1 -> notes = sudokuParser.parseNotes(LEARN_HIDDEN_PAIRS_NOTES_REDUCED)
            }
        }

        TutorialBoardStepContent(
            data = TutorialStepData(board = board, steps = steps, stepsCell = stepsCell, notes = notes),
            step = step,
            onStepChange = { step = it },
        )
    }
}
