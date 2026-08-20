package com.batodev.sudoku.ui.explorefolder

import androidx.compose.foundation.lazy.LazyListState
import com.batodev.sudoku.data.database.model.Folder
import com.batodev.sudoku.data.database.model.SavedGame
import com.batodev.sudoku.data.database.model.SudokuBoard

internal data class ExploreFolderListState(
    val folder: Folder?,
    val games: Map<SudokuBoard, SavedGame?>,
    val lazyListState: LazyListState
)
