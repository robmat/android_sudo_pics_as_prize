package com.batodev.sudoku.ui.settings

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.core.os.LocaleListCompat
import com.batodev.sudoku.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

@Composable
private fun SettingsMistakesDialog(viewModel: SettingsViewModel, highlightMistakes: Int) {
    SelectionDialog(
        title = stringResource(R.string.pref_mistakes_check),
        selections = listOf(
            stringResource(R.string.pref_mistakes_check_off),
            stringResource(R.string.pref_mistakes_check_violations),
            stringResource(R.string.pref_mistakes_check_final)
        ),
        selected = highlightMistakes,
        onSelect = { index -> viewModel.updateMistakesHighlight(index) },
        onDismiss = { viewModel.mistakesDialog = false }
    )
}

@Composable
private fun SettingsDarkModeDialog(viewModel: SettingsViewModel, darkTheme: Int) {
    SelectionDialog(
        title = stringResource(R.string.pref_dark_theme),
        selections = listOf(
            stringResource(R.string.pref_dark_theme_follow),
            stringResource(R.string.pref_dark_theme_off),
            stringResource(R.string.pref_dark_theme_on)
        ),
        selected = darkTheme,
        onSelect = { index -> viewModel.updateDarkTheme(index) },
        onDismiss = { viewModel.darkModeDialog = false }
    )
}

@Composable
private fun SettingsFontSizeDialog(viewModel: SettingsViewModel, fontSize: Int) {
    SelectionDialog(
        title = stringResource(R.string.pref_board_font_size),
        selections = listOf(
            stringResource(R.string.pref_board_font_size_small),
            stringResource(R.string.pref_board_font_size_medium),
            stringResource(R.string.pref_board_font_size_large)
        ),
        selected = fontSize,
        onSelect = { index -> viewModel.updateFontSize(index) },
        onDismiss = { viewModel.fontSizeDialog = false }
    )
}

@Composable
private fun SettingsInputMethodDialog(viewModel: SettingsViewModel, inputMethod: Int) {
    SelectionDialog(
        title = stringResource(R.string.pref_input),
        selections = listOf(
            stringResource(R.string.pref_input_cell_first),
            stringResource(R.string.pref_input_digit_first)
        ),
        selected = inputMethod,
        onSelect = { index -> viewModel.updateInputMethod(index) },
        onDismiss = { viewModel.inputMethodDialog = false }
    )
}

@Composable
private fun SettingsResetStatsDialog(
    viewModel: SettingsViewModel,
    scope: CoroutineScope,
    snackbarHostState: SnackbarHostState,
    context: Context
) {
    AlertDialog(
        title = { Text(stringResource(R.string.pref_delete_stats)) },
        text = { Text(stringResource(R.string.pref_delete_stats_summ)) },
        confirmButton = {
            TextButton(onClick = {
                viewModel.deleteAllTables()
                viewModel.resetStatsDialog = false
                scope.launch {
                    snackbarHostState.showSnackbar(
                        context.resources.getString(R.string.action_deleted)
                    )
                }
            }) {
                Text(
                    text = stringResource(R.string.action_delete),
                    color = MaterialTheme.colorScheme.error
                )
            }
        },
        dismissButton = {
            FilledTonalButton(onClick = { viewModel.resetStatsDialog = false }) {
                Text(stringResource(R.string.action_cancel))
            }
        },
        onDismissRequest = { viewModel.resetStatsDialog = false }
    )
}

@Composable
private fun SettingsLanguageDialog(viewModel: SettingsViewModel, context: Context) {
    SelectionDialog(
        title = stringResource(R.string.pref_app_language),
        entries = getLangs(context),
        selected = getCurrentLocaleTag(),
        onSelect = { localeKey ->
            val locale = if (localeKey == "") {
                LocaleListCompat.getEmptyLocaleList()
            } else {
                LocaleListCompat.forLanguageTags(localeKey)
            }
            AppCompatDelegate.setApplicationLocales(locale)
            viewModel.languagePickDialog = false
        },
        onDismiss = { viewModel.languagePickDialog = false }
    )
}

@Composable
private fun SettingsDateFormatDialogSection(viewModel: SettingsViewModel, dateFormat: String) {
    DateFormatDialog(
        info = DateFormatDialogInfo(
            title = stringResource(R.string.pref_date_format),
            entries = buildDateFormatEntries(),
            customDateFormatText = buildCustomDateFormatText(dateFormat),
            selected = dateFormat
        ),
        onSelect = { format ->
            if (format == "custom") {
                viewModel.customFormatDialog = true
            } else {
                viewModel.updateDateFormat(format)
            }
            viewModel.dateFormatDialog = false
        },
        onDismiss = { viewModel.dateFormatDialog = false }
    )
}

@Composable
private fun SettingsCustomFormatDialogSection(viewModel: SettingsViewModel, dateFormat: String) {
    var customDateFormat by rememberSaveable {
        mutableStateOf(
            if (DateFormats.contains(dateFormat)) "" else dateFormat
        )
    }
    var invalidCustomDateFormat by rememberSaveable { mutableStateOf(false) }
    var dateFormatPreview by rememberSaveable { mutableStateOf("") }

    SetDateFormatPatternDialog(
        state = CustomDateFormatState(
            customDateFormat = customDateFormat,
            invalidCustomDateFormat = invalidCustomDateFormat,
            datePreview = dateFormatPreview
        ),
        callbacks = CustomDateFormatCallbacks(
            onConfirm = {
                if (viewModel.checkCustomDateFormat(customDateFormat)) {
                    viewModel.updateDateFormat(customDateFormat)
                    invalidCustomDateFormat = false
                    viewModel.customFormatDialog = false
                } else {
                    invalidCustomDateFormat = true
                }
            },
            onDismissRequest = { viewModel.customFormatDialog = false },
            onTextValueChange = { text ->
                customDateFormat = text
                if (invalidCustomDateFormat) invalidCustomDateFormat = false

                dateFormatPreview = if (viewModel.checkCustomDateFormat(customDateFormat)) {
                    ZonedDateTime.now().format(DateTimeFormatter.ofPattern(customDateFormat))
                } else {
                    ""
                }
            }
        )
    )
}

@Composable
internal fun SettingsDialogs(
    viewModel: SettingsViewModel,
    state: SettingsPreferencesState,
    scope: CoroutineScope,
    snackbarHostState: SnackbarHostState,
    context: Context
) {
    when {
        viewModel.mistakesDialog ->
            SettingsMistakesDialog(viewModel, state.assistance.highlightMistakes)

        viewModel.darkModeDialog ->
            SettingsDarkModeDialog(viewModel, state.appearance.darkTheme)

        viewModel.fontSizeDialog ->
            SettingsFontSizeDialog(viewModel, state.appearance.fontSize)

        viewModel.inputMethodDialog ->
            SettingsInputMethodDialog(viewModel, state.gameplay.inputMethod)

        viewModel.resetStatsDialog ->
            SettingsResetStatsDialog(viewModel, scope, snackbarHostState, context)

        viewModel.languagePickDialog ->
            SettingsLanguageDialog(viewModel, context)

        viewModel.dateFormatDialog ->
            SettingsDateFormatDialogSection(viewModel, state.appearance.dateFormat)
    }

    if (viewModel.customFormatDialog) {
        SettingsCustomFormatDialogSection(viewModel, state.appearance.dateFormat)
    }
}
