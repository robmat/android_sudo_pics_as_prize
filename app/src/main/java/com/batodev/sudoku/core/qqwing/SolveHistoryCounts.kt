package com.batodev.sudoku.core.qqwing

/**
 * Get the number of cells for which the solution was determined because
 * there was only one possible value for that cell.
 */
internal fun SolveHistoryRecorder.getSingleCount(): Int = getLogCount(solveInstructions, LogType.SINGLE)

/**
 * Get the number of cells for which the solution was determined because
 * that cell had the only possibility for some value in the row, column, or
 * section.
 */
internal fun SolveHistoryRecorder.getHiddenSingleCount(): Int =
    getLogCount(solveInstructions, LogType.HIDDEN_SINGLE_ROW) +
        getLogCount(solveInstructions, LogType.HIDDEN_SINGLE_COLUMN) +
        getLogCount(solveInstructions, LogType.HIDDEN_SINGLE_SECTION)

/**
 * Get the number of naked pair reductions that were performed in solving
 * this puzzle.
 */
internal fun SolveHistoryRecorder.getNakedPairCount(): Int =
    getLogCount(solveInstructions, LogType.NAKED_PAIR_ROW) +
        getLogCount(solveInstructions, LogType.NAKED_PAIR_COLUMN) +
        getLogCount(solveInstructions, LogType.NAKED_PAIR_SECTION)

/**
 * Get the number of hidden pair reductions that were performed in solving
 * this puzzle.
 */
internal fun SolveHistoryRecorder.getHiddenPairCount(): Int =
    getLogCount(solveInstructions, LogType.HIDDEN_PAIR_ROW) +
        getLogCount(solveInstructions, LogType.HIDDEN_PAIR_COLUMN) +
        getLogCount(solveInstructions, LogType.HIDDEN_PAIR_SECTION)

/**
 * Get the number of pointing pair/triple reductions that were performed in
 * solving this puzzle.
 */
internal fun SolveHistoryRecorder.getPointingPairTripleCount(): Int =
    getLogCount(solveInstructions, LogType.POINTING_PAIR_TRIPLE_ROW) +
        getLogCount(solveInstructions, LogType.POINTING_PAIR_TRIPLE_COLUMN)

/**
 * Get the number of box/line reductions that were performed in solving this
 * puzzle.
 */
internal fun SolveHistoryRecorder.getBoxLineReductionCount(): Int =
    getLogCount(solveInstructions, LogType.ROW_BOX) + getLogCount(solveInstructions, LogType.COLUMN_BOX)

/**
 * Get the number lucky guesses in solving this puzzle.
 */
internal fun SolveHistoryRecorder.getGuessCount(): Int = getLogCount(solveInstructions, LogType.GUESS)

/**
 * Get the number of backtracks (unlucky guesses) required when solving this
 * puzzle.
 */
internal fun SolveHistoryRecorder.getBacktrackCount(): Int = getLogCount(solveHistory, LogType.ROLLBACK)
