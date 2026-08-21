package com.batodev.sudoku.ui.game.components

import android.view.HapticFeedbackConstants
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.batodev.sudoku.core.qqwing.GameType
import com.batodev.sudoku.ui.theme.SudokuTheme
import com.batodev.sudoku.ui.util.PreviewLightDark

private const val KEYBOARD_NUMBER_FONT_SIZE_SP = 25
private const val KEYBOARD_NUMBER_FONT_SIZE_NO_USES_SP = 36
private const val KEYBOARD_REMAINING_USES_FONT_SIZE_SP = 11
private const val KEYBOARD_12X12_ROW_SIZE = 6
private const val SELECTED_ITEM_ALPHA = 0.5f
private const val PREVIEW_REMAINING_USES = 5
private const val HEX_RADIX = 16

/** The tap/long-press handlers shared by [KeyboardItem], [DefaultGameKeyboard], and
 * [NumbersKeyboardRow], both taking the pressed digit. */
data class KeyboardClickHandlers(
    val onClick: (Int) -> Unit,
    val onLongClick: (Int) -> Unit = { },
)

/** The number-dependent state shared by [DefaultGameKeyboard] and [NumbersKeyboardRow]: how many
 * uses remain for each digit, which digit is [selected], and the tap [handlers]. */
data class KeyboardState(
    val remainingUses: List<Int>? = null,
    val selected: Int = 0,
    val handlers: KeyboardClickHandlers,
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun KeyboardItem(
    number: Int,
    handlers: KeyboardClickHandlers,
    modifier: Modifier = Modifier,
    remainingUses: Int? = null,
    selected: Boolean = false,
) {
    val mutableInteractionSource by remember { mutableStateOf(MutableInteractionSource()) }
    val color by animateColorAsState(
        targetValue =
            if (selected) {
                MaterialTheme.colorScheme.primary.copy(alpha = SELECTED_ITEM_ALPHA)
            } else {
                Color.Transparent
            },
    )
    val localView = LocalView.current
    val keyboardFontSize =
        if (remainingUses != null) {
            KEYBOARD_NUMBER_FONT_SIZE_SP.sp
        } else {
            KEYBOARD_NUMBER_FONT_SIZE_NO_USES_SP.sp
        }
    Box(
        modifier =
            modifier
                .clip(CircleShape)
                .background(color)
                .combinedClickable(
                    interactionSource = mutableInteractionSource,
                    onClick = {
                        handlers.onClick(number)
                    },
                    onLongClick = {
                        localView.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                        handlers.onLongClick(number)
                    },
                    indication =
                        ripple(
                            bounded = true,
                            color = MaterialTheme.colorScheme.primary,
                        ),
                ),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier.padding(7.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top,
        ) {
            Text(
                text = number.toString(HEX_RADIX).uppercase(),
                fontWeight = FontWeight.Bold,
                fontSize = keyboardFontSize,
            )
            if (remainingUses != null) {
                Text(
                    text = remainingUses.toString(),
                    fontSize = KEYBOARD_REMAINING_USES_FONT_SIZE_SP.sp,
                )
            }
        }
    }
}

/**
 * Builds the `onClick`/`onLongClick` handler pair for [DefaultGameKeyboard] that forward a
 * pressed digit to [processInputKeyboard] (shared shape of `CreateSudokuViewModel` and
 * `GameViewModel`'s `processInputKeyboard(number, longTap)` method).
 */
fun keyboardClickHandlers(processInputKeyboard: (number: Int, longTap: Boolean) -> Unit): KeyboardClickHandlers =
    KeyboardClickHandlers(
        onClick = { number -> processInputKeyboard(number, false) },
        onLongClick = { number -> processInputKeyboard(number, true) },
    )

@Composable
fun DefaultGameKeyboard(
    size: Int,
    state: KeyboardState,
    modifier: Modifier = Modifier,
    itemModifier: Modifier = Modifier,
) {
    val numbers by remember(size) { mutableStateOf((1..size).toList()) }
    val remainingUses = state.remainingUses

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        if (size == GameType.Default12x12.size) {
            // double-height keyboard only for 12x12
            val chunkedNumbers = numbers.chunked(KEYBOARD_12X12_ROW_SIZE)
            if (chunkedNumbers.size == 2) {
                chunkedNumbers.forEachIndexed { index, chunked ->
                    AnimatedVisibility(
                        visible =
                            (
                                remainingUses != null &&
                                    remainingUses.chunked(KEYBOARD_12X12_ROW_SIZE)[index].any { it > 0 }
                            ) ||
                                remainingUses == null,
                    ) {
                        NumbersKeyboardRow(
                            numbers = chunked,
                            state = state,
                            itemModifier = itemModifier,
                        )
                    }
                }
            }
        } else {
            NumbersKeyboardRow(
                numbers = numbers,
                state = state,
                itemModifier = itemModifier,
            )
        }
    }
}

