package com.batodev.sudoku.domain.repository

import com.batodev.sudoku.core.qqwing.GameDifficulty
import com.batodev.sudoku.data.database.model.SavedGame
import com.batodev.sudoku.data.database.model.SudokuBoard
import kotlinx.coroutines.flow.Flow

interface BoardRepository {
    fun getAll(): Flow<List<SudokuBoard>>
    fun getAll(difficulty: GameDifficulty): Flow<List<SudokuBoard>>

    fun getAllInFolder(folderUid: Long): Flow<List<SudokuBoard>>

    fun getAllInFolderList(folderUid: Long): List<SudokuBoard>
    fun getWithSavedGames(): Flow<Map<SudokuBoard, SavedGame?>>
    fun getWithSavedGames(difficulty: GameDifficulty): Flow<Map<SudokuBoard, SavedGame?>>
    fun getInFolderWithSaved(folderUid: Long): Flow<Map<SudokuBoard, SavedGame?>>
    fun getBoardsInFolderFlow(uid: Long): Flow<List<SudokuBoard>>
    fun getBoardsInFolder(uid: Long): List<SudokuBoard>
    suspend fun get(uid: Long): SudokuBoard
    suspend fun insert(boards: List<SudokuBoard>)
    suspend fun insert(board: SudokuBoard): Long
    suspend fun delete(board: SudokuBoard)
    suspend fun delete(boards: List<SudokuBoard>)
    suspend fun update(board: SudokuBoard)
    suspend fun update(boards: List<SudokuBoard>)
}