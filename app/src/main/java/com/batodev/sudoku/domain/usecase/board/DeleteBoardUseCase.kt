package com.batodev.sudoku.domain.usecase.board

import com.batodev.sudoku.data.database.model.SudokuBoard
import com.batodev.sudoku.domain.repository.BoardRepository
import javax.inject.Inject

class DeleteBoardUseCase @Inject constructor(
    private val boardRepository: BoardRepository
) {
    suspend operator fun invoke(board: SudokuBoard) = boardRepository.delete(board)
}