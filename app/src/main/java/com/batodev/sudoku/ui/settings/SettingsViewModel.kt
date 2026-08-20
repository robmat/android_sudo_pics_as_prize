package com.batodev.sudoku.ui.settings

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.batodev.sudoku.data.database.AppDatabase
import com.batodev.sudoku.data.datastore.AcraSharedPrefs
import com.batodev.sudoku.data.datastore.AppSettingsManager
import com.batodev.sudoku.data.datastore.ThemeSettingsManager
import com.batodev.sudoku.data.datastore.TipCardsDataStore
import com.batodev.sudoku.ui.theme.AppTheme
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel
@Inject constructor(
    internal val settingsDataManager: AppSettingsManager,
    private val tipCardsDataStore: TipCardsDataStore,
    private val appDatabase: AppDatabase,
    private val acraSharedPrefs: AcraSharedPrefs,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    @Inject
    lateinit var appThemeDataStore: ThemeSettingsManager

    val launchedFromGame by mutableStateOf(savedStateHandle.get<Boolean>("fromGame"))
    var resetStatsDialog by mutableStateOf(false)

    var darkModeDialog by mutableStateOf(false)
    var fontSizeDialog by mutableStateOf(false)
    var inputMethodDialog by mutableStateOf(false)
    var mistakesDialog by mutableStateOf(false)
    var languagePickDialog by mutableStateOf(false)
    var dateFormatDialog by mutableStateOf(false)
    var customFormatDialog by mutableStateOf(false)

    var crashReportingEnabled by mutableStateOf(acraSharedPrefs.getAcraEnabled())

    val darkTheme by lazy {
        appThemeDataStore.darkTheme
    }

    fun updateDarkTheme(value: Int) =
        viewModelScope.launch(Dispatchers.IO) {
            appThemeDataStore.setDarkTheme(value)
        }

    val dynamicColors by lazy {
        appThemeDataStore.dynamicColors
    }

    fun updateDynamicColors(enabled: Boolean) =
        viewModelScope.launch {
            appThemeDataStore.setDynamicColors(enabled)
        }

    val amoledBlack by lazy {
        appThemeDataStore.amoledBlack
    }

    fun updateAmoledBlack(enabled: Boolean) =
        viewModelScope.launch(Dispatchers.IO) {
            appThemeDataStore.setAmoledBlack(enabled)
        }

    val mistakesLimit = settingsDataManager.mistakesLimit
    val timer = settingsDataManager.timerEnabled
    val canResetTimer = settingsDataManager.resetTimerEnabled
    val highlightIdentical = settingsDataManager.highlightIdentical
    val disableHints = settingsDataManager.hintsDisabled
    val remainingUse = settingsDataManager.remainingUse
    val autoEraseNotes = settingsDataManager.autoEraseNotes
    val highlightMistakes = settingsDataManager.highlightMistakes
    val inputMethod = settingsDataManager.inputMethod

    fun resetTipCards() {
        viewModelScope.launch {
            tipCardsDataStore.setStreakCard(true)
            tipCardsDataStore.setRecordCard(true)
        }
    }

    fun deleteAllTables() {
        viewModelScope.launch(Dispatchers.IO) {
            appDatabase.clearAllTables()
        }
    }

    val fontSize = settingsDataManager.fontSize

    val currentTheme by lazy {
        appThemeDataStore.currentTheme
    }

    fun updateCurrentTheme(theme: AppTheme) {
        viewModelScope.launch(Dispatchers.IO) {
            appThemeDataStore.setCurrentTheme(theme)
        }
    }

    val keepScreenOn = settingsDataManager.keepScreenOn

    fun updateCrashReportingEnabled(enabled: Boolean) {
        acraSharedPrefs.setAcraEnabled(enabled)
        crashReportingEnabled = acraSharedPrefs.getAcraEnabled()
    }

    val funKeyboardOverNum = settingsDataManager.funKeyboardOverNumbers
    val dateFormat = settingsDataManager.dateFormat
    val saveLastSelectedDifficultyType = settingsDataManager.saveSelectedGameDifficultyType

    fun checkCustomDateFormat(pattern: String): Boolean {
        return try {
            DateTimeFormatter.ofPattern(pattern)
            true
        } catch (_: IllegalArgumentException) {
            false
        }
    }
}
