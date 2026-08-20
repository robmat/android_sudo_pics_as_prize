package com.batodev.sudoku.core.qqwing

/**
 * The "naked pairs" solving technique: if two cells in the same row, column
 * or section both have exactly the same two remaining possibilities, those
 * two values can be eliminated as possibilities everywhere else in that
 * row/column/section.
 */
internal class NakedPairTechniques(
    private val board: QQWing,
) {
    fun countPossibilities(position: Int): Int {
        var count = 0
        for (valIndex in 0 until QQWing.ROW_COL_SEC_SIZE) {
            val valPos = getPossibilityIndexInternal(valIndex, position)
            if (board.possibilities[valPos] == 0) count++
        }
        return count
    }

    fun arePossibilitiesSame(
        position1: Int,
        position2: Int,
    ): Boolean {
        for (valIndex in 0 until QQWing.ROW_COL_SEC_SIZE) {
            val isCandidate1 = board.possibilities[getPossibilityIndexInternal(valIndex, position1)] == 0
            val isCandidate2 = board.possibilities[getPossibilityIndexInternal(valIndex, position2)] == 0
            if (isCandidate1 != isCandidate2) return false
        }
        return true
    }

    fun removePossibilitiesInOneFromTwo(
        position1: Int,
        position2: Int,
        round: Int,
    ): Boolean {
        var doneSomething = false
        for (valIndex in 0 until QQWing.ROW_COL_SEC_SIZE) {
            val valPos1 = getPossibilityIndexInternal(valIndex, position1)
            val valPos2 = getPossibilityIndexInternal(valIndex, position2)
            if (board.possibilities[valPos1] == 0 && board.possibilities[valPos2] == 0) {
                board.possibilities[valPos2] = round
                doneSomething = true
            }
        }
        return doneSomething
    }

    fun handleNakedPairs(round: Int): Boolean {
        for (position in 0 until QQWing.BOARD_SIZE) {
            if (countPossibilities(position) != 2) continue
            if (tryHandleNakedPairsAt(round, position)) return true
        }
        return false
    }

    private fun tryHandleNakedPairsAt(
        round: Int,
        position: Int,
    ): Boolean {
        val row = cellToRowInternal(position)
        val column = cellToColumnInternal(position)
        val section = cellToSectionStartCellInternal(position)
        for (position2 in position until QQWing.BOARD_SIZE) {
            if (!isNakedPairCandidate(position, position2)) continue
            val reductions =
                listOf(
                    { row == cellToRowInternal(position2) && tryReduceRow(round, position, position2, row) },
                    { column == cellToColumnInternal(position2) && tryReduceColumn(round, position, position2, column) },
                    { section == cellToSectionStartCellInternal(position2) && tryReduceSection(round, position, position2) },
                )
            if (reductions.any { it() }) return true
        }
        return false
    }

    private fun isNakedPairCandidate(
        position: Int,
        position2: Int,
    ): Boolean = position != position2 && countPossibilities(position2) == 2 && arePossibilitiesSame(position, position2)

    private fun tryReduceRow(
        round: Int,
        position: Int,
        position2: Int,
        row: Int,
    ): Boolean {
        var doneSomething = false
        for (column2 in 0 until QQWing.ROW_COL_SEC_SIZE) {
            val position3 = rowColumnToCellInternal(row, column2)
            if (position3 != position && position3 != position2 &&
                removePossibilitiesInOneFromTwo(position, position3, round)
            ) {
                doneSomething = true
            }
        }
        if (doneSomething) {
            board.historyRecorder.recordMove(round, LogType.NAKED_PAIR_ROW, 0, position)
        }
        return doneSomething
    }

    private fun tryReduceColumn(
        round: Int,
        position: Int,
        position2: Int,
        column: Int,
    ): Boolean {
        var doneSomething = false
        for (row2 in 0 until QQWing.ROW_COL_SEC_SIZE) {
            val position3 = rowColumnToCellInternal(row2, column)
            if (position3 != position && position3 != position2 &&
                removePossibilitiesInOneFromTwo(position, position3, round)
            ) {
                doneSomething = true
            }
        }
        if (doneSomething) {
            board.historyRecorder.recordMove(round, LogType.NAKED_PAIR_COLUMN, 0, position)
        }
        return doneSomething
    }

    private fun tryReduceSection(
        round: Int,
        position: Int,
        position2: Int,
    ): Boolean {
        var doneSomething = false
        val secStart = cellToSectionStartCellInternal(position)
        for (i in 0 until QQWing.GRID_SIZE_COL) {
            for (j in 0 until QQWing.GRID_SIZE_ROW) {
                val position3 = secStart + i + QQWing.ROW_COL_SEC_SIZE * j
                if (position3 != position && position3 != position2 &&
                    removePossibilitiesInOneFromTwo(position, position3, round)
                ) {
                    doneSomething = true
                }
            }
        }
        if (doneSomething) {
            board.historyRecorder.recordMove(round, LogType.NAKED_PAIR_SECTION, 0, position)
        }
        return doneSomething
    }
}
