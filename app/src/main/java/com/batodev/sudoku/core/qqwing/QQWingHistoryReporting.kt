package com.batodev.sudoku.core.qqwing

import java.util.Collections

internal fun QQWing.printSolveInstructions() {
    print(getSolveInstructionsString())
}

internal fun QQWing.getSolveInstructionsString(): String =
    if (isSolved()) {
        historyRecorder.historyToString(historyRecorder.solveInstructions)
    } else {
        "No solve instructions - Puzzle is not possible to solve."
    }

internal fun QQWing.getSolveInstructions(): List<LogItem?> =
    if (isSolved()) {
        Collections.unmodifiableList(historyRecorder.solveInstructions)
    } else {
        emptyList()
    }

internal fun QQWing.printSolveHistory() {
    historyRecorder.printHistory(historyRecorder.solveHistory)
}

internal fun QQWing.getSolveHistoryString(): String = historyRecorder.historyToString(historyRecorder.solveHistory)

internal fun QQWing.getSolveHistory(): List<LogItem?> = Collections.unmodifiableList(historyRecorder.solveHistory)
