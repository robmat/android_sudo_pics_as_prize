package com.batodev.sudoku.core.qqwing

/**
 * Keeps track of the moves made while solving a puzzle, both the full
 * history (including backed-out guesses) and just the moves that were
 * needed to reach the solution.
 */
internal class SolveHistoryRecorder {
    /**
     * A list of moves used to solve the puzzle. This list contains all moves,
     * even on solve branches that did not lead to a solution.
     */
    internal val solveHistory = ArrayList<LogItem?>()

    /**
     * A list of moves used to solve the puzzle. This list contains only the
     * moves needed to solve the puzzle, but doesn't contain information about
     * bad guesses.
     */
    internal val solveInstructions = ArrayList<LogItem?>()

    var recordHistory = true
    var logHistory = false

    fun clear() {
        solveHistory.clear()
        solveInstructions.clear()
    }

    /**
     * Record a move, if history recording or logging is enabled.
     */
    fun recordMove(round: Int, type: LogType, value: Int = 0, position: Int = -1) {
        if (!logHistory && !recordHistory) return
        val item = LogItem(round, type, value, position)
        if (logHistory) {
            item.print()
            println()
        }
        if (recordHistory) {
            solveHistory.add(item)
            solveInstructions.add(item)
        }
    }

    /**
     * Remove the trailing solve instructions that belong to the given round,
     * used when a round is being rolled back.
     */
    fun removeInstructionsForRound(round: Int) {
        while (solveInstructions.isNotEmpty() && solveInstructions.last()!!.round == round) {
            solveInstructions.removeAt(solveInstructions.size - 1)
        }
    }

    fun printHistory(v: ArrayList<LogItem?>) {
        print(historyToString(v))
    }

    fun historyToString(v: ArrayList<LogItem?>): String {
        val sb = StringBuilder()
        if (!recordHistory) {
            appendNotRecordedHeader(sb)
        }
        for (i in v.indices) {
            appendHistoryLine(sb, i, v[i])
        }
        appendHistoryFooter(sb)
        return sb.toString()
    }

    private fun appendNotRecordedHeader(sb: StringBuilder) {
        sb.append("History was not recorded.").append(NL)
        sb.append(if (printStyle == PrintStyle.CSV) " -- " else "").append(NL)
    }

    private fun appendHistoryLine(sb: StringBuilder, index: Int, item: LogItem?) {
        sb.append((index + 1).toString() + ". ").append(NL)
        item!!.print()
        sb.append(if (printStyle == PrintStyle.CSV) " -- " else "").append(NL)
    }

    private fun appendHistoryFooter(sb: StringBuilder) {
        sb.append(if (printStyle == PrintStyle.CSV) "," else "").append(NL)
    }

    var printStyle = PrintStyle.READABLE

    /**
     * Given a vector of LogItems, determine how many log items in the vector
     * are of the specified type.
     */
    fun getLogCount(v: ArrayList<LogItem?>, type: LogType): Int {
        var count = 0
        for (i in v.indices) {
            if (v[i]!!.type == type) count++
        }
        return count
    }
}
