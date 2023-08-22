package com.batodev.sudoku.domain.usecase.folder

import com.batodev.sudoku.domain.repository.FolderRepository
import javax.inject.Inject

class GetLastSavedGamesAnyFolderUseCase @Inject constructor(
    private val folderRepository: FolderRepository
) {
    operator fun invoke(gamesCount: Int) = folderRepository.getLastSavedGamesAnyFolder(gamesCount)
}