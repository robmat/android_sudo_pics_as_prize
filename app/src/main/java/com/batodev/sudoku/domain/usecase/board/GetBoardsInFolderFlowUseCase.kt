package com.batodev.sudoku.domain.usecase.board

import com.batodev.sudoku.domain.repository.BoardRepository
import javax.inject.Inject

class GetBoardsInFolderFlowUseCase @Inject constructor(
    private val boardRepository: BoardRepository
) {
    operator fun invoke(uid: Long) = boardRepository.getBoardsInFolderFlow(uid)
}