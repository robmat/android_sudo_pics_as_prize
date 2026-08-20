package com.batodev.sudoku.data.database.converters

import androidx.room.TypeConverter
import com.batodev.sudoku.core.qqwing.GameDifficulty

/**
 * Converts Game Difficulty
 */
class GameDifficultyConverter {
    companion object {
        private const val UNSPECIFIED_ORDINAL = 0
        private const val SIMPLE_ORDINAL = 1
        private const val EASY_ORDINAL = 2
        private const val MODERATE_ORDINAL = 3
        private const val HARD_ORDINAL = 4
        private const val CHALLENGE_ORDINAL = 5
        private const val CUSTOM_ORDINAL = 6
    }

    @TypeConverter
    fun fromDifficulty(gameDifficulty: GameDifficulty): Int =
        when (gameDifficulty) {
            GameDifficulty.Unspecified -> UNSPECIFIED_ORDINAL
            GameDifficulty.Simple -> SIMPLE_ORDINAL
            GameDifficulty.Easy -> EASY_ORDINAL
            GameDifficulty.Moderate -> MODERATE_ORDINAL
            GameDifficulty.Hard -> HARD_ORDINAL
            GameDifficulty.Challenge -> CHALLENGE_ORDINAL
            GameDifficulty.Custom -> CUSTOM_ORDINAL
        }

    @TypeConverter
    fun toDifficulty(value: Int): GameDifficulty =
        when (value) {
            UNSPECIFIED_ORDINAL -> GameDifficulty.Unspecified
            SIMPLE_ORDINAL -> GameDifficulty.Simple
            EASY_ORDINAL -> GameDifficulty.Easy
            MODERATE_ORDINAL -> GameDifficulty.Moderate
            HARD_ORDINAL -> GameDifficulty.Hard
            CHALLENGE_ORDINAL -> GameDifficulty.Challenge
            CUSTOM_ORDINAL -> GameDifficulty.Custom
            else -> GameDifficulty.Unspecified
        }
}
