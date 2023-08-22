package com.batodev.sudoku.domain.usecase.folder

import com.batodev.sudoku.data.database.model.Folder
import com.batodev.sudoku.domain.repository.FolderRepository
import javax.inject.Inject

class InsertFolderUseCase @Inject constructor(
    private val folderRepository: FolderRepository
) {
    suspend operator fun invoke(folder: Folder) = folderRepository.insert(folder)
}