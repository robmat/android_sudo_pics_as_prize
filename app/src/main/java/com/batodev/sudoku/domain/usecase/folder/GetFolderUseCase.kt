package com.batodev.sudoku.domain.usecase.folder

import com.batodev.sudoku.domain.repository.FolderRepository
import javax.inject.Inject

class GetFolderUseCase @Inject constructor(
    private val folderRepository: FolderRepository
) {
    operator fun invoke(uid: Long) = folderRepository.get(uid)
}