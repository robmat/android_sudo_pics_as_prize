package com.batodev.sudoku.core.parser

import android.util.Log
import com.batodev.sudoku.data.database.model.SudokuBoard

/**
 * .sdm - is a very simple format.
 * Each line is a single puzzle.
 * Empty cells can be represented by a zero or a dot
 * Example: 000605000003020800045090270500000001062000540400000007098060450006040700000203000
 */
class SdmParser : FileImportParser {
    private val tag = "SDMParser"

    companion object {
        private const val STANDARD_BOARD_LENGTH = 81
    }

    private fun isValidLine(line: String): Boolean = line.length == STANDARD_BOARD_LENGTH && line.all { char -> char.isDigit() }

    private fun processLine(
        line: String,
        toImport: MutableList<String>,
    ) {
        val trimmed = line.trim()
        if (isValidLine(trimmed)) {
            toImport.add(trimmed.replace(".", "0"))
        } else {
            Log.i(tag, "This line was skipped: $trimmed")
        }
    }

    /**
     * @param content .sdm file content
     * @return Pair with: First - parsing success. Second - strings of parsed boards
     */
    override fun toBoards(content: String): Pair<Boolean, List<String>> {
        val toImport = mutableListOf<String>()
        var success = content.isNotEmpty()

        if (success) {
            try {
                content.lines().forEach { processLine(it, toImport) }
            } catch (expectedException: Exception) {
                Log.e(tag, "Exception while parsing!", expectedException)
                success = false
            }
        }

        return Pair(success, toImport)
    }

    fun boardsToSdm(boards: List<SudokuBoard>): String {
        val stringBuilder = StringBuilder()
        boards.forEach { game ->
            stringBuilder.append(game.initialBoard + "\n")
        }
        // Remove the extra \n that was added in the loop above
        stringBuilder.delete(stringBuilder.length - 1, stringBuilder.length)
        return stringBuilder.toString()
    }
}
