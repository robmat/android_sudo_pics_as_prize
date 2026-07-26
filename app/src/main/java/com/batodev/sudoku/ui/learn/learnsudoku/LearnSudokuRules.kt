package com.batodev.sudoku.ui.learn.learnsudoku

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.batodev.sudoku.LocalBoardColors
import com.batodev.sudoku.R
import com.batodev.sudoku.core.Cell
import com.batodev.sudoku.core.qqwing.GameType
import com.batodev.sudoku.core.utils.SudokuParser
import com.batodev.sudoku.ui.components.board.Board
import com.batodev.sudoku.ui.learn.components.TutorialBase

@Composable
fun LearnSudokuRules(
    helpNavController: NavController
) {
    TutorialBase(
        title = stringResource(R.string.learn_sudoku_rules),
        helpNavController = helpNavController
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            var selectedCell by remember { mutableStateOf(Cell(-1, -1, 0)) }
            var secondSelectedCell by remember { mutableStateOf(Cell(-1, -1, 0)) }

            val sudokuParser = SudokuParser()
            val previewBoard by remember {
                mutableStateOf(
                    sudokuParser.parseBoard(
                        board = "...6.....824753169...2........5..471...1..386...4..925...3........9........8.....",
                        gameType = GameType.Default9x9,
                        emptySeparator = '.'
                    )
                )
            }

            Text(stringResource(R.string.intro_what_is_sudoku))
            Text(stringResource(R.string.intro_rules))
            Board(
                board = previewBoard,
                size = 9,
                selectedCell = selectedCell,
                onClick = { selectedCell = it },
                boardColors = LocalBoardColors.current
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(stringResource(R.string.sudoku_rules_mistakes))

            var highlightError by remember { mutableStateOf(false) }
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = highlightError,
                    onCheckedChange = { highlightError = !highlightError }
                )
                Text(stringResource(R.string.sudoku_rules_mistakes_highlight))
            }

            val errorBoard by remember {
                mutableStateOf(previewBoard.map { cells -> cells.map { cell -> cell.copy() } })
            }
            Board(
                board = errorBoard.also {
                    it[1][7].apply {
                        value = 6
                        error = true
                    }
                    it[3][6].apply {
                        value = 2
                        error = true
                    }
                    it[4][7].apply {
                        value = 6
                    }
                    it[4][8].apply {
                        value = 8
                    }
                },
                size = 9,
                errorsHighlight = highlightError,
                selectedCell = secondSelectedCell,
                onClick = { secondSelectedCell = it },
                boardColors = LocalBoardColors.current
            )
            Text(stringResource(R.string.sudoku_rules_mistakes_explanation))
        }
    }
}
