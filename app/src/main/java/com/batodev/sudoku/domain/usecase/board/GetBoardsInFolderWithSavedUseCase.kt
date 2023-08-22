package com.batodev.sudoku.domain.usecase.board

import com.batodev.sudoku.domain.repository.BoardRepository
import javax.inject.Inject

class GetBoardsInFolderWithSavedUseCase @Inject constructor(
    private val boardRepository: BoardRepository
){
    operator fun invoke(folderUid: Long) = boardRepository.getInFolderWithSaved(folderUid)
}