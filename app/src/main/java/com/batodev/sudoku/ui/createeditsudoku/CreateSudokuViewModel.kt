package com.batodev.sudoku.ui.createeditsudoku

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.TextUnit
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.batodev.sudoku.R
import com.batodev.sudoku.core.Cell
import com.batodev.sudoku.core.PreferencesConstants
import com.batodev.sudoku.core.qqwing.GameDifficulty
import com.batodev.sudoku.core.qqwing.GameType
import com.batodev.sudoku.core.qqwing.QQWingController
import com.batodev.sudoku.core.utils.DigitFirstCallbacks
import com.batodev.sudoku.core.utils.GameState
import com.batodev.sudoku.core.utils.SudokuParser
import com.batodev.sudoku.core.utils.SudokuUtils
import com.batodev.sudoku.core.utils.UndoRedoManager
import com.batodev.sudoku.core.utils.applyClearedCellBookkeeping
import com.batodev.sudoku.core.utils.getFontSize
import com.batodev.sudoku.core.utils.handleDigitFirstBranches
import com.batodev.sudoku.data.database.model.SudokuBoard
import com.batodev.sudoku.data.datastore.AppSettingsManager
import com.batodev.sudoku.data.datastore.ThemeSettingsManager
import com.batodev.sudoku.domain.usecase.board.GetBoardUseCase
import com.batodev.sudoku.domain.usecase.board.InsertBoardUseCase
import com.batodev.sudoku.domain.usecase.board.UpdateBoardUseCase
import com.batodev.sudoku.ui.game.components.ToolBarItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

private const val PUZZLE_LENGTH_6X6 = 36
private const val PUZZLE_LENGTH_9X9 = 81
private const val PUZZLE_LENGTH_12X12 = 144
private const val PUZZLE_CHAR_HEX_RADIX = 16

private fun gameTypeForPuzzleLength(length: Int): GameType? = when (length) {
    PUZZLE_LENGTH_6X6 -> GameType.Default6x6
    PUZZLE_LENGTH_9X9 -> GameType.Default9x9
    PUZZLE_LENGTH_12X12 -> GameType.Default12x12
    else -> null
}

private fun isValidPuzzleChar(char: Char): Boolean {
    if (char == '0' || char == '.') return true
    return try {
        char.digitToInt(PUZZLE_CHAR_HEX_RADIX)
        true
    } catch (_: IllegalArgumentException) {
        false
    }
}

