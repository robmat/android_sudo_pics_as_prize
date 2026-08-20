package com.batodev.sudoku.ui.game

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.batodev.sudoku.core.Cell
import com.batodev.sudoku.core.Note
import com.batodev.sudoku.core.PreferencesConstants
import com.batodev.sudoku.core.qqwing.GameDifficulty
import com.batodev.sudoku.core.qqwing.GameType
import com.batodev.sudoku.core.utils.GameState
import com.batodev.sudoku.core.utils.SudokuParser
import com.batodev.sudoku.core.utils.SudokuUtils
import com.batodev.sudoku.core.utils.UndoRedoManager
import com.batodev.sudoku.data.database.model.SudokuBoard
import com.batodev.sudoku.data.datastore.AppSettingsManager
import com.batodev.sudoku.data.datastore.ThemeSettingsManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Timer
import javax.inject.Inject
import kotlin.time.Duration

/**
 * Holds all the state for the active sudoku game screen. The behavior that operates on this
 * state lives in extension functions spread across GameViewModelBoard.kt, GameViewModelInput.kt,
 * GameViewModelNotes.kt, GameViewModelActions.kt and GameViewModelPersistence.kt, so that this
 * class itself stays focused on state declaration.
 */
@HiltViewModel
class GameViewModel
    @Inject
    constructor(
        internal val dependencies: GameDependencies,
        internal val appSettingsManager: AppSettingsManager,
        themeSettingsManager: ThemeSettingsManager,
        private val savedStateHandle: SavedStateHandle,
        internal val application: Application,
    ) : ViewModel() {
        companion object {
            internal const val TIMER_UPDATE_RATE_MS = 50L
            internal const val RADIX = 13
            internal const val NANOS_PER_MILLI = 1e6
        }

        init {
            val sudokuParser = SudokuParser()
            val continueSaved = savedStateHandle.get<Boolean>("saved")

            viewModelScope.launch(Dispatchers.IO) {
                boardEntity = dependencies.getBoardUseCase(savedStateHandle["uid"] ?: 1L)
                val savedGame = dependencies.savedGameRepository.get(boardEntity.uid)

                withContext(Dispatchers.Main) {
                    gameType = boardEntity.type
                    gameDifficulty = boardEntity.difficulty
                }

                withContext(Dispatchers.Default) {
                    initialBoard =
                        sudokuParser
                            .parseBoard(
                                boardEntity.initialBoard,
                                boardEntity.type,
                            ).toList()
                    initialBoard.forEach { cells ->
                        cells.forEach { cell ->
                            cell.locked = cell.value != 0
                        }
                    }

                    if (boardEntity.solvedBoard.isNotBlank() && !boardEntity.solvedBoard.contains("0")) {
                        solvedBoard =
                            sudokuParser.parseBoard(
                                boardEntity.solvedBoard,
                                boardEntity.type,
                            )
                        for (i in solvedBoard.indices) {
                            for (j in solvedBoard.indices) {
                                solvedBoard[i][j].locked = initialBoard[i][j].locked
                            }
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            solveBoard()
                        }
                    }
                }

                withContext(Dispatchers.Main) {
                    if (savedGame != null && continueSaved!!) {
                        restoreSavedGame(savedGame)
                    } else {
                        gameBoard = initialBoard
                    }
                    size = gameBoard.size
                    undoRedoManager = UndoRedoManager(GameState(gameBoard, notes))
                    remainingUsesList = countRemainingUses(gameBoard)
                }
                saveGame()
            }
        }

        var giveUp by mutableStateOf(false)

        val fontSize = appSettingsManager.fontSize
        val keepScreenOn = appSettingsManager.keepScreenOn

        var remainingUsesList = emptyList<Int>()
        val firstGame = appSettingsManager.firstGame
        internal lateinit var boardEntity: SudokuBoard
        var size by mutableIntStateOf(GameType.Default9x9.size)
        var gameType by mutableStateOf(GameType.Unspecified)
        var gameDifficulty by mutableStateOf(GameDifficulty.Unspecified)

        // dialogs, menus
        var restartDialog by mutableStateOf(false)
        var showMenu by mutableStateOf(false)
        var showNotesMenu by mutableStateOf(false)
        var showUndoRedoMenu by mutableStateOf(false)

        // count remaining uses
        var remainingUse = appSettingsManager.remainingUse

        // timer
        var timerEnabled = appSettingsManager.timerEnabled

        // identical numbers highlight
        val identicalHighlight = appSettingsManager.highlightIdentical

        // mistakes checking method
        var mistakesMethod =
            appSettingsManager.highlightMistakes.stateIn(
                viewModelScope,
                SharingStarted.Eagerly,
                PreferencesConstants.DEFAULT_HIGHLIGHT_MISTAKES,
            )

        var positionLines = appSettingsManager.positionLines
        val crossHighlight = themeSettingsManager.boardCrossHighlight
        val funKeyboardOverNum = appSettingsManager.funKeyboardOverNumbers

        var mistakesLimit =
            appSettingsManager.mistakesLimit.stateIn(
                viewModelScope,
                SharingStarted.Eagerly,
                PreferencesConstants.DEFAULT_MISTAKES_LIMIT,
            )

        internal var autoEraseNotesEnabled =
            appSettingsManager.autoEraseNotes.stateIn(
                viewModelScope,
                SharingStarted.Eagerly,
                PreferencesConstants.DEFAULT_AUTO_ERASE_NOTES,
            )

        var resetTimerOnRestart = appSettingsManager.resetTimerEnabled

        var disableHints = appSettingsManager.hintsDisabled

        var endGame by mutableStateOf(false)
        var giveUpDialog by mutableStateOf(false)

        // mistakes
        // used for mistakes limit
        var mistakesCount by mutableIntStateOf(0)

        // notes
        var notesToggled by mutableStateOf(false)
        var notes by mutableStateOf(emptyList<Note>())

        internal lateinit var initialBoard: List<List<Cell>>
        internal val isInitialBoardInitialized: Boolean
            get() = this::initialBoard.isInitialized
        var gameBoard by mutableStateOf(
            List(GameType.Default9x9.size) { row -> List(GameType.Default9x9.size) { col -> Cell(row, col, 0) } },
        )
        var solvedBoard = emptyList<List<Cell>>()

        var currCell by mutableStateOf(Cell(-1, -1, 0))
        internal var undoRedoManager = UndoRedoManager(GameState(gameBoard, notes))
        internal var sudokuUtils = SudokuUtils()
        var gameCompleted by mutableStateOf(false)

        // Selected number for digit first method
        var digitFirstNumber by mutableIntStateOf(0)
        internal val inputMethod =
            appSettingsManager.inputMethod
                .stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.Eagerly,
                    initialValue = PreferencesConstants.DEFAULT_INPUT_METHOD,
                )

        // temporarily use digit first method when true
        internal var overrideInputMethodDF by mutableStateOf(false)

        // show/hide solution (when give up)
        var showSolution by mutableStateOf(false)

        // when true, tapping on any cell will clear it
        var eraseButtonToggled by mutableStateOf(false)

        // used only in the game-completed section. Not saved anywhere
        var hintsUsed = 0
        var mistakesMade = 0
        var notesTaken = 0

        val allRecords by lazy { dependencies.getAllRecordsUseCase(gameDifficulty, gameType) }

        var timeText by mutableStateOf("00:00")
        internal var duration = Duration.ZERO
        internal lateinit var timer: Timer
        var gamePlaying by mutableStateOf(false)
    }
