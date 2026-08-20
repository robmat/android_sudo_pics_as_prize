package com.batodev.sudoku.data.database.repository

import com.batodev.sudoku.data.database.dao.BoardWriteDao
import com.batodev.sudoku.data.database.model.SudokuBoard
import com.batodev.sudoku.domain.repository.BoardWriteRepository

class BoardWriteRepositoryImpl(
    private val boardDao: BoardWriteDao,
) : BoardWriteRepository {
    override suspend fun insert(boards: List<SudokuBoard>) = boardDao.insert(boards)

    override suspend fun insert(board: SudokuBoard): Long = boardDao.insert(board)

    override suspend fun delete(board: SudokuBoard) = boardDao.delete(board)

    override suspend fun delete(boards: List<SudokuBoard>) = boardDao.delete(boards)

    override suspend fun update(board: SudokuBoard) = boardDao.update(board)

    override suspend fun update(boards: List<SudokuBoard>) = boardDao.update(boards)
}
