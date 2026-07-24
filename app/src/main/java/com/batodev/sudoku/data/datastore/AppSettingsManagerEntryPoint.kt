package com.batodev.sudoku.data.datastore

import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * Test-only access point for AppSettingsManager's live Hilt singleton (reachable from
 * instrumentation tests via EntryPointAccessors.fromApplication). Has to live in the main
 * source set, not androidTest - Hilt aggregates @InstallIn entry points per module at
 * compile time, and the app-under-test's actual runtime component is generated from
 * app/src/main alone, so an androidTest-only entry point never becomes part of it.
 */
@EntryPoint
@InstallIn(SingletonComponent::class)
interface AppSettingsManagerEntryPoint {
    fun appSettingsManager(): AppSettingsManager
}
