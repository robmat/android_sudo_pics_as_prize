package com.batodev.sudoku.domain.usecase.board

import com.batodev.sudoku.data.database.model.SudokuBoard
import com.batodev.sudoku.domain.repository.BoardRepository
import javax.inject.Inject

class DeleteBoardsUseCase @Inject constructor(
    private val boardRepository: BoardRepository
) {
    suspend operator fun invoke(boards: List<SudokuBoard>) = boardRepository.delete(boards)
}