package com.batodev.sudoku.data.database.repository

import com.batodev.sudoku.data.database.dao.BoardDao
import com.batodev.sudoku.domain.repository.BoardReadRepository
import com.batodev.sudoku.domain.repository.BoardRepository
import com.batodev.sudoku.domain.repository.BoardWriteRepository

class BoardRepositoryImpl(
    boardDao: BoardDao
) : BoardRepository,
    BoardReadRepository by BoardReadRepositoryImpl(boardDao),
    BoardWriteRepository by BoardWriteRepositoryImpl(boardDao)
