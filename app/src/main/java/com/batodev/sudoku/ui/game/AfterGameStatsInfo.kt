package com.batodev.sudoku.ui.game

import com.batodev.sudoku.core.qqwing.GameDifficulty
import com.batodev.sudoku.core.qqwing.GameType
import com.batodev.sudoku.data.database.model.Record

data class AfterGameStatsInfo(
    val difficulty: GameDifficulty,
    val type: GameType,
    val hintsUsed: Int,
    val mistakesMade: Int,
    val mistakesLimit: Boolean,
    val mistakesLimitCount: Int,
    val giveUp: Boolean,
    val notesTaken: Int,
    val records: List<Record>,
    val timeText: String
)