@HiltViewModel
class CreateSudokuViewModel @Inject constructor(
    appSettingsManager: AppSettingsManager,
    themeSettingsManager: ThemeSettingsManager,
    private val getBoardUseCase: GetBoardUseCase,
    private val updateBoardUseCase: UpdateBoardUseCase,
    private val insertBoardUseCase: InsertBoardUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    val gameUid = savedStateHandle.get<Long>("game_uid") ?: -1L
    private val folderUid = savedStateHandle.get<Long>("folder_uid")

    init {
        if (gameUid != -1L) {
            viewModelScope.launch(Dispatchers.IO) {
                val board = getBoardUseCase(gameUid)
                withContext(Dispatchers.Default) {
                    val sudokuParser = SudokuParser()
                    val parsedBoard = sudokuParser.parseBoard(
                        board = board.initialBoard,
                        gameType = board.type
                    )
                    withContext(Dispatchers.Main) {
                        gameBoard = parsedBoard
                        gameDifficulty = board.difficulty
                        gameType = board.type
                    }
                }
            }
        }
    }

    val highlightIdentical = appSettingsManager.highlightIdentical
    private val inputMethod = appSettingsManager.inputMethod
        .stateIn(
            viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = PreferencesConstants.DEFAULT_INPUT_METHOD
        )

    val positionLines = appSettingsManager.positionLines
    val crossHighlight = themeSettingsManager.boardCrossHighlight
    val funKeyboardOverNum = appSettingsManager.funKeyboardOverNumbers

    val fontSize = appSettingsManager.fontSize

    var multipleSolutionsDialog by mutableStateOf(false)
    var noSolutionsDialog by mutableStateOf(false)

    var gameType by mutableStateOf(GameType.Default9x9)
    var gameDifficulty by mutableStateOf(GameDifficulty.Easy)
    var gameBoard by mutableStateOf(
        List(gameType.size) { row ->
            List(gameType.size) { col ->
                Cell(
                    row,
                    col,
                    0
                )
            }
        }
    )
    var currCell by mutableStateOf(Cell(-1, -1, 0))

    var importStringValue by mutableStateOf("")
    var importTextFieldError by mutableStateOf(false)

    private val sudokuUtils = SudokuUtils()
    private val undoRedoManager = UndoRedoManager(GameState(gameBoard, emptyList()))

    private var overrideInputMethodDF = false
    var digitFirstNumber = -1

    fun getFontSize(type: GameType = gameType, factor: Int): TextUnit =
        sudokuUtils.getFontSize(type, factor)

    fun processInput(cell: Cell): Boolean {
        currCell =
            if (currCell.row == cell.row && currCell.col == cell.col && digitFirstNumber == 0) {
                Cell(-1, -1)
            } else {
                cell
            }

        return if (currCell.row >= 0 && currCell.col >= 0) {
            if ((inputMethod.value == 1 || overrideInputMethodDF) && digitFirstNumber > 0) {
                gameBoard = setValueCell(
                    if (gameBoard[currCell.row][currCell.col].value == digitFirstNumber) 0 else digitFirstNumber
                )
                undoRedoManager.addState(GameState(copyBoard(gameBoard), emptyList()))
            }
            true
        } else {
            false
        }
    }

    fun processInputKeyboard(number: Int, longTap: Boolean = false) {
        if (!longTap && inputMethod.value == 0) {
            overrideInputMethodDF = false
            digitFirstNumber = 0
            if (currCell.row >= 0 && currCell.col >= 0) {
                gameBoard = setValueCell(if (gameBoard[currCell.row][currCell.col].value == number) 0 else number)
            }
            undoRedoManager.addState(GameState(copyBoard(gameBoard), emptyList()))
        } else {
            handleDigitFirstBranches(
                longTap,
                inputMethod.value,
                digitFirstNumber,
                number,
                DigitFirstCallbacks(
                    setOverrideInputMethodDF = { overrideInputMethodDF = true },
                    setDigitFirstNumber = { digitFirstNumber = it },
                    setCurrCell = { currCell = it }
                )
            )
        }
    }

    private fun setValueCell(
        value: Int,
        row: Int = currCell.row,
        col: Int = currCell.col
    ): List<List<Cell>> {
        val new = copyBoard(gameBoard)
        new[row][col].value = value

        val (updatedCurrCell, shouldReturnEarly) = applyClearedCellBookkeeping(currCell, new[row][col], value)
        currCell = updatedCurrCell
        if (shouldReturnEarly) {
            return new
        }

        new[row][col].error = !sudokuUtils.isValidCellDynamic(new, new[row][col], gameType)
        new.forEach { cells ->
            cells.forEach { cell ->
                if (cell.value != 0 && cell.error) {
                    cell.error = !sudokuUtils.isValidCellDynamic(new, cell, gameType)
                }
            }
        }

        return new
    }

    fun toolbarClick(item: ToolBarItem) {
        when (item) {
            ToolBarItem.Undo -> {
                if (undoRedoManager.canUndo()) {
                    gameBoard = undoRedoManager.undo().board
                    gameBoard = boardWithMistakesChecked(gameBoard, gameType, sudokuUtils)
                }
            }

            ToolBarItem.Redo -> {
                if (undoRedoManager.canRedo()) {
                    undoRedoManager.redo()?.let {
                        gameBoard = it.board
                    }
                    gameBoard = boardWithMistakesChecked(gameBoard, gameType, sudokuUtils)
                }
            }

            ToolBarItem.Remove -> {
                if (currCell.row >= 0 && currCell.col >= 0 && !currCell.locked) {
                    val prevValue = gameBoard[currCell.row][currCell.col].value
                    gameBoard = setValueCell(0)
                    if (prevValue != 0) {
                        undoRedoManager.addState(GameState(copyBoard(gameBoard), emptyList()))
                    }
                    gameBoard = boardWithMistakesChecked(gameBoard, gameType, sudokuUtils)
                }
            }

            else -> {}
        }
    }

    fun changeGameType(gameType: GameType) {
        if (this.gameType != gameType) {
            this.gameType = gameType
            currCell = Cell(-1, -1, 0)
            digitFirstNumber = -1
            gameBoard =
                List(gameType.size) { row -> List(gameType.size) { col -> Cell(row, col, 0) } }
        }
    }

    fun setFromString(puzzle: String): Boolean {
        val gameTypeForPuzzle = gameTypeForPuzzleLength(puzzle.length)
        if (gameTypeForPuzzle == null || !puzzle.all { isValidPuzzleChar(it) }) {
            return false
        }
        val sudokuParser = SudokuParser()
        gameType = gameTypeForPuzzle
        gameBoard = sudokuParser.parseBoard(
            board = puzzle,
            gameType = gameType
        )
        return true
    }

    fun saveGame(): Boolean {
        val result = solvePuzzle(gameBoard, gameType)
        when (result.second) {
            0 -> noSolutionsDialog = true
            1 -> saveToDb(result.first)
            else -> multipleSolutionsDialog = true
        }
        return result.second == 1
    }

    private fun saveToDb(board: IntArray) {
        viewModelScope.launch(Dispatchers.IO) {
            var solvedBoard: String
            var initialBoard: String
            withContext(Dispatchers.Default) {
                val sudokuParser = SudokuParser()
                initialBoard = sudokuParser.boardToString(gameBoard)
                val solvedBoardList =
                    List(gameType.size) { row -> List(gameType.size) { col -> Cell(row, col, 0) } }
                for (i in 0 until gameType.size) {
                    for (j in 0 until gameType.size) {
                        solvedBoardList[i][j].value = board[j + gameType.size * i]
                    }
                }
                solvedBoard = sudokuParser.boardToString(solvedBoardList)
            }
            if (gameUid != -1L) {
                val oldBoard = getBoardUseCase(gameUid)
                updateBoardUseCase(
                    oldBoard.copy(
                        initialBoard = initialBoard,
                        solvedBoard = solvedBoard,
                        difficulty = gameDifficulty,
                        type = gameType
                    )
                )
            } else {
                insertBoardUseCase(
                    SudokuBoard(
                        uid = 0,
                        initialBoard = initialBoard,
                        solvedBoard = solvedBoard,
                        difficulty = gameDifficulty,
                        type = gameType,
                        folderId = if (folderUid != -1L) folderUid else null
                    )
                )
            }
        }
    }

    fun changeGameDifficulty(gameDifficulty: GameDifficulty) {
        this.gameDifficulty = gameDifficulty
    }
}

private fun copyBoard(board: List<List<Cell>>): List<List<Cell>> =
    board.map { items -> items.map { item -> item.copy() } }

private fun solvePuzzle(gameBoard: List<List<Cell>>, gameType: GameType): Pair<IntArray, Int> {
    val qqWingController = QQWingController()
    val solution = qqWingController.solve(
        gameBoard.flatten().map { cell -> cell.value }.toIntArray(),
        gameType
    )
    return Pair(solution, qqWingController.solutionCount)
}

private fun boardWithMistakesChecked(
    gameBoard: List<List<Cell>>,
    gameType: GameType,
    sudokuUtils: SudokuUtils
): List<List<Cell>> {
    val new = copyBoard(gameBoard)
    for (i in new.indices) {
        for (j in new.indices) {
            if (new[i][j].value != 0) {
                new[i][j].error = !sudokuUtils.isValidCellDynamic(
                    board = new,
                    cell = new[i][j],
                    type = gameType
                )
            }
        }
    }
    return new
}

enum class GameStateFilter(val resName: Int) {
    All(R.string.filter_all),
    Completed(R.string.filter_completed),
    InProgress(R.string.filter_in_progress),
    NotStarted(R.string.filter_not_started)
}
