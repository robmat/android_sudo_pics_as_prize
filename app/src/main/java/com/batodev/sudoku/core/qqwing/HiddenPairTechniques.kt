package com.batodev.sudoku.core.qqwing

/**
 * The "hidden pairs" solving technique: if two values are only possible in
 * exactly the same two cells of a row, column or section, all other
 * candidate values can be eliminated from those two cells.
 *
 * The row/column/section variants share the exact same logic, differing
 * only in how a (group, index-within-group) pair maps to a board position.
 */
internal class HiddenPairTechniques(private val board: QQWing) {

    fun hiddenPairInRow(round: Int): Boolean {
        for (row in 0 until QQWing.ROW_COL_SEC_SIZE) {
            if (findHiddenPair(round, row, LogType.HIDDEN_PAIR_ROW) { r, col -> rowColumnToCellInternal(r, col) }) {
                return true
            }
        }
        return false
    }

    fun hiddenPairInColumn(round: Int): Boolean {
        for (column in 0 until QQWing.ROW_COL_SEC_SIZE) {
            val found = findHiddenPair(round, column, LogType.HIDDEN_PAIR_COLUMN) { col, row ->
                rowColumnToCellInternal(row, col)
            }
            if (found) return true
        }
        return false
    }

    fun hiddenPairInSection(round: Int): Boolean {
        for (section in 0 until QQWing.ROW_COL_SEC_SIZE) {
            val found = findHiddenPair(round, section, LogType.HIDDEN_PAIR_SECTION) { sec, secInd ->
                sectionToCellInternal(sec, secInd)
            }
            if (found) return true
        }
        return false
    }

    /**
     * Look for a hidden pair within one row/column/section (identified by
     * [group]), where [positionAt] maps an index within that group to a
     * board position.
     */
    private fun findHiddenPair(round: Int, group: Int, logType: LogType, positionAt: (Int, Int) -> Int): Boolean {
        for (valIndex in 0 until QQWing.ROW_COL_SEC_SIZE) {
            val (i1, i2, count) = countValueOccurrencesInGroup(group, valIndex, positionAt)
            if (count != 2) continue
            for (valIndex2 in valIndex + 1 until QQWing.ROW_COL_SEC_SIZE) {
                val (i3, i4, count2) = countValueOccurrencesInGroup(group, valIndex2, positionAt)
                if (count2 != 2 || i1 != i3 || i2 != i4) continue
                val position1 = positionAt(group, i1)
                val position2 = positionAt(group, i2)
                if (eliminateOtherCandidates(round, valIndex, valIndex2, position1, position2)) {
                    board.historyRecorder.recordMove(round, logType, valIndex + 1, position1)
                    return true
                }
            }
        }
        return false
    }

    private fun countValueOccurrencesInGroup(
        group: Int,
        valIndex: Int,
        positionAt: (Int, Int) -> Int
    ): Triple<Int, Int, Int> {
        var i1 = -1
        var i2 = -1
        var count = 0
        for (idx in 0 until QQWing.ROW_COL_SEC_SIZE) {
            val position = positionAt(group, idx)
            val valPos = getPossibilityIndexInternal(valIndex, position)
            if (board.possibilities[valPos] == 0) {
                if (i1 == -1 || i1 == idx) {
                    i1 = idx
                } else if (i2 == -1 || i2 == idx) {
                    i2 = idx
                }
                count++
            }
        }
        return Triple(i1, i2, count)
    }

    private fun eliminateOtherCandidates(
        round: Int,
        valIndex: Int,
        valIndex2: Int,
        position1: Int,
        position2: Int
    ): Boolean {
        var doneSomething = false
        for (valIndex3 in 0 until QQWing.ROW_COL_SEC_SIZE) {
            if (valIndex3 == valIndex || valIndex3 == valIndex2) continue
            val valPos1 = getPossibilityIndexInternal(valIndex3, position1)
            val valPos2 = getPossibilityIndexInternal(valIndex3, position2)
            if (board.possibilities[valPos1] == 0) {
                board.possibilities[valPos1] = round
                doneSomething = true
            }
            if (board.possibilities[valPos2] == 0) {
                board.possibilities[valPos2] = round
                doneSomething = true
            }
        }
        return doneSomething
    }
}
