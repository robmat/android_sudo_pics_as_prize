package com.batodev.sudoku.ui.settings

import androidx.lifecycle.viewModelScope
import com.batodev.sudoku.data.datastore.setAutoEraseNotes
import com.batodev.sudoku.data.datastore.setHighlightMistakes
import com.batodev.sudoku.data.datastore.setHintsDisabled
import com.batodev.sudoku.data.datastore.setInputMethod
import com.batodev.sudoku.data.datastore.setMistakesLimit
import com.batodev.sudoku.data.datastore.setRemainingUse
import com.batodev.sudoku.data.datastore.setResetTimer
import com.batodev.sudoku.data.datastore.setSameValuesHighlight
import com.batodev.sudoku.data.datastore.setTimer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

fun SettingsViewModel.updateMistakesLimit(enabled: Boolean) =
    viewModelScope.launch(Dispatchers.IO) {
        settingsDataManager.setMistakesLimit(enabled)
    }

fun SettingsViewModel.updateTimer(enabled: Boolean) =
    viewModelScope.launch(Dispatchers.IO) {
        settingsDataManager.setTimer(enabled)
    }

fun SettingsViewModel.updateCanResetTimer(enabled: Boolean) =
    viewModelScope.launch(Dispatchers.IO) {
        settingsDataManager.setResetTimer(enabled)
    }

fun SettingsViewModel.updateHighlightIdentical(enabled: Boolean) =
    viewModelScope.launch(Dispatchers.IO) {
        settingsDataManager.setSameValuesHighlight(enabled)
    }

fun SettingsViewModel.updateHintDisabled(disabled: Boolean) {
    viewModelScope.launch(Dispatchers.IO) {
        settingsDataManager.setHintsDisabled(disabled)
    }
}

fun SettingsViewModel.updateRemainingUse(enabled: Boolean) {
    viewModelScope.launch(Dispatchers.IO) {
        settingsDataManager.setRemainingUse(enabled)
    }
}

fun SettingsViewModel.updateAutoEraseNotes(enabled: Boolean) {
    viewModelScope.launch(Dispatchers.IO) {
        settingsDataManager.setAutoEraseNotes(enabled)
    }
}

fun SettingsViewModel.updateMistakesHighlight(index: Int) {
    viewModelScope.launch(Dispatchers.IO) {
        settingsDataManager.setHighlightMistakes(index)
    }
}

fun SettingsViewModel.updateInputMethod(value: Int) {
    viewModelScope.launch(Dispatchers.IO) {
        settingsDataManager.setInputMethod(value)
    }
}
