package com.batodev.sudoku.data.database.dao

import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Update
import com.batodev.sudoku.data.database.model.SudokuBoard

interface BoardWriteDao {
    @Insert
    suspend fun insert(boards: List<SudokuBoard>)

    @Insert
    suspend fun insert(board: SudokuBoard): Long

    @Delete
    suspend fun delete(board: SudokuBoard)

    @Delete
    suspend fun delete(boards: List<SudokuBoard>)

    @Update
    suspend fun update(board: SudokuBoard)

    @Update
    suspend fun update(boards: List<SudokuBoard>)
}
