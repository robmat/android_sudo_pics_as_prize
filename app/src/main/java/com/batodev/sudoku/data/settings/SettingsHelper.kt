package com.batodev.sudoku.data.settings

import android.content.Context
import android.content.SharedPreferences

class SettingsHelper(context: Context) {
    private val sharedPreferences: SharedPreferences
    val preferences: Preferences

    init {
        sharedPreferences = context.getSharedPreferences("preferences", Context.MODE_PRIVATE)
        preferences = loadPreferences()
    }

    fun savePreferences() {
        val editor = sharedPreferences.edit()
        editor.putString("uncoveredPics", preferences.uncoveredPics.joinToString(","))
        editor.apply()
    }

    private fun loadPreferences(): Preferences {
        val preferences = Preferences(mutableListOf())
        preferences.uncoveredPics =
            sharedPreferences.getString("uncoveredPics", "")?.split(",")?.toMutableList()
                ?: mutableListOf()
        preferences.uncoveredPics = preferences.uncoveredPics.filter { it != "" }.toMutableList()
        return preferences
    }
}

data class Preferences(
    var uncoveredPics: MutableList<String>,
)