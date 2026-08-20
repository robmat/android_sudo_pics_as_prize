package com.batodev.sudoku

import android.content.Context
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import com.batodev.sudoku.data.datastore.AppSettingsManagerEntryPoint
import com.batodev.sudoku.data.datastore.setFirstLaunch
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue

private const val ONBOARDING_START_TEXT = "Start"
private const val FIRST_GAME_DIALOG_TEXT = "Got it"
private const val START_NEW_GAME_TEXT = "Start new"

/**
 * AppSettingsManager's Preferences DataStore is an Application-scoped Hilt singleton that
 * stays alive across ActivityScenario launches within one instrumentation process, and
 * `preferencesDataStore`'s delegate actively refuses a second DataStore instance on the
 * same file ("There are multiple DataStores active for the same file") - so this can't
 * just construct a fresh AppSettingsManager, it has to reach the app's real live instance
 * via Hilt's EntryPoint mechanism and write through that.
 */
fun resetFirstLaunchFlag() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appSettingsManager = EntryPointAccessors
        .fromApplication(context, AppSettingsManagerEntryPoint::class.java)
        .appSettingsManager()
    runBlocking { appSettingsManager.setFirstLaunch(true) }
}

/**
 * The welcome screen (first app launch ever) and the first-game quick-tour dialog (first
 * game ever played) both only show once per install, driven by their own persisted flags.
 * Rather than reaching into DataStore for each one, click through them defensively if
 * present - idempotent, and realistic since it's exactly what a user does once.
 */
fun ComposeTestRule.completeOnboardingIfShown() {
    waitForIdle()
    if (onAllNodesWithText(ONBOARDING_START_TEXT).fetchSemanticsNodes().isNotEmpty()) {
        onNodeWithText(ONBOARDING_START_TEXT).performClick()
        waitUntil(timeoutMillis = 5000) {
            onAllNodesWithText(ONBOARDING_START_TEXT).fetchSemanticsNodes().isEmpty()
        }
    }
}

fun ComposeTestRule.dismissFirstGameDialogIfShown() {
    waitForIdle()
    if (onAllNodesWithText(FIRST_GAME_DIALOG_TEXT).fetchSemanticsNodes().isNotEmpty()) {
        onNodeWithText(FIRST_GAME_DIALOG_TEXT).performClick()
        waitUntil(timeoutMillis = 5000) {
            onAllNodesWithText(FIRST_GAME_DIALOG_TEXT).fetchSemanticsNodes().isEmpty()
        }
    }
}

/**
 * Once any earlier test has started a game, Home's "Play" button changes meaning: with an
 * in-progress saved game already in the DB (Room, shared across the whole instrumentation
 * process), Home shows a Continue/Play pair where "Play" only opens a
 * "start new game, discarding the current one" confirmation dialog instead of starting a
 * game directly. Handle both shapes rather than assuming which one is current.
 */
fun ComposeTestRule.clickPlayAndConfirmIfAsked() {
    onNodeWithText("Play").performClick()
    waitForIdle()
    if (onAllNodesWithText(START_NEW_GAME_TEXT).fetchSemanticsNodes().isNotEmpty()) {
        onNodeWithText(START_NEW_GAME_TEXT).performClick()
    }
}

fun assertEventuallyDestroyed(scenario: ActivityScenario<*>, timeoutMs: Long = 8000) {
    val start = System.currentTimeMillis()
    while (System.currentTimeMillis() - start < timeoutMs) {
        if (scenario.state == Lifecycle.State.DESTROYED) return
        Thread.sleep(50)
    }
    assertTrue(
        "Expected activity to reach DESTROYED within ${timeoutMs}ms, was ${scenario.state}",
        scenario.state == Lifecycle.State.DESTROYED
    )
}
