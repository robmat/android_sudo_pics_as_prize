package com.batodev.sudoku.ui.game

import androidx.compose.ui.unit.TextUnit
import androidx.lifecycle.viewModelScope
import com.batodev.sudoku.core.qqwing.GameType
import com.batodev.sudoku.core.utils.SudokuParser
import com.batodev.sudoku.core.utils.getFontSize
import com.batodev.sudoku.core.utils.toFormattedString
import com.batodev.sudoku.data.database.model.SavedGame
import com.batodev.sudoku.data.datastore.setFirstGame
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.ZonedDateTime
import kotlin.time.toKotlinDuration

internal suspend fun GameViewModel.saveGame() {
    val savedGame = dependencies.savedGameRepository.get(boardEntity.uid)
    val sudokuParser = SudokuParser()
    if (savedGame != null) {
        dependencies.savedGameRepository.update(
            savedGame.copy(
                timer = java.time.Duration.ofSeconds(duration.inWholeSeconds),
                currentBoard = sudokuParser.boardToString(gameBoard),
                notes = sudokuParser.notesToString(notes),
                mistakes = mistakesCount,
                lastPlayed = ZonedDateTime.now()
            )
        )
    } else {
        dependencies.savedGameRepository.insert(
            SavedGame(
                uid = boardEntity.uid,
                currentBoard = sudokuParser.boardToString(gameBoard),
                notes = sudokuParser.notesToString(notes),
                timer = java.time.Duration.ofSeconds(duration.inWholeSeconds),
                mistakes = mistakesCount,
                lastPlayed = ZonedDateTime.now(),
                startedAt = ZonedDateTime.now()
            )
        )
    }
}

internal fun GameViewModel.restoreSavedGame(savedGame: SavedGame?) {
    if (savedGame == null) return
    // restore timer and text
    duration = savedGame.timer.toKotlinDuration()
    timeText = duration.toFormattedString()

    mistakesCount = savedGame.mistakes
    val sudokuParser = SudokuParser()
    gameBoard = sudokuParser.parseBoard(
        savedGame.currentBoard,
        boardEntity.type
    )
    notes = sudokuParser.parseNotes(savedGame.notes)

    for (i in gameBoard.indices) {
        for (j in gameBoard.indices) {
            restoreCellState(i, j)
        }
    }
}

private fun GameViewModel.restoreCellState(i: Int, j: Int) {
    gameBoard[i][j].locked = initialBoard[i][j].locked
    if (gameBoard[i][j].value == 0 || gameBoard[i][j].locked) return
    gameBoard[i][j].error = if (mistakesMethod.value == 1) {
        !sudokuUtils.isValidCellDynamic(
            board = gameBoard,
            cell = gameBoard[i][j],
            type = boardEntity.type
        )
    } else {
        isValidCell(gameBoard, gameBoard[i][j])[i][j].error
    }
}

fun GameViewModel.getFontSize(type: GameType = gameType, factor: Int): TextUnit {
    return sudokuUtils.getFontSize(type, factor)
}

fun GameViewModel.setFirstGameFalse() {
    viewModelScope.launch(Dispatchers.IO) {
        appSettingsManager.setFirstGame(false)
    }
}

fun GameViewModel.prizeImageName(): String {
    return boardEntity.prizeImageName.orEmpty()
}
