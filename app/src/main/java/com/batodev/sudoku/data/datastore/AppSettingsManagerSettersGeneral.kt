package com.batodev.sudoku.data.datastore

import androidx.datastore.preferences.core.edit

suspend fun AppSettingsManager.setFirstLaunch(value: Boolean) {
    dataStore.edit { settings ->
        settings[firstLaunchKey] = value
    }
}

suspend fun AppSettingsManager.setMistakesLimit(enabled: Boolean) {
    dataStore.edit { settings ->
        settings[mistakesLimitKey] = enabled
    }
}

suspend fun AppSettingsManager.setHintsDisabled(disabled: Boolean) {
    dataStore.edit { settings ->
        settings[hintsDisabledKey] = disabled
    }
}

suspend fun AppSettingsManager.setTimer(enabled: Boolean) {
    dataStore.edit { settings ->
        settings[timerKey] = enabled
    }
}

suspend fun AppSettingsManager.setResetTimer(enabled: Boolean) {
    dataStore.edit { settings ->
        settings[resetTimerKey] = enabled
    }
}

suspend fun AppSettingsManager.setHighlightMistakes(value: Int) {
    dataStore.edit { settings ->
        settings[highlightMistakesKey] = value
    }
}

suspend fun AppSettingsManager.setSameValuesHighlight(enabled: Boolean) {
    dataStore.edit { settings ->
        settings[highlightIdenticalKey] = enabled
    }
}

suspend fun AppSettingsManager.setRemainingUse(enabled: Boolean) {
    dataStore.edit { settings ->
        settings[remainingUseKey] = enabled
    }
}

suspend fun AppSettingsManager.setPositionLines(enabled: Boolean) {
    dataStore.edit { settings ->
        settings[positionLinesKey] = enabled
    }
}
