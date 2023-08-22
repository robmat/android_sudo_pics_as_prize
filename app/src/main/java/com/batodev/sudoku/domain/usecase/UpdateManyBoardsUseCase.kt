package com.batodev.sudoku.domain.usecase

import com.batodev.sudoku.data.database.model.SudokuBoard
import com.batodev.sudoku.domain.repository.BoardRepository
import javax.inject.Inject

class UpdateManyBoardsUseCase @Inject constructor(
    private val boardRepository: BoardRepository
) {
    suspend operator fun invoke(boards: List<SudokuBoard>) = boardRepository.update(boards)
}