package com.batodev.sudoku.ui.game

import com.batodev.sudoku.domain.repository.RecordRepository
import com.batodev.sudoku.domain.repository.SavedGameRepository
import com.batodev.sudoku.domain.usecase.board.GetBoardUseCase
import com.batodev.sudoku.domain.usecase.board.UpdateBoardUseCase
import com.batodev.sudoku.domain.usecase.record.GetAllRecordsUseCase
import javax.inject.Inject

/**
 * Groups [GameViewModel]'s repository/use-case dependencies into a single injectable value so
 * the view model's own constructor doesn't need one parameter per dependency.
 */
class GameDependencies @Inject constructor(
    val savedGameRepository: SavedGameRepository,
    val recordRepository: RecordRepository,
    val updateBoardUseCase: UpdateBoardUseCase,
    val getBoardUseCase: GetBoardUseCase,
    val getAllRecordsUseCase: GetAllRecordsUseCase
)
