package com.batodev.sudoku.ui.game

import android.util.Log
import com.batodev.sudoku.core.Cell
import com.batodev.sudoku.core.PreferencesConstants
import com.batodev.sudoku.core.utils.applyClearedCellBookkeeping
import com.batodev.sudoku.data.settings.SettingsHelper

internal fun GameViewModel.setValueCell(
    value: Int,
    row: Int = currCell.row,
    col: Int = currCell.col
): List<List<Cell>> {
    var new = getBoardNoRef()
    new[row][col].value = value
    remainingUsesList = countRemainingUses(new)

    val (updatedCurrCell, shouldReturnEarly) = applyClearedCellBookkeeping(currCell, new[row][col], value)
    currCell = updatedCurrCell
    if (shouldReturnEarly) {
        return new
    }

    new = applyMistakeChecking(new, row, col)
    currCell.error = currCell.value == 0
    handleMistakeLimitIfError(new, row, col)
    handleGameCompletion(new)
    if (autoEraseNotesEnabled.value) {
        notes = autoEraseNotes(new, currCell)
    }
    return new
}

private fun GameViewModel.revalidateErrorCell(board: List<List<Cell>>, cell: Cell) {
    if (cell.value == 0 || !cell.error) return
    cell.error = !sudokuUtils.isValidCellDynamic(board, cell, boardEntity.type)
}

private fun GameViewModel.applyMistakeChecking(board: List<List<Cell>>, row: Int, col: Int): List<List<Cell>> {
    return when (mistakesMethod.value) {
        1 -> {
            // rule violations
            board[row][col].error = !sudokuUtils.isValidCellDynamic(board, board[row][col], boardEntity.type)
            board.forEach { cells -> cells.forEach { cell -> revalidateErrorCell(board, cell) } }
            board
        }
        // check with final solution
        2 -> isValidCell(board, board[row][col])
        else -> board
    }
}

private fun GameViewModel.handleMistakeLimitIfError(board: List<List<Cell>>, row: Int, col: Int) {
    if (!board[row][col].error) return
    mistakesMade++
    if (!mistakesLimit.value) return
    mistakesCount++
    if (mistakesCount >= PreferencesConstants.MISTAKES_LIMIT) {
        pauseTimer()
        giveUp()
        endGame = true
    }
}

private fun GameViewModel.handleGameCompletion(board: List<List<Cell>>) {
    gameCompleted = isCompleted(board)
    Log.d("GameViewModel", "gameCompleted: $gameCompleted")
    if (!gameCompleted) return
    val settingsHelper = SettingsHelper(application)
    if (!settingsHelper.preferences.uncoveredPics.contains(boardEntity.prizeImageName)) {
        boardEntity.prizeImageName?.let { settingsHelper.preferences.uncoveredPics.add(it) }
        settingsHelper.savePreferences()
    }
}

internal fun GameViewModel.processNumberInput(number: Int) {
    val canInput = currCell.row >= 0 && currCell.col >= 0 && gamePlaying && !currCell.locked
    if (!canInput) return
    if (!notesToggled) {
        // Clear all note to set a number
        notes = clearNotesAtCell(notes, currCell.row, currCell.col)

        gameBoard = setValueCell(
            if (gameBoard[currCell.row][currCell.col].value == number) 0 else number
        )
    } else {
        gameBoard = setValueCell(0)
        setNote(number)
        remainingUsesList = countRemainingUses(gameBoard)
    }
}

fun GameViewModel.toggleEraseButton() {
    notesToggled = false
    currCell = Cell(-1, -1, 0)
    digitFirstNumber = -1
    eraseButtonToggled = !eraseButtonToggled
}
