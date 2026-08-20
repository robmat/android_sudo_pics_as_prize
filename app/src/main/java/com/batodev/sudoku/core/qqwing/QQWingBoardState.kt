package com.batodev.sudoku.core.qqwing

import java.util.Arrays

/**
 * Core board state mutation: setting/resetting the puzzle, marking a value
 * into a cell, and checking whether the current state is solved/impossible.
 */

/**
 * Get the number of cells that are set in the puzzle (as opposed to figured
 * out in the solution).
 */
internal val QQWing.givenCount: Int
    get() {
        var count = 0
        for (i in 0 until QQWing.BOARD_SIZE) {
            if (puzzle[i] != 0) count++
        }
        return count
    }

/**
 * Set the board to the given puzzle. The given puzzle must be an array of
 * BOARD_SIZE integers.
 */
internal fun QQWing.setPuzzle(initPuzzle: IntArray?): Boolean {
    for (i in 0 until QQWing.BOARD_SIZE) {
        puzzle[i] = initPuzzle?.get(i) ?: 0
    }
    return reset()
}

/**
 * Reset the board to its initial state with only the givens. This method
 * clears any solution, resets statistics, and clears any history messages.
 */
internal fun QQWing.reset(): Boolean {
    Arrays.fill(solution, 0)
    Arrays.fill(solutionRound, 0)
    Arrays.fill(possibilities, 0)
    historyRecorder.clear()
    val round = 1
    for (position in 0 until QQWing.BOARD_SIZE) {
        if (puzzle[position] > 0) {
            if (!markGiven(position, round)) return false
        }
    }
    return true
}

private fun QQWing.markGiven(
    position: Int,
    round: Int,
): Boolean {
    val valIndex = puzzle[position] - 1
    val valPos = getPossibilityIndexInternal(valIndex, position)
    val value = puzzle[position]
    if (possibilities[valPos] != 0) return false
    mark(position, round, value)
    historyRecorder.recordMove(round, LogType.GIVEN, value, position)
    return true
}

/**
 * Mark the given value at the given position. Go through the row, column,
 * and section for the position and remove the value from the possibilities.
 */
internal fun QQWing.mark(
    position: Int,
    round: Int,
    value: Int,
) {
    require(solution[position] == 0) { "Marking position that already has been marked." }
    require(solutionRound[position] == 0) { "Marking position that was marked another round." }
    var valIndex = value - 1
    solution[position] = value
    val possInd = getPossibilityIndexInternal(valIndex, position)
    require(possibilities[possInd] == 0) { "Marking impossible position." }
    solutionRound[position] = round

    // Take this value out of the possibilities for everything in the row
    val rowStart = cellToRowInternal(position) * QQWing.ROW_COL_SEC_SIZE
    for (col in 0 until QQWing.ROW_COL_SEC_SIZE) {
        markPossibilityUsed(valIndex, rowStart + col, round)
    }

    // Take this value out of the possibilities for everything in the column
    val colStart = cellToColumnInternal(position)
    for (i in 0 until QQWing.ROW_COL_SEC_SIZE) {
        markPossibilityUsed(valIndex, colStart + QQWing.ROW_COL_SEC_SIZE * i, round)
    }

    // Take this value out of the possibilities for everything in the section
    val secStart = cellToSectionStartCellInternal(position)
    for (i in 0 until QQWing.GRID_SIZE_COL) {
        for (j in 0 until QQWing.GRID_SIZE_ROW) {
            markPossibilityUsed(valIndex, secStart + i + QQWing.ROW_COL_SEC_SIZE * j, round)
        }
    }

    // This position itself is determined, it should have possibilities.
    valIndex = 0
    while (valIndex < QQWing.ROW_COL_SEC_SIZE) {
        markPossibilityUsed(valIndex, position, round)
        valIndex++
    }
}

private fun QQWing.markPossibilityUsed(
    valIndex: Int,
    position: Int,
    round: Int,
) {
    val valPos = getPossibilityIndexInternal(valIndex, position)
    if (possibilities[valPos] == 0) {
        possibilities[valPos] = round
    }
}

internal fun QQWing.isSolved(): Boolean {
    for (i in 0 until QQWing.BOARD_SIZE) {
        if (solution[i] == 0) {
            return false
        }
    }
    return true
}

internal fun QQWing.isImpossible(): Boolean {
    for (position in 0 until QQWing.BOARD_SIZE) {
        if (solution[position] != 0) continue
        if (countRemainingPossibilities(position) == 0) return true
    }
    return false
}

private fun QQWing.countRemainingPossibilities(position: Int): Int {
    var count = 0
    for (valIndex in 0 until QQWing.ROW_COL_SEC_SIZE) {
        val valPos = getPossibilityIndexInternal(valIndex, position)
        if (possibilities[valPos] == 0) count++
    }
    return count
}
