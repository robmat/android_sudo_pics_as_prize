package com.batodev.sudoku.ui.folders

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.batodev.sudoku.core.parser.SdmParser
import com.batodev.sudoku.data.database.model.Folder
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.ZonedDateTime
import javax.inject.Inject

private const val LAST_SAVED_GAMES_COUNT = 10

@HiltViewModel
class FoldersViewModel
    @Inject
    constructor(
        private val dependencies: FoldersDependencies,
    ) : ViewModel() {
        val folders = dependencies.getFoldersUseCase()

        var selectedFolder: Folder? by mutableStateOf(null)

        private val _sudokuListToImport = MutableStateFlow(emptyList<String>())
        val sudokuListToImport = _sudokuListToImport.asStateFlow()

        var puzzlesCountInFolder by mutableStateOf(emptyList<Pair<Long, Int>>())

        val lastSavedGames = dependencies.getLastSavedGamesAnyFolderUseCase(gamesCount = LAST_SAVED_GAMES_COUNT)

        fun createFolder(name: String) {
            viewModelScope.launch {
                dependencies.writeUseCases.insertFolderUseCase(
                    Folder(
                        uid = 0,
                        name = name.trim(),
                        createdAt = ZonedDateTime.now(),
                    ),
                )
            }
        }

        fun countPuzzlesInFolders(folders: List<Folder>) {
            viewModelScope.launch(Dispatchers.IO) {
                val numberPuzzles = mutableListOf<Pair<Long, Int>>()
                folders.forEach {
                    numberPuzzles.add(
                        Pair(it.uid, dependencies.countPuzzlesFolderUseCase(it.uid).toInt()),
                    )
                }
                puzzlesCountInFolder = numberPuzzles
            }
        }

        fun renameFolder(newName: String) {
            selectedFolder?.let {
                viewModelScope.launch {
                    dependencies.writeUseCases.updateFolderUseCase(
                        it.copy(name = newName.trim()),
                    )
                }
            }
        }

        fun deleteFolder() {
            selectedFolder?.let {
                viewModelScope.launch {
                    dependencies.writeUseCases.deleteFolderUseCase(it)
                }
            }
        }

        fun generateFolderExportData(): ByteArray {
            val gamesInFolder = dependencies.getGamesInFolderUseCase(selectedFolder!!.uid)

            val sdmParser = SdmParser()
            return sdmParser.boardsToSdm(gamesInFolder).toByteArray()
        }
    }
