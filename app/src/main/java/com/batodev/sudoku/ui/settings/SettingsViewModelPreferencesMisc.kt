package com.batodev.sudoku.ui.settings

import androidx.lifecycle.viewModelScope
import com.batodev.sudoku.data.datastore.setDateFormat
import com.batodev.sudoku.data.datastore.setFontSize
import com.batodev.sudoku.data.datastore.setFunKeyboardOverNum
import com.batodev.sudoku.data.datastore.setKeepScreenOn
import com.batodev.sudoku.data.datastore.setSaveSelectedGameDifficultyType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

fun SettingsViewModel.updateFontSize(value: Int) {
    viewModelScope.launch(Dispatchers.IO) {
        settingsDataManager.setFontSize(value)
    }
}

fun SettingsViewModel.updateKeepScreenOn(enabled: Boolean) {
    viewModelScope.launch(Dispatchers.IO) {
        settingsDataManager.setKeepScreenOn(enabled)
    }
}

fun SettingsViewModel.updateFunKeyboardOverNum(enabled: Boolean) {
    viewModelScope.launch(Dispatchers.IO) {
        settingsDataManager.setFunKeyboardOverNum(enabled)
    }
}

fun SettingsViewModel.updateDateFormat(format: String) {
    viewModelScope.launch(Dispatchers.IO) {
        settingsDataManager.setDateFormat(format)
    }
}

fun SettingsViewModel.updateSaveLastSelectedDifficultyType(enabled: Boolean) =
    viewModelScope.launch(Dispatchers.IO) {
        settingsDataManager.setSaveSelectedGameDifficultyType(enabled)
    }
