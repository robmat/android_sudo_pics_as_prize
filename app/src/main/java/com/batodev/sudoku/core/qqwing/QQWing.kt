package com.batodev.sudoku.core.qqwing

// @formatter:off

/*
 * qqwing - Sudoku solver and generator
 * Copyright (C) 2006-2014 Stephen Ostermiller http://ostermiller.org/
 * Copyright (C) 2007 Jacques Bensimon (jacques@ipm.com)
 * Copyright (C) 2007 Joel Yarde (joel.yarde - gmail.com)
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

// @formatter:on

/**
 * The board containing all the memory structures needed for solving or
 * generating sudoku puzzles.
 *
 * This class only holds state (the puzzle/solution/possibilities arrays,
 * solve history, and a handful of settings). The solving and generating
 * logic that operates on that state lives in extension functions spread
 * across QQWingBoardState.kt, QQWingSolving.kt, QQWingCounting.kt,
 * QQWingGeneration.kt, QQWingDifficulty.kt, QQWingSettings.kt,
 * QQWingPuzzlePrinting.kt and QQWingHistoryReporting.kt, plus a handful of
 * small solving-technique helper classes (e.g. [SingleValueTechniques],
 * [NakedPairTechniques]) - kept this way so that no single file/class grows
 * unwieldy.
 */
class QQWing(
    type: GameType,
    difficulty: GameDifficulty,
) {
    internal val historyRecorder = SolveHistoryRecorder()

    /**
     * The last round of solving
     */
    internal var lastSolveRound = 0

    /**
     * The integers that make up a sudoku puzzle. Givens are 1-9, unknowns
     * are 0. Once initialized, this puzzle remains as is. The answer is worked
     * out in "solution".
     */
    var puzzle = IntArray(BOARD_SIZE)

    /**
     * The integers that make up a sudoku puzzle. The solution is built here,
     * after completion all will be non-zero.
     */
    var solution = IntArray(BOARD_SIZE)

    /**
     * Recursion depth at which each of the numbers in the solution were placed.
     * Useful for backing out solve branches that don't lead to a solution.
     */
    internal var solutionRound = IntArray(BOARD_SIZE)

    /**
     * The integers that make up the possible values for a Sudoku puzzle. If
     * possibilities[i] is zero, then the possibility could still be filled in
     * according to the Sudoku rules. When a possibility is eliminated,
     * possibilities[i] is assigned the round (recursion level) at which it
     * was determined that it could not be a possibility.
     */
    internal var possibilities = IntArray(POSSIBILITY_SIZE)

    /**
     * An array the size of the board containing each of the numbers 0-n
     * exactly once. This array may be shuffled so that operations that need to
     * look at each cell can do so in a random order.
     */
    internal var randomBoardArray = QQWingRandom.fillIncrementing(IntArray(BOARD_SIZE))

    /**
     * An array with one element for each position, in some random order to
     * be used when trying each position in turn during guesses.
     */
    internal var randomPossibilityArray = QQWingRandom.fillIncrementing(IntArray(ROW_COL_SEC_SIZE))

    internal var gameType = GameType.Unspecified
    var difficulty = GameDifficulty.Unspecified

    internal val singleValueTechniques = SingleValueTechniques(this)
    internal val nakedPairTechniques = NakedPairTechniques(this)
    internal val hiddenPairTechniques = HiddenPairTechniques(this)
    internal val boxLineTechniques = BoxLineTechniques(this)
    internal val pointingTechniques = PointingTechniques(this)

    /*
     * Create a new Sudoku board
     */
    init {
        gameType = type
        this.difficulty = difficulty
        GRID_SIZE_ROW = type.sectionHeight
        GRID_SIZE_COL = type.sectionWidth
        ROW_COL_SEC_SIZE = GRID_SIZE_ROW * GRID_SIZE_COL
        SEC_GROUP_SIZE = ROW_COL_SEC_SIZE * GRID_SIZE_ROW
        BOARD_SIZE = ROW_COL_SEC_SIZE * ROW_COL_SEC_SIZE
        POSSIBILITY_SIZE = BOARD_SIZE * ROW_COL_SEC_SIZE
        puzzle = IntArray(BOARD_SIZE)
        solution = IntArray(BOARD_SIZE)
        solutionRound = IntArray(BOARD_SIZE)
        possibilities = IntArray(POSSIBILITY_SIZE)
        randomBoardArray = QQWingRandom.fillIncrementing(IntArray(BOARD_SIZE))
        randomPossibilityArray = QQWingRandom.fillIncrementing(IntArray(ROW_COL_SEC_SIZE))
    }

    companion object {
        const val QQWING_VERSION = "1.3.4"
        var GRID_SIZE_ROW = 3
        var GRID_SIZE_COL = 3
        var ROW_COL_SEC_SIZE = GRID_SIZE_ROW * GRID_SIZE_COL
        var SEC_GROUP_SIZE = ROW_COL_SEC_SIZE * GRID_SIZE_ROW
        var BOARD_SIZE = ROW_COL_SEC_SIZE * ROW_COL_SEC_SIZE
        var POSSIBILITY_SIZE = BOARD_SIZE * ROW_COL_SEC_SIZE
    }
}
