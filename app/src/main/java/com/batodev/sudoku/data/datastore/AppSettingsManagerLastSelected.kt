package com.batodev.sudoku.data.datastore

import androidx.datastore.preferences.core.edit
import com.batodev.sudoku.core.qqwing.GameDifficulty
import com.batodev.sudoku.core.qqwing.GameType

private const val GAME_DIFFICULTY_UNSPECIFIED_CODE = "0"
private const val GAME_DIFFICULTY_SIMPLE_CODE = "1"
private const val GAME_DIFFICULTY_EASY_CODE = "2"
private const val GAME_DIFFICULTY_MODERATE_CODE = "3"
private const val GAME_DIFFICULTY_HARD_CODE = "4"
private const val GAME_DIFFICULTY_CHALLENGE_CODE = "5"
private const val GAME_DIFFICULTY_CUSTOM_CODE = "6"

private const val GAME_TYPE_UNSPECIFIED_CODE = "0"
private const val GAME_TYPE_9X9_CODE = "1"
private const val GAME_TYPE_12X12_CODE = "2"
private const val GAME_TYPE_6X6_CODE = "3"

private fun difficultyCode(difficulty: GameDifficulty): String =
    when (difficulty) {
        GameDifficulty.Unspecified -> GAME_DIFFICULTY_UNSPECIFIED_CODE
        GameDifficulty.Simple -> GAME_DIFFICULTY_SIMPLE_CODE
        GameDifficulty.Easy -> GAME_DIFFICULTY_EASY_CODE
        GameDifficulty.Moderate -> GAME_DIFFICULTY_MODERATE_CODE
        GameDifficulty.Hard -> GAME_DIFFICULTY_HARD_CODE
        GameDifficulty.Challenge -> GAME_DIFFICULTY_CHALLENGE_CODE
        GameDifficulty.Custom -> GAME_DIFFICULTY_CUSTOM_CODE
    }

private fun typeCode(type: GameType): String =
    when (type) {
        GameType.Unspecified -> GAME_TYPE_UNSPECIFIED_CODE
        GameType.Default9x9 -> GAME_TYPE_9X9_CODE
        GameType.Default12x12 -> GAME_TYPE_12X12_CODE
        GameType.Default6x6 -> GAME_TYPE_6X6_CODE
    }

suspend fun AppSettingsManager.setLastSelectedGameDifficultyType(
    difficulty: GameDifficulty,
    type: GameType,
) {
    dataStore.edit { settings ->
        val difficultyAndType = "${difficultyCode(difficulty)};${typeCode(type)}"
        settings[lastSelectedGameDifficultyTypeKey] = difficultyAndType
    }
}
