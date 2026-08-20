package com.batodev.sudoku.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.batodev.sudoku.core.PreferencesConstants
import com.batodev.sudoku.core.qqwing.GameDifficulty
import com.batodev.sudoku.core.qqwing.GameType
import kotlinx.coroutines.flow.map
import java.time.chrono.IsoChronology
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.time.format.FormatStyle
import java.util.Locale
import javax.inject.Singleton

@Singleton
class AppSettingsManager(
    context: Context,
) {
    private val Context.createDataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")
    internal val dataStore = context.createDataStore

    // first app launch
    internal val firstLaunchKey = booleanPreferencesKey("first_launch")

    // input method (0 -> cell first, 1 -> digit first)
    internal val inputMethodKey = intPreferencesKey("input_method")

    // mistakes limit
    internal val mistakesLimitKey = booleanPreferencesKey("mistakes_limit")

    // disable hint button
    internal val hintsDisabledKey = booleanPreferencesKey("hints_disabled")

    // show timer
    internal val timerKey = booleanPreferencesKey("timer")

    // game reset resets timer
    internal val resetTimerKey = booleanPreferencesKey("timer_reset")

    // highlight mistakes
    internal val highlightMistakesKey = intPreferencesKey("mistakes_highlight")

    // highlight same numbers
    internal val highlightIdenticalKey = booleanPreferencesKey("same_values_highlight")

    // count and show remaining uses for numbers
    internal val remainingUseKey = booleanPreferencesKey("remaining_use")

    // highlight current position with horizontal and vertical lines
    internal val positionLinesKey = booleanPreferencesKey("position_lines")

    // auto erase notes
    internal val autoEraseNotesKey = booleanPreferencesKey("notes_auto_erase")

    // font size (0 - small, 1 - medium (default), 2 - big)
    internal val fontSizeKey = intPreferencesKey("font_size")

    // keep screen on
    internal val keepScreenOnKey = booleanPreferencesKey("keep_screen_on")

    // first game
    internal val firstGameKey = booleanPreferencesKey("first_game")

    // place function keyboard (undo, erase etc.) above the numbers keyboard
    internal val funKeyboardOverNumKey = booleanPreferencesKey("fun_keyboard_over_numbers")

    // custom date format
    internal val dateFormatKey = stringPreferencesKey("date_format")

    // whether to save the last selected type and difficulty in the HomeScreen
    internal val saveSelectedGameDifficultyTypeKey =
        booleanPreferencesKey("save_last_selected_difficulty_type")

    // last selected difficulty and type
    internal val lastSelectedGameDifficultyTypeKey =
        stringPreferencesKey("last_selected_difficulty_type")

    val firstLaunch =
        dataStore.data.map { preferences ->
            preferences[firstLaunchKey] ?: true
        }

    val mistakesLimit =
        dataStore.data.map { preferences ->
            preferences[mistakesLimitKey] ?: PreferencesConstants.DEFAULT_MISTAKES_LIMIT
        }

    val hintsDisabled =
        dataStore.data.map { preferences ->
            preferences[hintsDisabledKey] ?: PreferencesConstants.DEFAULT_HINTS_DISABLED
        }

    val timerEnabled =
        dataStore.data.map { preferences ->
            preferences[timerKey] ?: PreferencesConstants.DEFAULT_SHOW_TIMER
        }

    val resetTimerEnabled =
        dataStore.data.map { preferences ->
            preferences[resetTimerKey] ?: PreferencesConstants.DEFAULT_GAME_RESET_TIMER
        }

    val highlightMistakes =
        dataStore.data.map { preferences ->
            preferences[highlightMistakesKey] ?: PreferencesConstants.DEFAULT_HIGHLIGHT_MISTAKES
        }

    val highlightIdentical =
        dataStore.data.map { preferences ->
            preferences[highlightIdenticalKey] ?: PreferencesConstants.DEFAULT_HIGHLIGHT_IDENTICAL
        }

    val remainingUse =
        dataStore.data.map { preferences ->
            preferences[remainingUseKey] ?: PreferencesConstants.DEFAULT_REMAINING_USES
        }

    val positionLines =
        dataStore.data.map { preferences ->
            preferences[positionLinesKey] ?: PreferencesConstants.DEFAULT_POSITION_LINES
        }

    val autoEraseNotes =
        dataStore.data.map { preferences ->
            preferences[autoEraseNotesKey] ?: PreferencesConstants.DEFAULT_AUTO_ERASE_NOTES
        }

    val inputMethod =
        dataStore.data.map { preferences ->
            preferences[inputMethodKey] ?: PreferencesConstants.DEFAULT_INPUT_METHOD
        }

    val fontSize =
        dataStore.data.map { preferences ->
            preferences[fontSizeKey] ?: PreferencesConstants.DEFAULT_FONT_SIZE_FACTOR
        }

    val keepScreenOn =
        dataStore.data.map { preferences ->
            preferences[keepScreenOnKey] ?: PreferencesConstants.DEFAULT_KEEP_SCREEN_ON
        }

    val firstGame =
        dataStore.data.map { preferences ->
            preferences[firstGameKey] ?: true
        }

    val funKeyboardOverNumbers =
        dataStore.data.map { prefs ->
            prefs[funKeyboardOverNumKey] ?: PreferencesConstants.DEFAULT_FUN_KEYBOARD_OVER_NUM
        }

    val dateFormat =
        dataStore.data.map { prefs ->
            prefs[dateFormatKey] ?: ""
        }

    /**
     * Whether to save the last selected type and difficulty in the HomeScreen
     */
    val saveSelectedGameDifficultyType =
        dataStore.data.map { prefs ->
            prefs[saveSelectedGameDifficultyTypeKey]
                ?: PreferencesConstants.DEFAULT_SAVE_LAST_SELECTED_DIFF_TYPE
        }

    /**
     * Last selected difficulty and type. Returns Pair<GameDifficulty, GameType>
     */
    val lastSelectedGameDifficultyType =
        dataStore.data.map { prefs ->
            var gameDifficulty = GameDifficulty.Easy
            var gameType = GameType.Default9x9

            val key = prefs[lastSelectedGameDifficultyTypeKey] ?: ""
            if (key.isNotEmpty() && key.contains(";")) {
                gameDifficulty =
                    when (key.substring(0, key.indexOf(";"))) {
                        "0" -> GameDifficulty.Unspecified
                        "1" -> GameDifficulty.Simple
                        "2" -> GameDifficulty.Easy
                        "3" -> GameDifficulty.Moderate
                        "4" -> GameDifficulty.Hard
                        "5" -> GameDifficulty.Challenge
                        "6" -> GameDifficulty.Custom
                        else -> GameDifficulty.Easy
                    }
                gameType =
                    when (key.substring(key.indexOf(";") + 1)) {
                        "0" -> GameType.Unspecified
                        "1" -> GameType.Default9x9
                        "2" -> GameType.Default12x12
                        "3" -> GameType.Default6x6
                        else -> GameType.Default9x9
                    }
            }
            Pair(gameDifficulty, gameType)
        }

    companion object {
        fun dateFormat(format: String): DateTimeFormatter =
            when (format) {
                "" -> {
                    DateTimeFormatter.ofPattern(
                        DateTimeFormatterBuilder.getLocalizedDateTimePattern(
                            FormatStyle.SHORT,
                            null,
                            IsoChronology.INSTANCE,
                            Locale.getDefault(),
                        ),
                    )
                }

                else -> {
                    DateTimeFormatter.ofPattern(format, Locale.getDefault())
                }
            }
    }
}
