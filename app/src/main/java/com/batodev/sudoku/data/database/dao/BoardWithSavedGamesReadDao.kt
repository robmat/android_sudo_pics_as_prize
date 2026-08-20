package com.batodev.sudoku.data.database.dao

import androidx.room.Query
import com.batodev.sudoku.core.qqwing.GameDifficulty
import com.batodev.sudoku.data.database.model.SavedGame
import com.batodev.sudoku.data.database.model.SudokuBoard
import kotlinx.coroutines.flow.Flow

interface BoardWithSavedGamesReadDao {
    @Query(
        "SELECT * FROM board " +
            "LEFT OUTER JOIN saved_game ON board.uid = saved_game.board_uid " +
            "ORDER BY uid DESC",
    )
    fun getBoardsWithSavedGames(): Flow<Map<SudokuBoard, SavedGame?>>

    @Query(
        "SELECT * FROM board " +
            "LEFT OUTER JOIN saved_game ON board.uid = saved_game.board_uid " +
            "WHERE difficulty == :difficulty " +
            "ORDER BY uid DESC",
    )
    fun getBoardsWithSavedGames(difficulty: GameDifficulty): Flow<Map<SudokuBoard, SavedGame?>>

    @Query(
        "SELECT * FROM board " +
            "LEFT OUTER JOIN saved_game ON board.uid = saved_game.board_uid " +
            "WHERE folder_id == :folderUid " +
            "ORDER BY uid DESC",
    )
    fun getInFolderWithSaved(folderUid: Long): Flow<Map<SudokuBoard, SavedGame?>>
}
