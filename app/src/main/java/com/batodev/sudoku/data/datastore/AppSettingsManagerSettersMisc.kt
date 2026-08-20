package com.batodev.sudoku.data.datastore

import androidx.datastore.preferences.core.edit

suspend fun AppSettingsManager.setAutoEraseNotes(enabled: Boolean) {
    dataStore.edit { settings ->
        settings[autoEraseNotesKey] = enabled
    }
}

suspend fun AppSettingsManager.setInputMethod(value: Int) {
    dataStore.edit { settings ->
        settings[inputMethodKey] = value
    }
}

suspend fun AppSettingsManager.setFontSize(value: Int) {
    dataStore.edit { settings ->
        settings[fontSizeKey] = value
    }
}

suspend fun AppSettingsManager.setKeepScreenOn(enabled: Boolean) {
    dataStore.edit { settings ->
        settings[keepScreenOnKey] = enabled
    }
}

suspend fun AppSettingsManager.setFirstGame(value: Boolean) {
    dataStore.edit { settings ->
        settings[firstGameKey] = value
    }
}

suspend fun AppSettingsManager.setFunKeyboardOverNum(enabled: Boolean) {
    dataStore.edit { settings ->
        settings[funKeyboardOverNumKey] = enabled
    }
}

suspend fun AppSettingsManager.setDateFormat(format: String) {
    dataStore.edit { settings ->
        settings[dateFormatKey] = format
    }
}

suspend fun AppSettingsManager.setSaveSelectedGameDifficultyType(enabled: Boolean) {
    dataStore.edit { settings ->
        settings[saveSelectedGameDifficultyTypeKey] = enabled
    }
}
