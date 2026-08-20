package com.batodev.sudoku.ui.settings

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.batodev.sudoku.R
import com.batodev.sudoku.core.PreferencesConstants
import com.batodev.sudoku.ui.components.PreferenceRow
import com.batodev.sudoku.ui.components.PreferenceRowInfo
import com.batodev.sudoku.ui.components.PreferenceRowInteractions
import com.batodev.sudoku.ui.components.PreferenceRowSwitch

@Composable
private fun GameplayInputMethodRow(
    inputMethod: Int,
    onClick: () -> Unit,
) {
    PreferenceRow(
        info =
            PreferenceRowInfo(
                title = stringResource(R.string.pref_input),
                subtitle =
                    when (inputMethod) {
                        0 -> stringResource(R.string.pref_input_cell_first)
                        1 -> stringResource(R.string.pref_input_digit_first)
                        else -> ""
                    },
            ),
        interactions = PreferenceRowInteractions(onClick = onClick),
    )
}

@Composable
private fun GameplayMistakesLimitSwitch(
    mistakesLimit: Boolean,
    onClick: () -> Unit,
) {
    PreferenceRowSwitch(
        info =
            PreferenceRowInfo(
                title = stringResource(R.string.pref_mistakes_limit),
                subtitle = stringResource(R.string.pref_mistakes_limit_summ),
            ),
        checked = mistakesLimit,
        onClick = onClick,
    )
}

@Composable
private fun GameplayHintsDisabledSwitch(
    hintDisabled: Boolean,
    onClick: () -> Unit,
) {
    PreferenceRowSwitch(
        info =
            PreferenceRowInfo(
                title = stringResource(R.string.pref_disable_hints),
                subtitle = stringResource(R.string.pref_disable_hints_summ),
            ),
        checked = hintDisabled,
        onClick = onClick,
    )
}

@Composable
private fun GameplayShowTimerSwitch(
    timerEnabled: Boolean,
    onClick: () -> Unit,
) {
    PreferenceRowSwitch(
        info = PreferenceRowInfo(title = stringResource(R.string.pref_show_timer)),
        checked = timerEnabled,
        onClick = onClick,
    )
}

@Composable
private fun GameplayResetTimerSwitch(
    resetTimer: Boolean,
    onClick: () -> Unit,
) {
    PreferenceRowSwitch(
        info = PreferenceRowInfo(title = stringResource(R.string.pref_reset_timer)),
        checked = resetTimer,
        onClick = onClick,
    )
}

@Composable
private fun GameplayFunKeyboardSwitch(viewModel: SettingsViewModel) {
    val funKeyboardOverNum by viewModel.funKeyboardOverNum.collectAsStateWithLifecycle(
        initialValue = PreferencesConstants.DEFAULT_FUN_KEYBOARD_OVER_NUM,
    )
    PreferenceRowSwitch(
        info =
            PreferenceRowInfo(
                title = stringResource(R.string.pref_fun_keyboard_over_num),
                subtitle = stringResource(R.string.pref_fun_keyboard_over_num_subtitle),
            ),
        checked = funKeyboardOverNum,
        onClick = {
            viewModel.updateFunKeyboardOverNum(!funKeyboardOverNum)
        },
    )
}

internal fun LazyListScope.settingsGameplayItems(
    state: SettingsPreferencesState,
    viewModel: SettingsViewModel,
) {
    item {
        HorizontalDivider(modifier = Modifier.fillMaxWidth())
        SettingsCategory(title = stringResource(R.string.pref_gameplay))
        GameplayInputMethodRow(state.gameplay.inputMethod) { viewModel.inputMethodDialog = true }
    }
    item {
        GameplayMistakesLimitSwitch(state.gameplay.mistakesLimit) {
            viewModel.updateMistakesLimit(!state.gameplay.mistakesLimit)
        }
    }
    item {
        GameplayHintsDisabledSwitch(state.gameplay.hintDisabled) {
            viewModel.updateHintDisabled(!state.gameplay.hintDisabled)
        }
    }
    item {
        GameplayShowTimerSwitch(state.gameplay.timerEnabled) {
            viewModel.updateTimer(!state.gameplay.timerEnabled)
        }
    }
    item {
        GameplayResetTimerSwitch(state.gameplay.resetTimer) {
            viewModel.updateCanResetTimer(!state.gameplay.resetTimer)
        }
    }
    item { GameplayFunKeyboardSwitch(viewModel) }
}
