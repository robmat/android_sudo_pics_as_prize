package com.batodev.sudoku.core.qqwing

internal fun QQWing.solve(): Boolean {
    reset()
    shuffleRandomArrays()
    return solve(2)
}

internal fun QQWing.solve(round: Int): Boolean {
    lastSolveRound = round
    return advanceWithSingleMoves(round) ?: solveByGuessing(round)
}

private fun QQWing.advanceWithSingleMoves(round: Int): Boolean? {
    var result: Boolean? = null
    while (result == null && singleSolveMove(round)) {
        result = when {
            isSolved() -> true
            isImpossible() -> false
            else -> null
        }
    }
    return result
}

private fun QQWing.solveByGuessing(round: Int): Boolean {
    val nextGuessRound = round + 1
    val nextRound = round + 2
    var guessNumber = 0
    var solved = false
    while (!solved && guess(nextGuessRound, guessNumber)) {
        solved = if (isImpossible() || !solve(nextRound)) {
            rollbackRound(nextRound)
            rollbackRound(nextGuessRound)
            false
        } else {
            true
        }
        guessNumber++
    }
    return solved
}

/**
 * return true if the puzzle has a solution and only a single solution
 */
internal fun QQWing.hasUniqueSolution(): Boolean {
    return countSolutionsLimited() == 1
}

internal fun QQWing.rollbackRound(round: Int) {
    historyRecorder.recordMove(round, LogType.ROLLBACK)
    for (i in 0 until QQWing.BOARD_SIZE) {
        if (solutionRound[i] == round) {
            solutionRound[i] = 0
            solution[i] = 0
        }
    }
    for (i in 0 until QQWing.POSSIBILITY_SIZE) {
        if (possibilities[i] == round) {
            possibilities[i] = 0
        }
    }
    historyRecorder.removeInstructionsForRound(round)
}
