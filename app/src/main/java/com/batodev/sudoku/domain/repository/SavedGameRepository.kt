package com.batodev.sudoku.domain.repository

import com.batodev.sudoku.data.database.model.SavedGame
import com.batodev.sudoku.data.database.model.SudokuBoard
import kotlinx.coroutines.flow.Flow

interface SavedGameRepository {
    fun getAll(): Flow<List<SavedGame>>
    suspend fun get(uid: Long): SavedGame?
    fun getWithBoards(): Flow<Map<SavedGame, SudokuBoard>>
    fun getLast(): Flow<SavedGame?>
    suspend fun insert(savedGame: SavedGame): Long
    suspend fun update(savedGame: SavedGame)
    suspend fun delete(savedGame: SavedGame)
}