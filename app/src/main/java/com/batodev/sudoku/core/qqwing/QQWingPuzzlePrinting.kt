package com.batodev.sudoku.core.qqwing

/**
 * Print the sudoku puzzle.
 */
internal fun QQWing.printPuzzle() {
    print(puzzleToString(puzzle, historyRecorder.printStyle))
}

internal fun QQWing.getPuzzleString(): String = puzzleToString(puzzle, historyRecorder.printStyle)

internal fun QQWing.getPuzzle(): IntArray = puzzle.clone()

/**
 * Print the sudoku solution.
 */
internal fun QQWing.printSolution() {
    print(puzzleToString(solution, historyRecorder.printStyle))
}

internal fun QQWing.getSolutionString(): String = puzzleToString(solution, historyRecorder.printStyle)

internal fun QQWing.getSolution(): IntArray = solution.clone()

/**
 * Print the given BOARD_SIZEd array of ints as a sudoku puzzle, using the
 * given print style.
 */
private fun puzzleToString(
    sudoku: IntArray,
    printStyle: PrintStyle,
): String {
    val sb = StringBuilder()
    for (i in 0 until QQWing.BOARD_SIZE) {
        appendCellValue(sb, sudoku[i], printStyle)
        appendCellSeparator(sb, i, printStyle)
    }
    return sb.toString()
}

private fun appendCellValue(
    sb: StringBuilder,
    value: Int,
    printStyle: PrintStyle,
) {
    if (printStyle == PrintStyle.READABLE) sb.append(" ")
    if (value == 0) sb.append('.') else sb.append(value)
}

private fun appendCellSeparator(
    sb: StringBuilder,
    i: Int,
    printStyle: PrintStyle,
) {
    val isReadableOrCompact = printStyle == PrintStyle.READABLE || printStyle == PrintStyle.COMPACT
    when {
        i == QQWing.BOARD_SIZE - 1 -> {
            sb.append(if (printStyle == PrintStyle.CSV) "," else NL)
            if (isReadableOrCompact) sb.append(NL)
        }

        i % QQWing.ROW_COL_SEC_SIZE == QQWing.ROW_COL_SEC_SIZE - 1 -> {
            if (isReadableOrCompact) sb.append(NL)
            if (i % QQWing.SEC_GROUP_SIZE == QQWing.SEC_GROUP_SIZE - 1 && printStyle == PrintStyle.READABLE) {
                sb.append("-------|-------|-------").append(NL)
            }
        }

        i % QQWing.GRID_SIZE_ROW == QQWing.GRID_SIZE_ROW - 1 -> {
            if (printStyle == PrintStyle.READABLE) sb.append(" |")
        }
    }
}
