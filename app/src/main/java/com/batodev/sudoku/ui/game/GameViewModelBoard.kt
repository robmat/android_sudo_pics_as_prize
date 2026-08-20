package com.batodev.sudoku.ui.game

import androidx.lifecycle.viewModelScope
import com.batodev.sudoku.core.Cell
import com.batodev.sudoku.core.qqwing.QQWingController
import com.batodev.sudoku.core.utils.SudokuParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.ZonedDateTime

internal fun GameViewModel.getBoardNoRef(): List<List<Cell>> =
    gameBoard.map { items -> items.map { item -> item.copy() } }

internal fun GameViewModel.countRemainingUses(board: List<List<Cell>>): MutableList<Int> {
    val uses = mutableListOf<Int>()
    for (i in 0..size) {
        uses.add(size - sudokuUtils.countNumberInBoard(board, i + 1))
    }
    return uses
}

internal fun GameViewModel.isValidCell(
    board: List<List<Cell>> = getBoardNoRef(),
    cell: Cell
): List<List<Cell>> {
    if (solvedBoard.isNotEmpty()) {
        board[cell.row][cell.col].error =
            solvedBoard[cell.row][cell.col].value != board[cell.row][cell.col].value
    } else {
        solveBoard()
    }
    return board
}

internal fun GameViewModel.isCompleted(board: List<List<Cell>> = getBoardNoRef()): Boolean {
    if (solvedBoard.isEmpty()) solveBoard()
    for (i in solvedBoard.indices) {
        for (j in solvedBoard.indices) {
            if (solvedBoard[i][j].value != board[i][j].value) {
                return false
            }
        }
    }
    viewModelScope.launch(Dispatchers.IO) {
        val savedGame = dependencies.savedGameRepository.get(boardEntity.uid)
        if (savedGame != null) {
            dependencies.savedGameRepository.update(
                savedGame.copy(
                    completed = true,
                    giveUp = false,
                    canContinue = false,
                    finishedAt = ZonedDateTime.now()
                )
            )
        }
    }
    return true
}

fun GameViewModel.checkMistakesAll() {
    var new = getBoardNoRef()
    if (!isInitialBoardInitialized) return
    for (i in new.indices) {
        for (j in new.indices) {
            if (new[i][j].value == 0 || new[i][j].locked) continue
            new = applyMistakeCheck(new, i, j)
        }
    }
    gameBoard = new
}

private fun GameViewModel.applyMistakeCheck(board: List<List<Cell>>, i: Int, j: Int): List<List<Cell>> {
    when (mistakesMethod.value) {
        0 -> board[i][j].error = false
        1 -> board[i][j].error = !sudokuUtils.isValidCellDynamic(board, board[i][j], boardEntity.type)
        2 -> return isValidCell(board, board[i][j])
    }
    return board
}

// to make sure that solvedBoard really contains a solved board
internal fun GameViewModel.solveBoard() {
    val qqWing = QQWingController()
    val boardToSolve = boardEntity.initialBoard.map { it.digitToInt(GameViewModel.RADIX) }.toIntArray()
    val solved = qqWing.solve(boardToSolve, boardEntity.type)

    val newSolvedBoard = List(boardEntity.type.size) { row ->
        List(boardEntity.type.size) { col ->
            Cell(
                row,
                col,
                0
            )
        }
    }
    for (i in 0 until size) {
        for (j in 0 until size) {
            newSolvedBoard[i][j].value = solved[i * size + j]
        }
    }

    viewModelScope.launch(Dispatchers.IO) {
        val sudokuParser = SudokuParser()
        dependencies.updateBoardUseCase(
            boardEntity.copy(solvedBoard = sudokuParser.boardToString(newSolvedBoard))
        )
    }
    solvedBoard = newSolvedBoard

    for (i in solvedBoard.indices) {
        for (j in solvedBoard.indices) {
            solvedBoard[i][j].locked = initialBoard[i][j].locked
        }
    }
}
