package com.batodev.sudoku.ui.explorefolder

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.batodev.sudoku.core.qqwing.QQWingController
import com.batodev.sudoku.core.utils.SudokuParser
import com.batodev.sudoku.data.database.model.SudokuBoard
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val EXPLORE_FOLDER_BOARD_CHAR_RADIX = 13

@HiltViewModel
class ExploreFolderViewModel @Inject constructor(
    private val dependencies: ExploreFolderDependencies,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val folderUid = savedStateHandle.get<Long>("uid") ?: 0

    val folder = dependencies.getFolderUseCase(folderUid)
    val games = dependencies.getBoardsInFolderWithSavedUseCase(folderUid)

    var gameUidToPlay: Long? by mutableStateOf(null)
    var isPlayedBefore by mutableStateOf(false)
    var readyToPlay by mutableStateOf(false)

    var inSelectionMode by mutableStateOf(false)
    var selectedBoardsList by mutableStateOf(emptyList<SudokuBoard>())

    val folders = dependencies.getFoldersUseCase()

    fun prepareSudokuToPlay(board: SudokuBoard) {
        gameUidToPlay = board.uid
        if (board.solvedBoard == "") {
            viewModelScope.launch {
                val qqWingController = QQWingController()
                val sudokuParser = SudokuParser()
                val boardToSolve = board.initialBoard
                    .map { it.digitToInt(EXPLORE_FOLDER_BOARD_CHAR_RADIX) }
                    .toIntArray()

                val solved = qqWingController.solve(boardToSolve, board.type)

                if (qqWingController.solutionCount == 1) {
                    dependencies.writeUseCases.updateBoardUseCase(
                        board.copy(solvedBoard = sudokuParser.boardToString(solved))
                    )
                    readyToPlay = true
                }
            }
        } else {
            isPlayedBefore = true
            readyToPlay = true
        }
    }

    fun addToSelection(board: SudokuBoard) {
        var currentSelected = selectedBoardsList
        currentSelected = if (!currentSelected.contains(board)) {
            currentSelected + board
        } else {
            currentSelected - board
        }
        selectedBoardsList = currentSelected
    }

    fun addAllToSelection(boards: List<SudokuBoard>) {
        selectedBoardsList = boards
    }

    fun deleteSelected() {
        viewModelScope.launch(Dispatchers.IO) {
            dependencies.writeUseCases.deleteBoardsUseCase(selectedBoardsList)
            selectedBoardsList = emptyList()
            inSelectionMode = false
        }
    }

    fun deleteGame(board: SudokuBoard) {
        viewModelScope.launch {
            dependencies.writeUseCases.deleteBoardUseCase(board)
        }
    }

    fun moveBoards(folderUid: Long) {
        viewModelScope.launch {
            dependencies.writeUseCases.updateManyBoardsUseCase(
                selectedBoardsList.map { it.copy(folderId = folderUid) }
            )
            selectedBoardsList = emptyList()
        }
    }
}
