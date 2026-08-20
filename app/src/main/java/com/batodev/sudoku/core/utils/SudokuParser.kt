package com.batodev.sudoku.core.utils

import com.batodev.sudoku.core.Cell
import com.batodev.sudoku.core.Note
import com.batodev.sudoku.core.qqwing.GameType

class SudokuParser {
    private val emptySeparators = listOf('0', '.')
    private val radix = RADIX

    companion object {
        private const val RADIX = 13
        private const val MAX_STANDARD_DIGIT = 9
        private const val NOTE_VALUE_CHAR_INDEX = 4
    }

    fun parseBoard(
        board: String,
        gameType: GameType,
        locked: Boolean = false,
        emptySeparator: Char? = null,
    ): MutableList<MutableList<Cell>> {
        if (board.isEmpty()) {
            throw BoardParseException(message = "Input string was empty")
        }

        val size = gameType.size
        val listBoard =
            MutableList(size) { row ->
                MutableList(size) { col ->
                    Cell(row, col, 0)
                }
            }

        for (i in board.indices) {
            val value =
                if (emptySeparator != null) {
                    if (board[i] == emptySeparator) 0 else boardDigitToInt(board[i])
                } else {
                    if (board[i] in emptySeparators) 0 else boardDigitToInt(board[i])
                }

            listBoard[i / size][i % size].value = value
            listBoard[i / size][i % size].locked = locked
        }

        return listBoard
    }

    /**
     * Converts sudoku board to string
     * @param boardList Sudoku board
     * @return Sudoku in string
     */
    private fun cellToChar(
        cell: Cell,
        emptySeparator: Char,
    ): String {
        val isStandardDigit = cell.value <= MAX_STANDARD_DIGIT
        return when {
            isStandardDigit && cell.value != 0 -> cell.value.toString()
            isStandardDigit -> emptySeparator.toString()
            else -> cell.value.toString(radix)
        }
    }

    fun boardToString(
        boardList: List<List<Cell>>,
        emptySeparator: Char = '0',
    ): String {
        var boardString = ""
        boardList.forEach { cells ->
            cells.forEach { cell -> boardString += cellToChar(cell, emptySeparator) }
        }
        return boardString
    }

    fun boardToString(
        board: IntArray,
        emptySeparator: Char = '0',
    ): String {
        var boardString = ""
        board.forEach {
            boardString += if (it != 0) it.toString(radix) else emptySeparator
        }
        return boardString
    }

    fun parseNotes(notesString: String): List<Note> {
        val notes = mutableListOf<Note>()
        var i = 0
        while (i < notesString.length) {
            println(i.toString())
            val index = notesString.indexOf(';', i)
            val toParse = notesString.substring(i..index)
            val row = boardDigitToInt(toParse[0])
            val col = boardDigitToInt(toParse[2])
            val value = boardDigitToInt(toParse[NOTE_VALUE_CHAR_INDEX])
            notes.add(Note(row, col, value))
            i += index - i + 1
        }
        return notes
    }

    fun notesToString(notes: List<Note>): String {
        var notesString = ""
        // row,col,number;row,col,number....row,col,number;
        // e.g 0,3,1;0,3,5;7,7,5;
        notes.forEach {
            notesString +=
                "${it.row.toString(radix)},${it.col.toString(radix)},${it.value.toString(radix)};"
        }
        return notesString
    }

    private fun boardDigitToInt(char: Char): Int = char.digitToInt(radix)
}

class BoardParseException(
    message: String,
) : Exception(message)
