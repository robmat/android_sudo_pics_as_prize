package com.batodev.sudoku.ui.game

import androidx.lifecycle.viewModelScope
import com.batodev.sudoku.core.Cell
import com.batodev.sudoku.core.utils.GameState
import com.batodev.sudoku.core.utils.SudokuParser
import com.batodev.sudoku.core.utils.toFormattedString
import com.batodev.sudoku.data.database.model.Record
import com.batodev.sudoku.ui.game.components.ToolBarItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.ZonedDateTime
import kotlin.concurrent.fixedRateTimer
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.toDuration
import kotlin.time.toJavaDuration

private const val HINT_TIME_PENALTY_SECONDS = 30

fun GameViewModel.startTimer() {
    if (gamePlaying) return
    gamePlaying = true
    val updateRate = GameViewModel.TIMER_UPDATE_RATE_MS

    timer = fixedRateTimer(initialDelay = updateRate, period = updateRate) {
        val prevTime = duration

        duration = duration.plus((updateRate * GameViewModel.NANOS_PER_MILLI).toDuration(DurationUnit.NANOSECONDS))
        // update text every second
        if (prevTime.toInt(DurationUnit.SECONDS) != duration.toInt(DurationUnit.SECONDS)) {
            timeText = duration.toFormattedString()
            // save game
            if (gameBoard.any { it.any { cell -> cell.value != 0 } }) {
                viewModelScope.launch(Dispatchers.IO) {
                    saveGame()
                }
            }
        }
    }
}

fun GameViewModel.pauseTimer() {
    gamePlaying = false
    timer.cancel()
}

fun GameViewModel.toolbarClick(item: ToolBarItem) {
    if (!gamePlaying) return
    when (item) {
        ToolBarItem.Undo -> handleUndo()
        ToolBarItem.Redo -> handleRedo()
        ToolBarItem.Hint -> useHint()
        ToolBarItem.Note -> {
            notesToggled = !notesToggled
            eraseButtonToggled = false
        }

        ToolBarItem.Remove -> handleRemove()
    }
}

private fun GameViewModel.handleUndo() {
    if (undoRedoManager.canUndo()) {
        undoRedoManager.undo().also {
            gameBoard = it.board
            notes = it.notes
        }
        checkMistakesAll()
    }
    remainingUsesList = countRemainingUses(gameBoard)
}

private fun GameViewModel.handleRedo() {
    if (undoRedoManager.canRedo()) {
        undoRedoManager.redo()?.let {
            gameBoard = it.board
            notes = it.notes
        }
        checkMistakesAll()
    }
    remainingUsesList = countRemainingUses(gameBoard)
}

private fun GameViewModel.handleRemove() {
    if (inputMethod.value == 1 || eraseButtonToggled) {
        toggleEraseButton()
        return
    }
    if (currCell.row >= 0 && currCell.col >= 0 && !currCell.locked) {
        val prevValue = gameBoard[currCell.row][currCell.col].value
        val notesInCell =
            notes.count { note -> note.row == currCell.row && note.col == currCell.col }
        notes = clearNotesAtCell(notes)
        gameBoard = setValueCell(0)
        if (prevValue != 0 || notesInCell != 0) {
            undoRedoManager.addState(GameState(gameBoard, notes))
        }
    }
}

private fun GameViewModel.useHint() {
    if (solvedBoard.isEmpty()) solveBoard()
    if (currCell.row >= 0 && currCell.col >= 0 && !currCell.locked) {
        notes = clearNotesAtCell(notes, currCell.row, currCell.col)
        gameBoard = setValueCell(solvedBoard[currCell.row][currCell.col].value)

        val new = getBoardNoRef()
        new[currCell.row][currCell.col].error = false
        gameBoard = new

        duration = duration.plus(HINT_TIME_PENALTY_SECONDS.toDuration(DurationUnit.SECONDS))
        timeText = duration.toFormattedString()
        undoRedoManager.addState(GameState(gameBoard, notes))
        hintsUsed++
    }
}

fun GameViewModel.resetGame(resetTimer: Boolean) {
    // stop and reset game
    notes = emptyNotes()
    currCell = Cell(-1, -1, 0)
    if (resetTimer) {
        duration = Duration.ZERO
        timeText = duration.toFormattedString()
    }
    digitFirstNumber = 0
    notesToggled = false
    undoRedoManager.clear()

    // init a new game with initial board
    gameBoard = initialBoard.map { items -> items.map { item -> item.copy() } }

    remainingUsesList = countRemainingUses(gameBoard)

    hintsUsed = 0
    mistakesMade = 0
    notesTaken = 0
}

fun GameViewModel.giveUp() {
    giveUp = true
    endGame = true
    currCell = Cell(-1, -1, 0)
    viewModelScope.launch(Dispatchers.IO) {
        val savedGame = dependencies.savedGameRepository.get(boardEntity.uid)
        if (savedGame != null) {
            val sudokuParser = SudokuParser()
            dependencies.savedGameRepository.update(
                savedGame.copy(
                    timer = java.time.Duration.ofSeconds(duration.inWholeSeconds),
                    currentBoard = sudokuParser.boardToString(gameBoard),
                    completed = true,
                    giveUp = true,
                    mistakes = mistakesCount,
                    canContinue = false,
                    finishedAt = ZonedDateTime.now()
                )
            )
        }
    }
}

fun GameViewModel.onGameComplete() {
    viewModelScope.launch(Dispatchers.IO) {
        saveGame()
        dependencies.recordRepository.insert(
            Record(
                boardUid = boardEntity.uid,
                type = boardEntity.type,
                difficulty = boardEntity.difficulty,
                date = ZonedDateTime.now(),
                time = duration.toJavaDuration()
            )
        )
    }
    pauseTimer()
    currCell = Cell(-1, -1, 0)
}
