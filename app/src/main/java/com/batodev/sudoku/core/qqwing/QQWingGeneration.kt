package com.batodev.sudoku.core.qqwing

internal fun QQWing.generatePuzzle(): Boolean {
    return generatePuzzleSymmetry(Symmetry.NONE)
}

internal fun QQWing.generatePuzzleSymmetry(symmetry: Symmetry): Boolean {
    val effectiveSymmetry = if (symmetry == Symmetry.RANDOM) QQWingRandom.randomSymmetry else symmetry

    // Don't record history while generating.
    val recHistory = historyRecorder.recordHistory
    setRecordHistory(false)
    val lHistory = historyRecorder.logHistory
    setLogHistory(false)
    clearPuzzle()

    // Start by getting the randomness in order so that
    // each puzzle will be different from the last.
    shuffleRandomArrays()

    // Now solve the puzzle the whole way. The solve
    // uses random algorithms, so we should have a
    // really randomly totally filled sudoku
    // Even when starting from an empty grid
    solve()
    if (effectiveSymmetry == Symmetry.NONE) {
        // Rollback any square for which it is obvious that
        // the square doesn't contribute to a unique solution
        // (ie, squares that were filled by logic rather
        // than by guess)
        rollbackNonGuesses()
    }

    // Record all marked squares as the puzzle so
    // that we can call countSolutions without losing it.
    for (i in 0 until QQWing.BOARD_SIZE) {
        puzzle[i] = solution[i]
    }

    // Rerandomize everything so that we test squares
    // in a different order than they were added.
    shuffleRandomArrays()

    // Remove one value at a time and see if
    // the puzzle still has only one solution.
    // If it does, leave it out the point because
    // it is not needed.
    for (i in 0 until QQWing.BOARD_SIZE) {
        // check all the positions, but in shuffled order
        val position = randomBoardArray[i]
        if (puzzle[position] > 0) {
            removeCellIfNotNeeded(position, effectiveSymmetry)
        }
    }

    // Clear all solution info, leaving just the puzzle.
    reset()

    // Restore recording history.
    setRecordHistory(recHistory)
    setLogHistory(lHistory)
    return true
}

private fun QQWing.removeCellIfNotNeeded(position: Int, symmetry: Symmetry) {
    // try backing out the value and
    // counting solutions to the puzzle
    val (sym1, sym2, sym3) = computeSymmetricPositions(symmetry, position)
    val savedValue = puzzle[position]
    puzzle[position] = 0
    val savedSym1 = clearIfPresent(sym1)
    val savedSym2 = clearIfPresent(sym2)
    val savedSym3 = clearIfPresent(sym3)
    reset()
    if (countSolutions(2, true) > 1) {
        // Put it back in, it is needed
        puzzle[position] = savedValue
        restoreIfPresent(sym1, savedSym1)
        restoreIfPresent(sym2, savedSym2)
        restoreIfPresent(sym3, savedSym3)
    }
}

private fun QQWing.clearIfPresent(position: Int): Int {
    if (position < 0) return 0
    val saved = puzzle[position]
    puzzle[position] = 0
    return saved
}

private fun QQWing.restoreIfPresent(position: Int, savedValue: Int) {
    if (position >= 0 && savedValue != 0) puzzle[position] = savedValue
}

private fun computeSymmetricPositions(symmetry: Symmetry, position: Int): Triple<Int, Int, Int> {
    val size = QQWing.ROW_COL_SEC_SIZE
    val row = cellToRowInternal(position)
    val column = cellToColumnInternal(position)
    return when (symmetry) {
        Symmetry.ROTATE90 -> Triple(
            rowColumnToCellInternal(size - 1 - row, size - 1 - column),
            rowColumnToCellInternal(size - 1 - column, row),
            rowColumnToCellInternal(column, size - 1 - row)
        )

        Symmetry.ROTATE180 -> Triple(rowColumnToCellInternal(size - 1 - row, size - 1 - column), -1, -1)
        Symmetry.MIRROR -> Triple(rowColumnToCellInternal(row, size - 1 - column), -1, -1)
        Symmetry.FLIP -> Triple(rowColumnToCellInternal(size - 1 - row, column), -1, -1)
        else -> Triple(-1, -1, -1)
    }
}

private fun QQWing.rollbackNonGuesses() {
    // Guesses are odd rounds
    // Non-guesses are even rounds
    var i = 2
    while (i <= lastSolveRound) {
        rollbackRound(i)
        i += 2
    }
}

private fun QQWing.clearPuzzle() {
    // Clear any existing puzzle
    for (i in 0 until QQWing.BOARD_SIZE) {
        puzzle[i] = 0
    }
    reset()
}

internal fun QQWing.shuffleRandomArrays() {
    QQWingRandom.shuffleArray(randomBoardArray, QQWing.BOARD_SIZE)
    QQWingRandom.shuffleArray(randomPossibilityArray, QQWing.ROW_COL_SEC_SIZE)
}
