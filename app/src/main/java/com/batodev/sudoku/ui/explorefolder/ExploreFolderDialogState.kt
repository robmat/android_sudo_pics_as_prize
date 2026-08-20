package com.batodev.sudoku.ui.explorefolder

import androidx.compose.runtime.MutableState
import com.batodev.sudoku.data.database.model.SudokuBoard

internal data class ExploreFolderDialogState(
    val addSudokuBottomSheet: MutableState<Boolean>,
    val moveSelectedDialog: MutableState<Boolean>,
    val deleteBoardDialog: MutableState<Boolean>,
    val deleteBoardDialogBoard: MutableState<SudokuBoard?>
)