/** Renders a single [KeyboardRow] of [KeyboardItem]s for the given [numbers]. */
@Composable
private fun NumbersKeyboardRow(
    numbers: List<Int>,
    state: KeyboardState,
    modifier: Modifier = Modifier,
    itemModifier: Modifier = Modifier,
) {
    val remainingUses = state.remainingUses
    KeyboardRow(modifier = modifier) {
        numbers.forEach { number ->
            val hide =
                remainingUses != null && (remainingUses.size > number && remainingUses[number - 1] <= 0)
            KeyboardItem(
                modifier =
                    itemModifier
                        .weight(1f)
                        .alpha(if (hide) 0f else 1f)
                        .testTag("keyboard_$number"),
                number = number,
                handlers =
                    KeyboardClickHandlers(
                        onClick = {
                            if (!hide) {
                                state.handlers.onClick(number)
                            }
                        },
                        onLongClick = {
                            if (!hide) {
                                state.handlers.onLongClick(number)
                            }
                        },
                    ),
                remainingUses =
                    if (remainingUses != null && remainingUses.size >= number) {
                        remainingUses[number - 1]
                    } else {
                        null
                    },
                selected = number == state.selected,
            )
        }
    }
}

@Composable
private fun KeyboardRow(
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .then(modifier),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.CenterVertically,
        content = content,
    )
}

@PreviewLightDark
@Composable
private fun KeyboardItemPreview() {
    SudokuTheme {
        Surface {
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                KeyboardItem(
                    number = 1,
                    handlers = KeyboardClickHandlers(onClick = { }),
                )
                KeyboardItem(
                    number = 1,
                    selected = true,
                    handlers = KeyboardClickHandlers(onClick = { }),
                )
                KeyboardItem(
                    number = 1,
                    remainingUses = PREVIEW_REMAINING_USES,
                    handlers = KeyboardClickHandlers(onClick = { }),
                )
                KeyboardItem(
                    number = 1,
                    remainingUses = PREVIEW_REMAINING_USES,
                    selected = true,
                    handlers = KeyboardClickHandlers(onClick = { }),
                )
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun KeyboardPreview9x9() {
    SudokuTheme {
        Surface {
            DefaultGameKeyboard(
                size = GameType.Default9x9.size,
                state =
                    KeyboardState(
                        remainingUses = (1..GameType.Default9x9.size).toList(),
                        handlers = KeyboardClickHandlers(onClick = { }, onLongClick = { }),
                    ),
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun KeyboardPreview12x12() {
    SudokuTheme {
        Surface {
            DefaultGameKeyboard(
                size = GameType.Default12x12.size,
                state =
                    KeyboardState(
                        remainingUses = (1..GameType.Default12x12.size).toList(),
                        handlers = KeyboardClickHandlers(onClick = { }, onLongClick = { }),
                    ),
            )
        }
    }
}
