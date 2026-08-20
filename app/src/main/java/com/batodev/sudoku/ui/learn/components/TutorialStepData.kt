package com.batodev.sudoku.ui.learn.components

import com.batodev.sudoku.core.Cell
import com.batodev.sudoku.core.Note

/** The puzzle/step data a [TutorialBoardStepContent] needs: the [board], its per-step [steps]
 * text, the cells to highlight for each step ([stepsCell]), and optional pencil-mark [notes]. */
data class TutorialStepData(
    val board: List<List<Cell>>,
    val steps: List<String>,
    val stepsCell: List<List<Cell>>,
    val notes: List<Note>? = null,
)
