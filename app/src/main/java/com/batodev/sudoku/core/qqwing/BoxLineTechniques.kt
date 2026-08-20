package com.batodev.sudoku.core.qqwing

/**
 * The "box/line reduction" solving techniques: if all remaining candidates
 * for a value within a row (or column) fall inside a single box, that value
 * can be eliminated from the rest of the box; and the symmetric case for
 * columns.
 */
internal class BoxLineTechniques(
    private val board: QQWing,
) {
    fun colBoxReduction(round: Int): Boolean {
        for (valIndex in 0 until QQWing.ROW_COL_SEC_SIZE) {
            for (col in 0 until QQWing.ROW_COL_SEC_SIZE) {
                val colBox = findColBox(valIndex, col) ?: continue
                val colStart = columnToFirstCellInternal(col)
                if (eliminateColBoxCandidates(round, valIndex, col, colBox)) {
                    board.historyRecorder.recordMove(round, LogType.COLUMN_BOX, valIndex + 1, colStart)
                    return true
                }
            }
        }
        return false
    }

    fun rowBoxReduction(round: Int): Boolean {
        for (valIndex in 0 until QQWing.ROW_COL_SEC_SIZE) {
            for (row in 0 until QQWing.ROW_COL_SEC_SIZE) {
                val rowBox = findRowBox(valIndex, row) ?: continue
                val rowStart = rowToFirstCellInternal(row)
                if (eliminateRowBoxCandidates(round, valIndex, row, rowBox)) {
                    board.historyRecorder.recordMove(round, LogType.ROW_BOX, valIndex + 1, rowStart)
                    return true
                }
            }
        }
        return false
    }

    private fun findColBox(
        valIndex: Int,
        col: Int,
    ): Int? {
        var inOneBox = true
        var colBox = -1
        for (i in 0 until QQWing.GRID_SIZE_COL) {
            for (j in 0 until QQWing.GRID_SIZE_ROW) {
                val row = i * QQWing.GRID_SIZE_ROW + j
                val position = rowColumnToCellInternal(row, col)
                val valPos = getPossibilityIndexInternal(valIndex, position)
                if (board.possibilities[valPos] != 0) continue
                if (colBox == -1 || colBox == i) colBox = i else inOneBox = false
            }
        }
        return if (inOneBox && colBox != -1) colBox else null
    }

    private fun eliminateColBoxCandidates(
        round: Int,
        valIndex: Int,
        col: Int,
        colBox: Int,
    ): Boolean {
        var doneSomething = false
        val row = QQWing.GRID_SIZE_ROW * colBox
        val secStart = cellToSectionStartCellInternal(rowColumnToCellInternal(row, col))
        val secStartRow = cellToRowInternal(secStart)
        val secStartCol = cellToColumnInternal(secStart)
        for (i in 0 until QQWing.GRID_SIZE_COL) {
            for (j in 0 until QQWing.GRID_SIZE_ROW) {
                val row2 = secStartRow + j
                val col2 = secStartCol + i
                val position = rowColumnToCellInternal(row2, col2)
                val valPos = getPossibilityIndexInternal(valIndex, position)
                if (col != col2 && board.possibilities[valPos] == 0) {
                    board.possibilities[valPos] = round
                    doneSomething = true
                }
            }
        }
        return doneSomething
    }

    private fun findRowBox(
        valIndex: Int,
        row: Int,
    ): Int? {
        var inOneBox = true
        var rowBox = -1
        for (i in 0 until QQWing.GRID_SIZE_ROW) {
            for (j in 0 until QQWing.GRID_SIZE_COL) {
                val column = i * QQWing.GRID_SIZE_COL + j
                val position = rowColumnToCellInternal(row, column)
                val valPos = getPossibilityIndexInternal(valIndex, position)
                if (board.possibilities[valPos] != 0) continue
                if (rowBox == -1 || rowBox == i) rowBox = i else inOneBox = false
            }
        }
        return if (inOneBox && rowBox != -1) rowBox else null
    }

    private fun eliminateRowBoxCandidates(
        round: Int,
        valIndex: Int,
        row: Int,
        rowBox: Int,
    ): Boolean {
        var doneSomething = false
        val column = QQWing.GRID_SIZE_COL * rowBox
        val secStart = cellToSectionStartCellInternal(rowColumnToCellInternal(row, column))
        val secStartRow = cellToRowInternal(secStart)
        val secStartCol = cellToColumnInternal(secStart)
        for (i in 0 until QQWing.GRID_SIZE_ROW) {
            for (j in 0 until QQWing.GRID_SIZE_COL) {
                val row2 = secStartRow + i
                val col2 = secStartCol + j
                val position = rowColumnToCellInternal(row2, col2)
                val valPos = getPossibilityIndexInternal(valIndex, position)
                if (row != row2 && board.possibilities[valPos] == 0) {
                    board.possibilities[valPos] = round
                    doneSomething = true
                }
            }
        }
        return doneSomething
    }
}
