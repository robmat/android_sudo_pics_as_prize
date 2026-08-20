package com.batodev.sudoku.ui.settings

import android.content.Context
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.SnackbarHostState
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
private fun OtherSaveLastDiffTypeSwitch(viewModel: SettingsViewModel) {
    val saveLastSelectedDifficultyType by viewModel.saveLastSelectedDifficultyType
        .collectAsStateWithLifecycle(
            initialValue = PreferencesConstants.DEFAULT_SAVE_LAST_SELECTED_DIFF_TYPE
        )
    PreferenceRowSwitch(
        info = PreferenceRowInfo(
            title = stringResource(R.string.pref_save_last_diff_and_type),
            subtitle = stringResource(R.string.pref_save_last_diff_and_type_subtitle)
        ),
        checked = saveLastSelectedDifficultyType,
        onClick = {
            viewModel.updateSaveLastSelectedDifficultyType(!saveLastSelectedDifficultyType)
        }
    )
}

@Composable
private fun OtherKeepScreenOnSwitch(keepScreenOn: Boolean, onClick: () -> Unit) {
    PreferenceRowSwitch(
        info = PreferenceRowInfo(title = stringResource(R.string.pref_keep_screen_on)),
        checked = keepScreenOn,
        onClick = onClick
    )
}

@Composable
private fun OtherResetTipCardsRow(
    viewModel: SettingsViewModel,
    scope: CoroutineScope,
    snackbarHostState: SnackbarHostState,
    context: Context
) {
    PreferenceRow(
        info = PreferenceRowInfo(title = stringResource(R.string.pref_reset_tipcards)),
        interactions = PreferenceRowInteractions(
            onClick = {
                viewModel.resetTipCards()
                scope.launch {
                    snackbarHostState.showSnackbar(
                        context.resources.getString(R.string.pref_tipcards_reset)
                    )
                }
            }
        )
    )
}

@Composable
private fun OtherDeleteStatsRow(viewModel: SettingsViewModel) {
    if (viewModel.launchedFromGame == null || viewModel.launchedFromGame == false) {
        PreferenceRow(
            info = PreferenceRowInfo(title = stringResource(R.string.pref_delete_stats)),
            interactions = PreferenceRowInteractions(
                onClick = {
                    viewModel.resetStatsDialog = true
                }
            )
        )
    }
}

@Composable
private fun OtherCrashReportingSwitch(viewModel: SettingsViewModel) {
    PreferenceRowSwitch(
        info = PreferenceRowInfo(
            title = stringResource(R.string.pref_crash_reporting),
            subtitle = stringResource(R.string.pref_crash_reporting_subtitle)
        ),
        checked = viewModel.crashReportingEnabled,
        onClick = {
            viewModel.updateCrashReportingEnabled(!viewModel.crashReportingEnabled)
        }
    )
}

internal fun LazyListScope.settingsOtherItems(
    viewModel: SettingsViewModel,
    state: SettingsPreferencesState,
    scope: CoroutineScope,
    snackbarHostState: SnackbarHostState,
    context: Context
) {
    item {
        HorizontalDivider(modifier = Modifier.fillMaxWidth())
        SettingsCategory(title = stringResource(R.string.pref_other))
        OtherSaveLastDiffTypeSwitch(viewModel)
    }
    item {
        OtherKeepScreenOnSwitch(state.keepScreenOn) {
            viewModel.updateKeepScreenOn(!state.keepScreenOn)
        }
    }
    item { OtherResetTipCardsRow(viewModel, scope, snackbarHostState, context) }
    item { OtherDeleteStatsRow(viewModel) }
    item { OtherCrashReportingSwitch(viewModel) }
}
