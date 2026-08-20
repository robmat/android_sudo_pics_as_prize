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

private const val LEARN_NAKED_PAIRS_BOARD =
    ".......................................9........68..............................."
private const val LEARN_NAKED_PAIRS_NOTES_INITIAL =
    "3,3,1;3,3,2;3,3,4;3,3,5;3,4,1;3,4,2;3,4,4;3,4,5;3,4,7;3,5,2;3,5,4;3,5,5;3,5,7;" +
        "4,4,2;4,4,3;4,5,2;4,5,3;5,5,2;5,5,3;5,5,5;"
private const val LEARN_NAKED_PAIRS_NOTES_REDUCED =
    "3,3,1;3,3,4;3,3,5;3,4,1;3,4,4;3,4,5;3,4,7;3,5,4;3,5,5;3,5,7;4,4,2;4,4,3;4,5,2;4,5,3;5,5,5;"

@Composable
fun LearnNakedPairs(helpNavController: NavController) {
    TutorialBase(
        title = stringResource(R.string.naked_pairs_title),
        helpNavController = helpNavController,
    ) {
        val sudokuParser = SudokuParser()
        val board by remember {
            mutableStateOf(
                sudokuParser
                    .parseBoard(
                        board = LEARN_NAKED_PAIRS_BOARD,
                        gameType = GameType.Default9x9,
                        emptySeparator = '.',
                    ).toList(),
            )
        }
        var notes by remember {
            mutableStateOf(sudokuParser.parseNotes(LEARN_NAKED_PAIRS_NOTES_INITIAL))
        }
        val steps =
            listOf(
                stringResource(R.string.naked_pairs_explanation),
                stringResource(R.string.naked_pairs_end),
            )
        val stepsCell =
            listOf(
                listOf(Cell(row = 4, col = 4), Cell(row = 4, col = 5)),
            )
        var step by remember { mutableIntStateOf(0) }
        LaunchedEffect(key1 = step) {
            when (step) {
                0 -> notes = sudokuParser.parseNotes(LEARN_NAKED_PAIRS_NOTES_INITIAL)
                1 -> notes = sudokuParser.parseNotes(LEARN_NAKED_PAIRS_NOTES_REDUCED)
            }
        }

        TutorialBoardStepContent(
            data = TutorialStepData(board = board, steps = steps, stepsCell = stepsCell, notes = notes),
            step = step,
            onStepChange = { step = it },
        )
    }
}
