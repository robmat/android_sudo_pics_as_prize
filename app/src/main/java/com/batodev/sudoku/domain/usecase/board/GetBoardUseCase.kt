package com.batodev.sudoku.domain.usecase.board

import com.batodev.sudoku.domain.repository.BoardRepository
import javax.inject.Inject

class GetBoardUseCase @Inject constructor(
    private val boardRepository: BoardRepository
) {
    suspend operator fun invoke(uid: Long) = boardRepository.get(uid)
}