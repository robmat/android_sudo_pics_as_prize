package com.batodev.sudoku.data.database.converters

import androidx.room.TypeConverter
import com.batodev.sudoku.core.qqwing.GameType

/**
 * Converts GameType
 */
class GameTypeConverter {
    companion object {
        private const val DEFAULT_6X6_CODE = 3
    }

    @TypeConverter
    fun fromType(gameType: GameType): Int =
        when (gameType) {
            GameType.Unspecified -> 0
            GameType.Default9x9 -> 1
            GameType.Default12x12 -> 2
            GameType.Default6x6 -> DEFAULT_6X6_CODE
        }

    @TypeConverter
    fun toType(value: Int): GameType =
        when (value) {
            0 -> GameType.Unspecified
            1 -> GameType.Default9x9
            2 -> GameType.Default12x12
            DEFAULT_6X6_CODE -> GameType.Default6x6
            else -> GameType.Unspecified
        }
}
