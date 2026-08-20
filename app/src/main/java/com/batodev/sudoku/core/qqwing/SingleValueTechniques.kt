package com.batodev.sudoku.core.qqwing

/**
 * Solving techniques that look for a cell/row/column/section that has only
 * a single remaining possibility ("singles" and "hidden singles").
 */
internal class SingleValueTechniques(
    private val board: QQWing,
) {
    /**
     * Mark exactly one cell that has a single possibility, if such a cell
     * exists. This method will look for a cell that has only one possibility.
     * This type of cell is often called a "single".
     */
    fun onlyPossibilityForCell(round: Int): Boolean {
        for (position in 0 until QQWing.BOARD_SIZE) {
            if (board.solution[position] != 0) continue
            val (count, lastValue) = countCellPossibilities(position)
            if (count == 1) {
                markSingleValue(round, LogType.SINGLE, lastValue - 1, position)
                return true
            }
        }
        return false
    }

    /**
     * Mark exactly one cell which is the only possible value for some row, if
     * such a cell exists. This method will look in a row for a possibility
     * that is only listed for one cell. This type of cell is often called a
     * "hidden single".
     */
    fun onlyValueInRow(round: Int): Boolean {
        for (row in 0 until QQWing.ROW_COL_SEC_SIZE) {
            for (valIndex in 0 until QQWing.ROW_COL_SEC_SIZE) {
                val (count, lastPosition) = countRowValueOccurrences(row, valIndex)
                if (count == 1) {
                    markSingleValue(round, LogType.HIDDEN_SINGLE_ROW, valIndex, lastPosition)
                    return true
                }
            }
        }
        return false
    }

    /**
     * Mark exactly one cell which is the only possible value for some column,
     * if such a cell exists. This method will look in a column for a
     * possibility that is only listed for one cell. This type of cell is
     * often called a "hidden single".
     */
    fun onlyValueInColumn(round: Int): Boolean {
        for (col in 0 until QQWing.ROW_COL_SEC_SIZE) {
            for (valIndex in 0 until QQWing.ROW_COL_SEC_SIZE) {
                val (count, lastPosition) = countColumnValueOccurrences(col, valIndex)
                if (count == 1) {
                    markSingleValue(round, LogType.HIDDEN_SINGLE_COLUMN, valIndex, lastPosition)
                    return true
                }
            }
        }
        return false
    }

    /**
     * Mark exactly one cell which is the only possible value for some section,
     * if such a cell exists. This method will look in a section for a
     * possibility that is only listed for one cell. This type of cell is
     * often called a "hidden single".
     */
    fun onlyValueInSection(round: Int): Boolean {
        for (sec in 0 until QQWing.ROW_COL_SEC_SIZE) {
            val secPos = sectionToFirstCellInternal(sec)
            for (valIndex in 0 until QQWing.ROW_COL_SEC_SIZE) {
                val (count, lastPosition) = countSectionValueOccurrences(secPos, valIndex)
                if (count == 1) {
                    markSingleValue(round, LogType.HIDDEN_SINGLE_SECTION, valIndex, lastPosition)
                    return true
                }
            }
        }
        return false
    }

    private fun countCellPossibilities(position: Int): Pair<Int, Int> {
        var count = 0
        var lastValue = 0
        for (valIndex in 0 until QQWing.ROW_COL_SEC_SIZE) {
            val valPos = getPossibilityIndexInternal(valIndex, position)
            if (board.possibilities[valPos] == 0) {
                count++
                lastValue = valIndex + 1
            }
        }
        return count to lastValue
    }

    private fun countRowValueOccurrences(
        row: Int,
        valIndex: Int,
    ): Pair<Int, Int> {
        var count = 0
        var lastPosition = 0
        for (col in 0 until QQWing.ROW_COL_SEC_SIZE) {
            val position = row * QQWing.ROW_COL_SEC_SIZE + col
            val valPos = getPossibilityIndexInternal(valIndex, position)
            if (board.possibilities[valPos] == 0) {
                count++
                lastPosition = position
            }
        }
        return count to lastPosition
    }

    private fun countColumnValueOccurrences(
        col: Int,
        valIndex: Int,
    ): Pair<Int, Int> {
        var count = 0
        var lastPosition = 0
        for (row in 0 until QQWing.ROW_COL_SEC_SIZE) {
            val position = rowColumnToCellInternal(row, col)
            val valPos = getPossibilityIndexInternal(valIndex, position)
            if (board.possibilities[valPos] == 0) {
                count++
                lastPosition = position
            }
        }
        return count to lastPosition
    }

    private fun countSectionValueOccurrences(
        secPos: Int,
        valIndex: Int,
    ): Pair<Int, Int> {
        var count = 0
        var lastPosition = 0
        for (i in 0 until QQWing.GRID_SIZE_COL) {
            for (j in 0 until QQWing.GRID_SIZE_ROW) {
                val position = secPos + i + QQWing.ROW_COL_SEC_SIZE * j
                val valPos = getPossibilityIndexInternal(valIndex, position)
                if (board.possibilities[valPos] == 0) {
                    count++
                    lastPosition = position
                }
            }
        }
        return count to lastPosition
    }

    private fun markSingleValue(
        round: Int,
        type: LogType,
        valIndex: Int,
        position: Int,
    ) {
        val value = valIndex + 1
        board.mark(position, round, value)
        board.historyRecorder.recordMove(round, type, value, position)
    }
}
