package com.batodev.sudoku.data.database.dao

import androidx.room.Query
import com.batodev.sudoku.core.qqwing.GameDifficulty
import com.batodev.sudoku.data.database.model.SudokuBoard
import kotlinx.coroutines.flow.Flow

interface BoardReadDao {
    @Query("SELECT * FROM board")
    fun getAll(): Flow<List<SudokuBoard>>

    @Query("SELECT * FROM board WHERE difficulty == :gameDifficulty")
    fun getAll(gameDifficulty: GameDifficulty): Flow<List<SudokuBoard>>

    @Query("SELECT * FROM board WHERE folder_id == :folderUid")
    fun getAllInFolder(folderUid: Long): Flow<List<SudokuBoard>>

    @Query("SELECT * FROM board WHERE folder_id == :folderUid")
    fun getAllInFolderList(folderUid: Long): List<SudokuBoard>

    @Query("SELECT * FROM board")
    fun getAllList(): List<SudokuBoard>

    @Query("SELECT * FROM board WHERE folder_id == :uid")
    fun getBoardsInFolderFlow(uid: Long): Flow<List<SudokuBoard>>

    @Query("SELECT * FROM board WHERE folder_id == :uid")
    fun getBoardsInFolder(uid: Long): List<SudokuBoard>

    @Query("SELECT * FROM board WHERE uid == :uid")
    fun get(uid: Long): SudokuBoard
}
