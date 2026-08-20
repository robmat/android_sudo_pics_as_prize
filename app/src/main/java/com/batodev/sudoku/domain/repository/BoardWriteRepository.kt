package com.batodev.sudoku.domain.repository

import com.batodev.sudoku.data.database.model.SudokuBoard

interface BoardWriteRepository {
    suspend fun insert(boards: List<SudokuBoard>)
    suspend fun insert(board: SudokuBoard): Long
    suspend fun delete(board: SudokuBoard)
    suspend fun delete(boards: List<SudokuBoard>)
    suspend fun update(board: SudokuBoard)
    suspend fun update(boards: List<SudokuBoard>)
}
