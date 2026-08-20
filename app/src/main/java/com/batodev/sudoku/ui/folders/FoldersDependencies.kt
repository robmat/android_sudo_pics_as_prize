package com.batodev.sudoku.ui.folders

import com.batodev.sudoku.domain.usecase.board.GetGamesInFolderUseCase
import com.batodev.sudoku.domain.usecase.folder.CountPuzzlesFolderUseCase
import com.batodev.sudoku.domain.usecase.folder.DeleteFolderUseCase
import com.batodev.sudoku.domain.usecase.folder.GetFoldersUseCase
import com.batodev.sudoku.domain.usecase.folder.GetLastSavedGamesAnyFolderUseCase
import com.batodev.sudoku.domain.usecase.folder.InsertFolderUseCase
import com.batodev.sudoku.domain.usecase.folder.UpdateFolderUseCase
import javax.inject.Inject

class FoldersWriteUseCases @Inject constructor(
    val insertFolderUseCase: InsertFolderUseCase,
    val updateFolderUseCase: UpdateFolderUseCase,
    val deleteFolderUseCase: DeleteFolderUseCase
)

class FoldersDependencies @Inject constructor(
    val getFoldersUseCase: GetFoldersUseCase,
    val getGamesInFolderUseCase: GetGamesInFolderUseCase,
    val countPuzzlesFolderUseCase: CountPuzzlesFolderUseCase,
    val getLastSavedGamesAnyFolderUseCase: GetLastSavedGamesAnyFolderUseCase,
    val writeUseCases: FoldersWriteUseCases
)
