package com.batodev.sudoku.core.qqwing

/**
 * The "pointing pairs/triples" solving techniques: if all remaining
 * candidates for a value within a section fall inside a single row (or
 * column), that value can be eliminated from the rest of the row/column.
 */
internal class PointingTechniques(
    private val board: QQWing,
) {
    fun pointingRowReduction(round: Int): Boolean {
        for (valIndex in 0 until QQWing.ROW_COL_SEC_SIZE) {
            for (section in 0 until QQWing.ROW_COL_SEC_SIZE) {
                val secStart = sectionToFirstCellInternal(section)
                val boxRow = findBoxRow(valIndex, secStart) ?: continue
                val row = cellToRowInternal(secStart) + boxRow
                val rowStart = rowToFirstCellInternal(row)
                if (eliminateRowCandidates(round, valIndex, section, rowStart)) {
                    board.historyRecorder.recordMove(round, LogType.POINTING_PAIR_TRIPLE_ROW, valIndex + 1, rowStart)
                    return true
                }
            }
        }
        return false
    }

    fun pointingColumnReduction(round: Int): Boolean {
        for (valIndex in 0 until QQWing.ROW_COL_SEC_SIZE) {
            for (section in 0 until QQWing.ROW_COL_SEC_SIZE) {
                val secStart = sectionToFirstCellInternal(section)
                val boxCol = findBoxCol(valIndex, secStart) ?: continue
                val col = cellToColumnInternal(secStart) + boxCol
                val colStart = columnToFirstCellInternal(col)
                if (eliminateColumnCandidates(round, valIndex, section, colStart)) {
                    board.historyRecorder.recordMove(round, LogType.POINTING_PAIR_TRIPLE_COLUMN, valIndex + 1, colStart)
                    return true
                }
            }
        }
        return false
    }

    private fun findBoxRow(
        valIndex: Int,
        secStart: Int,
    ): Int? {
        var inOneRow = true
        var boxRow = -1
        for (j in 0 until QQWing.GRID_SIZE_ROW) {
            for (i in 0 until QQWing.GRID_SIZE_COL) {
                val secVal = secStart + i + QQWing.ROW_COL_SEC_SIZE * j
                val valPos = getPossibilityIndexInternal(valIndex, secVal)
                if (board.possibilities[valPos] != 0) continue
                if (boxRow == -1 || boxRow == j) boxRow = j else inOneRow = false
            }
        }
        return if (inOneRow && boxRow != -1) boxRow else null
    }

    private fun eliminateRowCandidates(
        round: Int,
        valIndex: Int,
        section: Int,
        rowStart: Int,
    ): Boolean {
        var doneSomething = false
        for (i in 0 until QQWing.ROW_COL_SEC_SIZE) {
            val position = rowStart + i
            val section2 = cellToSectionInternal(position)
            val valPos = getPossibilityIndexInternal(valIndex, position)
            if (section != section2 && board.possibilities[valPos] == 0) {
                board.possibilities[valPos] = round
                doneSomething = true
            }
        }
        return doneSomething
    }

    private fun findBoxCol(
        valIndex: Int,
        secStart: Int,
    ): Int? {
        var inOneCol = true
        var boxCol = -1
        for (i in 0 until QQWing.GRID_SIZE_COL) {
            for (j in 0 until QQWing.GRID_SIZE_ROW) {
                val secVal = secStart + i + QQWing.ROW_COL_SEC_SIZE * j
                val valPos = getPossibilityIndexInternal(valIndex, secVal)
                if (board.possibilities[valPos] != 0) continue
                if (boxCol == -1 || boxCol == i) boxCol = i else inOneCol = false
            }
        }
        return if (inOneCol && boxCol != -1) boxCol else null
    }

    private fun eliminateColumnCandidates(
        round: Int,
        valIndex: Int,
        section: Int,
        colStart: Int,
    ): Boolean {
        var doneSomething = false
        for (i in 0 until QQWing.ROW_COL_SEC_SIZE) {
            val position = colStart + QQWing.ROW_COL_SEC_SIZE * i
            val section2 = cellToSectionInternal(position)
            val valPos = getPossibilityIndexInternal(valIndex, position)
            if (section != section2 && board.possibilities[valPos] == 0) {
                board.possibilities[valPos] = round
                doneSomething = true
            }
        }
        return doneSomething
    }
}
