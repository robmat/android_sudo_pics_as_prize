package com.batodev.sudoku.data.database.dao

import androidx.room.Dao

@Dao
interface BoardDao :
    BoardReadDao,
    BoardWithSavedGamesReadDao,
    BoardWriteDao
