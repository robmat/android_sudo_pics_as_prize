package com.batodev.sudoku.ui.explorefolder

import com.batodev.sudoku.domain.usecase.UpdateManyBoardsUseCase
import com.batodev.sudoku.domain.usecase.board.DeleteBoardUseCase
import com.batodev.sudoku.domain.usecase.board.DeleteBoardsUseCase
import com.batodev.sudoku.domain.usecase.board.GetBoardsInFolderWithSavedUseCase
import com.batodev.sudoku.domain.usecase.board.UpdateBoardUseCase
import com.batodev.sudoku.domain.usecase.folder.GetFolderUseCase
import com.batodev.sudoku.domain.usecase.folder.GetFoldersUseCase
import javax.inject.Inject

class ExploreFolderBoardWriteUseCases @Inject constructor(
    val updateBoardUseCase: UpdateBoardUseCase,
    val updateManyBoardsUseCase: UpdateManyBoardsUseCase,
    val deleteBoardUseCase: DeleteBoardUseCase,
    val deleteBoardsUseCase: DeleteBoardsUseCase
)

class ExploreFolderDependencies @Inject constructor(
    val getFolderUseCase: GetFolderUseCase,
    val getBoardsInFolderWithSavedUseCase: GetBoardsInFolderWithSavedUseCase,
    val getFoldersUseCase: GetFoldersUseCase,
    val writeUseCases: ExploreFolderBoardWriteUseCases
)
