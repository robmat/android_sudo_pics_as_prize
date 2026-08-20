package com.batodev.sudoku

import android.app.Instrumentation
import android.content.Intent
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso
import androidx.test.espresso.NoActivityResumedException
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers.hasAction
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.batodev.sudoku.ui.gallery.GalleryActivity
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Covers the app shell: bottom nav (Home/More/Gallery), the "more games" external link,
 * More screen's fan-out to its five sub-screens, and exiting the app. Does not chase into
 * the sub-screens reachable only from deeper inside those (e.g. games history from
 * Statistics, saved games, folder contents, create/import sudoku) - this app has ~20
 * screens total, that's a separate, larger effort than this pass.
 */
@RunWith(AndroidJUnit4::class)
class HomeScreenTest {
    @get:Rule
    val composeTestRule = createEmptyComposeRule()

    @Before
    fun setUp() {
        Intents.init()
    }

    @After
    fun tearDown() {
        Intents.release()
    }

    @Test
    fun moreGamesButtonFiresExternalViewIntent() {
        Intents
            .intending(hasAction(Intent.ACTION_VIEW))
            .respondWith(Instrumentation.ActivityResult(0, null))
        ActivityScenario.launch(MainActivity::class.java).use {
            composeTestRule.completeOnboardingIfShown()
            composeTestRule.onNodeWithText("More games like this").performClick()
            Intents.intended(hasAction(Intent.ACTION_VIEW))
        }
    }

    @Test
    fun bottomNavGalleryLaunchesGalleryActivity() {
        Intents
            .intending(hasComponent(GalleryActivity::class.java.name))
            .respondWith(Instrumentation.ActivityResult(0, null))
        ActivityScenario.launch(MainActivity::class.java).use {
            composeTestRule.completeOnboardingIfShown()
            composeTestRule.onNodeWithText("Pictures gallery").performClick()
            Intents.intended(hasComponent(GalleryActivity::class.java.name))
        }
    }

    @Test
    fun bottomNavMoreShowsAllFiveEntries() {
        ActivityScenario.launch(MainActivity::class.java).use {
            composeTestRule.completeOnboardingIfShown()
            composeTestRule.onNodeWithText("More").performClick()
            composeTestRule.onNodeWithText("Statistics").assertExists()
            composeTestRule.onNodeWithText("Settings").assertExists()
            composeTestRule.onNodeWithText("Folders").assertExists()
            composeTestRule.onNodeWithText("Learn").assertExists()
            composeTestRule.onNodeWithText("About").assertExists()
        }
    }

    @Test
    fun moreScreenEachEntryNavigatesAndSystemBackReturns() {
        ActivityScenario.launch(MainActivity::class.java).use {
            composeTestRule.completeOnboardingIfShown()
            composeTestRule.onNodeWithText("More").performClick()

            listOf("Statistics", "Settings", "Folders", "Learn", "About").forEach { entry ->
                composeTestRule.onNodeWithText(entry).performClick()
                composeTestRule.waitForIdle()
                Espresso.pressBack()
                composeTestRule.waitForIdle()
                composeTestRule.onNodeWithText(entry).assertExists()
            }
        }
    }

    @Test
    fun backPressFromHomeExitsApp() {
        val scenario = ActivityScenario.launch(MainActivity::class.java)
        composeTestRule.completeOnboardingIfShown()
        try {
            Espresso.pressBack()
        } catch (expected: NoActivityResumedException) {
            // Correct signal here - MainActivity is the root of its own task.
        }
        assertEventuallyDestroyed(scenario)
        scenario.close()
    }
}
