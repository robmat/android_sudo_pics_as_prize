package com.batodev.sudoku.data.datastore

import android.content.Context
import androidx.core.content.edit
import com.batodev.sudoku.di.ACRA_SHARED_PREFS_NAME
import dagger.hilt.android.qualifiers.ApplicationContext
import org.acra.ACRA
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AcraSharedPrefs
    @Inject
    constructor(
        @ApplicationContext context: Context,
    ) {
        private val acraEnabledKey = ACRA.PREF_ENABLE_ACRA

        private var prefs = context.getSharedPreferences(ACRA_SHARED_PREFS_NAME, Context.MODE_PRIVATE)

        fun getAcraEnabled(): Boolean = prefs.getBoolean(acraEnabledKey, true)

        fun setAcraEnabled(enabled: Boolean) {
            prefs.edit {
                putBoolean(acraEnabledKey, enabled)
            }
        }
    }
