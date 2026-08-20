package com.batodev.sudoku.core.utils

import com.batodev.sudoku.core.Cell
import com.batodev.sudoku.core.Note
import com.batodev.sudoku.core.qqwing.GameType

class SudokuUtils {
    // returns range of row indexes in region of give cell
    fun getBoxRowRange(
        cell: Cell,
        sectionHeight: Int,
    ): IntRange = cell.row - cell.row % sectionHeight until (cell.row - cell.row % sectionHeight) + sectionHeight

    // returns range of col indexes in region of give cell
    fun getBoxColRange(
        cell: Cell,
        sectionWidth: Int,
    ): IntRange = cell.col - cell.col % sectionWidth until (cell.col - cell.col % sectionWidth) + sectionWidth

    // returns candidates for given cell
    fun getCandidates(
        board: List<List<Cell>>,
        cell: Cell,
        type: GameType,
    ): List<Int> {
        var candidates = List(type.size) { index -> index + 1 }

        for (i in getBoxRowRange(cell, type.sectionHeight)) {
            for (j in getBoxColRange(cell, type.sectionWidth)) {
                if (board[i][j].value != 0 && (i != cell.row || j != cell.col)) {
                    candidates = candidates.minus(board[i][j].value)
                }
            }
        }

        for (i in 0 until type.size) {
            if (board[i][cell.col].value != 0 && i != cell.row) {
                candidates = candidates.minus(board[i][cell.col].value)
            }
            if (board[cell.row][i].value != 0 && i != cell.col) {
                candidates = candidates.minus(board[cell.row][i].value)
            }
        }

        return candidates
    }

    private fun hasDuplicateInBox(
        board: List<List<Cell>>,
        cell: Cell,
        type: GameType,
    ): Boolean {
        for (i in getBoxRowRange(cell, type.sectionHeight)) {
            for (j in getBoxColRange(cell, type.sectionWidth)) {
                val isDuplicateInBox =
                    board[i][j].value != 0 && board[i][j].value == cell.value &&
                        (i != cell.row || j != cell.col)
                if (isDuplicateInBox) {
                    return true
                }
            }
        }
        return false
    }

    private fun hasDuplicateInRowOrCol(
        board: List<List<Cell>>,
        cell: Cell,
        type: GameType,
    ): Boolean {
        for (i in 0 until type.size) {
            val isDuplicateInRowOrCol =
                (board[i][cell.col].value == cell.value && i != cell.row) ||
                    (board[cell.row][i].value == cell.value && i != cell.col)
            if (isDuplicateInRowOrCol) {
                return true
            }
        }
        return false
    }

    // returns if given cell not violating sudoku rules
    fun isValidCellDynamic(
        board: List<List<Cell>>,
        cell: Cell,
        type: GameType,
    ): Boolean = !hasDuplicateInBox(board, cell, type) && !hasDuplicateInRowOrCol(board, cell, type)

    // returns count of given number on board
    fun countNumberInBoard(
        board: List<List<Cell>>,
        number: Int,
    ): Int {
        var count = 0
        board.forEach { cells ->
            cells.forEach {
                if (it.value == number) {
                    count++
                }
            }
        }
        return count
    }

    // compute all candidates for empty cells and returns them as notes
    fun computeNotes(
        board: List<List<Cell>>,
        type: GameType,
    ): List<Note> =
        board
            .flatten()
            .filter { it.value == 0 }
            .flatMap { cell -> getCandidates(board, cell, type).map { Note(cell.row, cell.col, it) } }

    fun autoEraseNotes(
        board: List<List<Cell>>,
        notes: List<Note>,
        cell: Cell,
        type: GameType,
    ): List<Note> {
        var newNotes = notes

        for (i in getBoxRowRange(cell, type.sectionHeight)) {
            for (j in getBoxColRange(cell, type.sectionWidth)) {
                if (board[i][j].value == 0 && newNotes.contains(Note(i, j, cell.value))) {
                    newNotes = newNotes.minus(Note(i, j, cell.value))
                }
            }
        }
        for (i in 0 until type.size) {
            if (board[i][cell.col].value == 0 && newNotes.contains(Note(i, cell.col, cell.value))) {
                newNotes = newNotes.minus(Note(i, cell.col, cell.value))
            }
            if (board[cell.row][i].value == 0 && newNotes.contains(Note(cell.row, i, cell.value))) {
                newNotes = newNotes.minus(Note(cell.row, i, cell.value))
            }
        }
        return newNotes
    }
}
