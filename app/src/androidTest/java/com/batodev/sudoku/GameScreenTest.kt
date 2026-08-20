package com.batodev.sudoku

import androidx.compose.ui.test.click
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * The board (Board.kt) is drawn entirely on a single Canvas - individual cells aren't
 * separate semantics nodes, so there's no way to target/assert a specific cell through
 * Compose's semantics tree. Board interaction here is necessarily coordinate-based
 * (tap the board's center) rather than cell-precise; the keyboard buttons are matched by
 * a "keyboard_<n>" testTag rather than their digit text - KeyboardItem merges its digit
 * and remaining-use-count Text children into one semantics node, and those two numbers
 * collide across different buttons often enough that text matching is ambiguous.
 */
@RunWith(AndroidJUnit4::class)
class GameScreenTest {
    @get:Rule
    val composeTestRule = createEmptyComposeRule()

    private fun launchIntoGame(): ActivityScenario<MainActivity> {
        val scenario = ActivityScenario.launch(MainActivity::class.java)
        composeTestRule.completeOnboardingIfShown()
        composeTestRule.clickPlayAndConfirmIfAsked()
        // A new board is generated (qqwing) before navigating here - can take a couple of
        // real seconds, so wait for the board itself rather than assuming it's instant.
        composeTestRule.waitUntil(timeoutMillis = 20_000) {
            composeTestRule.onAllNodesWithTag("sudoku_board").fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.dismissFirstGameDialogIfShown()
        return scenario
    }

    @Test
    fun tappingBoardSelectsACellAndKeyboardDigitAndUndoAreClickable() {
        launchIntoGame().use {
            composeTestRule.onNodeWithTag("sudoku_board").performTouchInput { click(center) }
            composeTestRule.onNodeWithTag("keyboard_5").performClick()
            composeTestRule.onNodeWithTag("game_undo").performClick()
            composeTestRule.onNodeWithTag("sudoku_board").assertExists()
        }
    }

    @Test
    fun restartButtonShowsConfirmationDialogAndCancelDismissesIt() {
        launchIntoGame().use {
            composeTestRule.onNodeWithTag("game_restart").performClick()
            composeTestRule.onNodeWithText("Reset the game").assertExists()
            composeTestRule.onNodeWithText("No").performClick()
            composeTestRule.onNodeWithText("Reset the game").assertDoesNotExist()
        }
    }

    @Test
    fun restartButtonConfirmResetsBoard() {
        launchIntoGame().use {
            composeTestRule.onNodeWithTag("game_restart").performClick()
            composeTestRule.onNodeWithText("Yes").performClick()
            composeTestRule.onNodeWithText("Reset the game").assertDoesNotExist()
            composeTestRule.onNodeWithTag("sudoku_board").assertExists()
        }
    }

    @Test
    fun backButtonReturnsToHome() {
        launchIntoGame().use {
            composeTestRule.onNodeWithTag("game_back").performClick()
            composeTestRule.onNodeWithText("Play").assertExists()
        }
    }
}
