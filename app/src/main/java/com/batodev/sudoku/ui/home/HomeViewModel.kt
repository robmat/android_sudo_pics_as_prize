package com.batodev.sudoku.ui.home

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.batodev.sudoku.core.Cell
import com.batodev.sudoku.core.PreferencesConstants
import com.batodev.sudoku.core.qqwing.GameDifficulty
import com.batodev.sudoku.core.qqwing.GameType
import com.batodev.sudoku.core.qqwing.QQWingController
import com.batodev.sudoku.core.utils.SudokuParser
import com.batodev.sudoku.data.database.model.SudokuBoard
import com.batodev.sudoku.data.datastore.AppSettingsManager
import com.batodev.sudoku.data.settings.SettingsHelper
import com.batodev.sudoku.domain.repository.BoardRepository
import com.batodev.sudoku.domain.repository.SavedGameRepository
import com.batodev.sudoku.ui.gallery.PRIZE_IMAGES
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class HomeViewModel
@Inject constructor(
    private val appSettingsManager: AppSettingsManager,
    private val boardRepository: BoardRepository,
    private val savedGameRepository: SavedGameRepository,
    private val context: Application,
) : ViewModel() {

    val lastSavedGame = savedGameRepository.getLast()
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    var insertedBoardUid = -1L

    private val difficulties = listOf(
        GameDifficulty.Easy,
        GameDifficulty.Moderate,
        GameDifficulty.Hard,
        GameDifficulty.Challenge,
    )

    private val types = listOf(
        GameType.Default9x9,
        GameType.Default6x6,
        GameType.Default12x12
    )

    val lastSelectedGameDifficultyType = appSettingsManager.lastSelectedGameDifficultyType
        .stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            initialValue = Pair(difficulties.first(), types.first())
        )

    val saveSelectedGameDifficultyType = appSettingsManager.saveSelectedGameDifficultyType
        .stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            initialValue = PreferencesConstants.DEFAULT_SAVE_LAST_SELECTED_DIFF_TYPE
        )

    var selectedDifficulty by mutableStateOf(difficulties.first())
    var selectedType by mutableStateOf(types.first())

    var isGenerating by mutableStateOf(false)
    var isSolving by mutableStateOf(false)
    var readyToPlay by mutableStateOf(false)
    var prizeImageName by mutableStateOf("")

    private var puzzle =
        List(selectedType.size) { row -> List(selectedType.size) { col -> Cell(row, col, 0) } }
    private var solvedPuzzle =
        List(selectedType.size) { row -> List(selectedType.size) { col -> Cell(row, col, 0) } }


    fun startGame() {
        isSolving = false
        isGenerating = false

        val gameTypeToGenerate = selectedType
        val gameDifficultyToGenerate = selectedDifficulty
        val size = gameTypeToGenerate.size

        puzzle = List(size) { row -> List(size) { col -> Cell(row, col, 0) } }
        solvedPuzzle = List(size) { row -> List(size) { col -> Cell(row, col, 0) } }

        val allImages = context.assets.list(PRIZE_IMAGES)!!.toMutableList()
        val settingsHelper = SettingsHelper(context)
        allImages.removeAll(settingsHelper.preferences.uncoveredPics)
        prizeImageName =
            if (allImages.isEmpty()) settingsHelper.preferences.uncoveredPics.random() else allImages.random()

        viewModelScope.launch(Dispatchers.Default) {
            if (saveSelectedGameDifficultyType.value) {
                appSettingsManager.setLastSelectedGameDifficultyType(
                    difficulty = selectedDifficulty,
                    type = selectedType
                )
            }

            val qqWingController = QQWingController()

            // generating
            isGenerating = true
            val generated = qqWingController.generate(gameTypeToGenerate, gameDifficultyToGenerate)
            isGenerating = false

            isSolving = true
            val solved = qqWingController.solve(generated, gameTypeToGenerate)
            isSolving = false

            if (!qqWingController.isImpossible && qqWingController.solutionCount == 1) {
                for (i in 0 until size) {
                    for (j in 0 until size) {
                        puzzle[i][j].value = generated[i * size + j]
                        solvedPuzzle[i][j].value = solved[i * size + j]
                    }
                }

                withContext(Dispatchers.IO) {
                    val sudokuParser = SudokuParser()
                    insertedBoardUid = boardRepository.insert(
                        SudokuBoard(
                            uid = 0,
                            initialBoard = sudokuParser.boardToString(puzzle),
                            solvedBoard = sudokuParser.boardToString(solvedPuzzle),
                            difficulty = selectedDifficulty,
                            type = selectedType,
                            prizeImageName = prizeImageName
                        )
                    )
                }

                readyToPlay = true
            }
        }
    }

    fun changeDifficulty(diff: Int) {
        val indexToSet = difficulties.indexOf(selectedDifficulty) + diff
        if (indexToSet >= 0 && indexToSet < difficulties.count()) {
            selectedDifficulty = difficulties[indexToSet]
        }
    }

    fun changeType(diff: Int) {
        val indexToSet = types.indexOf(selectedType) + diff
        if (indexToSet >= 0 && indexToSet < types.count()) {
            selectedType = types[indexToSet]
        }
    }

    fun giveUpLastGame() {
        viewModelScope.launch(Dispatchers.IO) {
            lastSavedGame.value?.let {
                if (!it.completed) {
                    savedGameRepository.update(
                        it.copy(
                            completed = true,
                            canContinue = true
                        )
                    )
                }
            }
        }
    }

    fun restoreDifficultyAndType() {
        if (saveSelectedGameDifficultyType.value) {
            selectedDifficulty = lastSelectedGameDifficultyType.value.first
            selectedType = lastSelectedGameDifficultyType.value.second
        }
    }
}