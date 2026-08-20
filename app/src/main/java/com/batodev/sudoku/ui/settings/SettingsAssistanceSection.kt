package com.batodev.sudoku.ui.settings

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.batodev.sudoku.R
import com.batodev.sudoku.ui.components.PreferenceRow
import com.batodev.sudoku.ui.components.PreferenceRowInfo
import com.batodev.sudoku.ui.components.PreferenceRowInteractions
import com.batodev.sudoku.ui.components.PreferenceRowSwitch

@Composable
private fun AssistanceMistakesCheckRow(
    highlightMistakes: Int,
    onClick: () -> Unit,
) {
    PreferenceRow(
        info =
            PreferenceRowInfo(
                title = stringResource(R.string.pref_mistakes_check),
                subtitle =
                    when (highlightMistakes) {
                        0 -> stringResource(R.string.pref_mistakes_check_off)
                        1 -> stringResource(R.string.pref_mistakes_check_violations)
                        2 -> stringResource(R.string.pref_mistakes_check_final)
                        else -> stringResource(R.string.pref_mistakes_check_off)
                    },
            ),
        interactions = PreferenceRowInteractions(onClick = onClick),
    )
}

@Composable
private fun AssistanceHighlightIdenticalSwitch(
    highlightIdentical: Boolean,
    onClick: () -> Unit,
) {
    PreferenceRowSwitch(
        info =
            PreferenceRowInfo(
                title = stringResource(R.string.pref_highlight_identical),
                subtitle = stringResource(R.string.pref_highlight_identical_summ),
            ),
        checked = highlightIdentical,
        onClick = onClick,
    )
}

@Composable
private fun AssistanceRemainingUsesSwitch(
    remainingUse: Boolean,
    onClick: () -> Unit,
) {
    PreferenceRowSwitch(
        info =
            PreferenceRowInfo(
                title = stringResource(R.string.pref_remaining_uses),
                subtitle = stringResource(R.string.pref_remaining_uses_summ),
            ),
        checked = remainingUse,
        onClick = onClick,
    )
}

@Composable
private fun AssistanceAutoEraseNotesSwitch(
    autoEraseNotes: Boolean,
    onClick: () -> Unit,
) {
    PreferenceRowSwitch(
        info = PreferenceRowInfo(title = stringResource(R.string.pref_auto_erase_notes)),
        checked = autoEraseNotes,
        onClick = onClick,
    )
}

internal fun LazyListScope.settingsAssistanceItems(
    state: SettingsPreferencesState,
    viewModel: SettingsViewModel,
) {
    item {
        HorizontalDivider(modifier = Modifier.fillMaxWidth())
        SettingsCategory(title = stringResource(R.string.pref_assistance))
        AssistanceMistakesCheckRow(state.assistance.highlightMistakes) { viewModel.mistakesDialog = true }
    }
    item {
        AssistanceHighlightIdenticalSwitch(state.assistance.highlightIdentical) {
            viewModel.updateHighlightIdentical(!state.assistance.highlightIdentical)
        }
    }
    item {
        AssistanceRemainingUsesSwitch(state.assistance.remainingUse) {
            viewModel.updateRemainingUse(!state.assistance.remainingUse)
        }
    }
    item {
        AssistanceAutoEraseNotesSwitch(state.assistance.autoEraseNotes) {
            viewModel.updateAutoEraseNotes(!state.assistance.autoEraseNotes)
        }
    }
}
