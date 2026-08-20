package com.batodev.sudoku.core.qqwing

/**
 * Count the number of solutions to the puzzle
 */
internal fun QQWing.countSolutions(): Int {
    return countSolutions(false)
}

/**
 * Count the number of solutions to the puzzle but return two any time there
 * are two or more solutions. This method will run much faster than
 * countSolutions() when there are many possible solutions and can be used
 * when you are interested in knowing if the puzzle has zero, one, or
 * multiple solutions.
 */
internal fun QQWing.countSolutionsLimited(): Int {
    return countSolutions(true)
}

private fun QQWing.countSolutions(limitToTwo: Boolean): Int {
    // Don't record history while generating.
    val recHistory = historyRecorder.recordHistory
    setRecordHistory(false)
    val lHistory = historyRecorder.logHistory
    setLogHistory(false)
    reset()
    val solutionCount = countSolutions(2, limitToTwo)

    // Restore recording history.
    setRecordHistory(recHistory)
    setLogHistory(lHistory)
    return solutionCount
}

internal fun QQWing.countSolutions(round: Int, limitToTwo: Boolean): Int {
    val result = countSolutionsAfterSingleMoves(round) ?: countSolutionsByGuessing(round, limitToTwo)
    rollbackRound(round)
    return result
}

private fun QQWing.countSolutionsAfterSingleMoves(round: Int): Int? {
    var result: Int? = null
    while (result == null && singleSolveMove(round)) {
        result = when {
            isSolved() -> 1
            isImpossible() -> 0
            else -> null
        }
    }
    return result
}

private fun QQWing.countSolutionsByGuessing(round: Int, limitToTwo: Boolean): Int {
    var solutions = 0
    val nextRound = round + 1
    var guessNumber = 0
    var stop = false
    while (!stop && guess(nextRound, guessNumber)) {
        solutions += countSolutions(nextRound, limitToTwo)
        if (limitToTwo && solutions >= 2) {
            stop = true
        } else {
            guessNumber++
        }
    }
    return solutions
}

private fun QQWing.findPositionWithFewestPossibilities(): Int {
    var minPossibilities = QQWing.ROW_COL_SEC_SIZE + 1
    var bestPosition = 0
    for (i in 0 until QQWing.BOARD_SIZE) {
        val position = randomBoardArray[i]
        if (solution[position] == 0) {
            val count = countOpenPossibilities(position)
            if (count < minPossibilities) {
                minPossibilities = count
                bestPosition = position
            }
        }
    }
    return bestPosition
}

private fun QQWing.countOpenPossibilities(position: Int): Int {
    var count = 0
    for (valIndex in 0 until QQWing.ROW_COL_SEC_SIZE) {
        val valPos = getPossibilityIndexInternal(valIndex, position)
        if (possibilities[valPos] == 0) count++
    }
    return count
}

internal fun QQWing.guess(round: Int, guessNumber: Int): Boolean {
    var localGuessCount = 0
    val position = findPositionWithFewestPossibilities()
    for (i in 0 until QQWing.ROW_COL_SEC_SIZE) {
        val valIndex = randomPossibilityArray[i]
        val valPos = getPossibilityIndexInternal(valIndex, position)
        if (possibilities[valPos] != 0) continue
        if (localGuessCount == guessNumber) {
            val value = valIndex + 1
            mark(position, round, value)
            historyRecorder.recordMove(round, LogType.GUESS, value, position)
            return true
        }
        localGuessCount++
    }
    return false
}

internal fun QQWing.singleSolveMove(round: Int): Boolean {
    val techniques = listOf<(Int) -> Boolean>(
        singleValueTechniques::onlyPossibilityForCell,
        singleValueTechniques::onlyValueInSection,
        singleValueTechniques::onlyValueInRow,
        singleValueTechniques::onlyValueInColumn,
        nakedPairTechniques::handleNakedPairs,
        pointingTechniques::pointingRowReduction,
        pointingTechniques::pointingColumnReduction,
        boxLineTechniques::rowBoxReduction,
        boxLineTechniques::colBoxReduction,
        hiddenPairTechniques::hiddenPairInRow,
        hiddenPairTechniques::hiddenPairInColumn,
        hiddenPairTechniques::hiddenPairInSection
    )
    return techniques.any { it(round) }
}
