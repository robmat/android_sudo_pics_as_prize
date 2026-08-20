package com.batodev.sudoku.data.database.repository

import com.batodev.sudoku.core.qqwing.GameDifficulty
import com.batodev.sudoku.data.database.dao.BoardDao
import com.batodev.sudoku.data.database.model.SavedGame
import com.batodev.sudoku.data.database.model.SudokuBoard
import com.batodev.sudoku.domain.repository.BoardReadRepository
import kotlinx.coroutines.flow.Flow

class BoardReadRepositoryImpl(
    private val boardDao: BoardDao
) : BoardReadRepository {
    override fun getAll(): Flow<List<SudokuBoard>> =
        boardDao.getAll()

    override fun getAll(difficulty: GameDifficulty): Flow<List<SudokuBoard>> =
        boardDao.getAll(difficulty)

    override fun getAllInFolder(folderUid: Long): Flow<List<SudokuBoard>> =
        boardDao.getAllInFolder(folderUid)

    override fun getAllInFolderList(folderUid: Long): List<SudokuBoard> =
        boardDao.getAllInFolderList(folderUid)

    override fun getWithSavedGames(): Flow<Map<SudokuBoard, SavedGame?>> =
        boardDao.getBoardsWithSavedGames()

    override fun getWithSavedGames(difficulty: GameDifficulty): Flow<Map<SudokuBoard, SavedGame?>> =
        boardDao.getBoardsWithSavedGames(difficulty)

    override fun getInFolderWithSaved(folderUid: Long): Flow<Map<SudokuBoard, SavedGame?>> =
        boardDao.getInFolderWithSaved(folderUid)

    override fun getBoardsInFolder(uid: Long): List<SudokuBoard> = boardDao.getBoardsInFolder(uid)
    override fun getBoardsInFolderFlow(uid: Long): Flow<List<SudokuBoard>> =
        boardDao.getBoardsInFolderFlow(uid)

    override suspend fun get(uid: Long): SudokuBoard = boardDao.get(uid)
}
