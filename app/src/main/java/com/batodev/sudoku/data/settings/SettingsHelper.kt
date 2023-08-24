package com.batodev.sudoku.data.settings

import android.app.Activity
import android.util.Log
import org.simpleframework.xml.core.Persister
import java.io.File

const val SETTINGS_FILE_NAME: String = "settings.xml"

object SettingsHelper {
    var settings: Settings = Settings()

    fun createIfNotExists(activity: Activity) {
        val savePath = savePath(activity)
        if (!File(savePath).exists()) save(activity)
    }

    fun save(activity: Activity) {
        val savePath = savePath(activity)
        Persister().write(settings, File(savePath))
    }

    fun load(activity: Activity) {
        val savePath = savePath(activity)
        if (File(savePath).exists()) {
            this.settings = Persister().read(Settings(), File(savePath))
            Log.d(SettingsHelper.javaClass.simpleName, File(savePath).readText())
        }
    }

    private fun savePath(activity: Activity) =
        activity.filesDir.absolutePath + File.separator + SETTINGS_FILE_NAME
}
