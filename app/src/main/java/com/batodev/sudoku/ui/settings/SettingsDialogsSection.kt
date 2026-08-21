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
private fun SettingsMistakesDialog(
    highlightMistakes: Int,
    onSelect: (Int) -> Unit,
    onDismiss: () -> Unit,
) {
    SelectionDialog(
        title = stringResource(R.string.pref_mistakes_check),
        selections =
            listOf(
                stringResource(R.string.pref_mistakes_check_off),
                stringResource(R.string.pref_mistakes_check_violations),
                stringResource(R.string.pref_mistakes_check_final),
            ),
        selected = highlightMistakes,
        onSelect = onSelect,
        onDismiss = onDismiss,
    )
}

@Composable
private fun SettingsDarkModeDialog(
    darkTheme: Int,
    onSelect: (Int) -> Unit,
    onDismiss: () -> Unit,
) {
    SelectionDialog(
        title = stringResource(R.string.pref_dark_theme),
        selections =
            listOf(
                stringResource(R.string.pref_dark_theme_follow),
                stringResource(R.string.pref_dark_theme_off),
                stringResource(R.string.pref_dark_theme_on),
            ),
        selected = darkTheme,
        onSelect = onSelect,
        onDismiss = onDismiss,
    )
}

@Composable
private fun SettingsFontSizeDialog(
    fontSize: Int,
    onSelect: (Int) -> Unit,
    onDismiss: () -> Unit,
) {
    SelectionDialog(
        title = stringResource(R.string.pref_board_font_size),
        selections =
            listOf(
                stringResource(R.string.pref_board_font_size_small),
                stringResource(R.string.pref_board_font_size_medium),
                stringResource(R.string.pref_board_font_size_large),
            ),
        selected = fontSize,
        onSelect = onSelect,
        onDismiss = onDismiss,
    )
}

@Composable
private fun SettingsInputMethodDialog(
    inputMethod: Int,
    onSelect: (Int) -> Unit,
    onDismiss: () -> Unit,
) {
    SelectionDialog(
        title = stringResource(R.string.pref_input),
        selections =
            listOf(
                stringResource(R.string.pref_input_cell_first),
                stringResource(R.string.pref_input_digit_first),
            ),
        selected = inputMethod,
        onSelect = onSelect,
        onDismiss = onDismiss,
    )
}

@Composable
private fun SettingsResetStatsDialog(
    scope: CoroutineScope,
    snackbarHostState: SnackbarHostState,
    context: Context,
    onConfirmDelete: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        title = { Text(stringResource(R.string.pref_delete_stats)) },
        text = { Text(stringResource(R.string.pref_delete_stats_summ)) },
        confirmButton = {
            TextButton(onClick = {
                onConfirmDelete()
                onDismiss()
                scope.launch {
                    snackbarHostState.showSnackbar(
                        context.resources.getString(R.string.action_deleted),
                    )
                }
            }) {
                Text(
                    text = stringResource(R.string.action_delete),
                    color = MaterialTheme.colorScheme.error,
                )
            }
        },
        dismissButton = {
            FilledTonalButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_cancel))
            }
        },
        onDismissRequest = onDismiss,
    )
}

@Composable
private fun SettingsLanguageDialog(
    context: Context,
    onDismiss: () -> Unit,
) {
    SelectionDialog(
        title = stringResource(R.string.pref_app_language),
        entries = getLangs(context),
        selected = getCurrentLocaleTag(),
        onSelect = { localeKey ->
            val locale =
                if (localeKey == "") {
                    LocaleListCompat.getEmptyLocaleList()
                } else {
                    LocaleListCompat.forLanguageTags(localeKey)
                }
            AppCompatDelegate.setApplicationLocales(locale)
            onDismiss()
        },
        onDismiss = onDismiss,
    )
}

@Composable
private fun SettingsDateFormatDialogSection(
    dateFormat: String,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    DateFormatDialog(
        info =
            DateFormatDialogInfo(
                title = stringResource(R.string.pref_date_format),
                entries = buildDateFormatEntries(),
                customDateFormatText = buildCustomDateFormatText(dateFormat),
                selected = dateFormat,
            ),
        onSelect = onSelect,
        onDismiss = onDismiss,
    )
}

@Composable
private fun SettingsCustomFormatDialogSection(
    dateFormat: String,
    onCheckCustomDateFormat: (String) -> Boolean,
    onConfirmCustomFormat: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var customDateFormat by rememberSaveable {
        mutableStateOf(
            if (DateFormats.contains(dateFormat)) "" else dateFormat,
        )
    }
    var invalidCustomDateFormat by rememberSaveable { mutableStateOf(false) }
    var dateFormatPreview by rememberSaveable { mutableStateOf("") }

    SetDateFormatPatternDialog(
        state =
            CustomDateFormatState(
                customDateFormat = customDateFormat,
                invalidCustomDateFormat = invalidCustomDateFormat,
                datePreview = dateFormatPreview,
            ),
        callbacks =
            CustomDateFormatCallbacks(
                onConfirm = {
                    if (onCheckCustomDateFormat(customDateFormat)) {
                        onConfirmCustomFormat(customDateFormat)
                        invalidCustomDateFormat = false
                    } else {
                        invalidCustomDateFormat = true
                    }
                },
                onDismissRequest = onDismiss,
                onTextValueChange = { text ->
                    customDateFormat = text
                    if (invalidCustomDateFormat) invalidCustomDateFormat = false

                    dateFormatPreview =
                        if (onCheckCustomDateFormat(customDateFormat)) {
                            ZonedDateTime.now().format(DateTimeFormatter.ofPattern(customDateFormat))
                        } else {
                            ""
                        }
                },
            ),
    )
}

/** Which of [SettingsDialogs]' dialogs is currently visible. */
internal data class SettingsDialogsVisibility(
    val mistakesDialog: Boolean,
    val darkModeDialog: Boolean,
    val fontSizeDialog: Boolean,
    val inputMethodDialog: Boolean,
    val resetStatsDialog: Boolean,
    val languagePickDialog: Boolean,
    val dateFormatDialog: Boolean,
    val customFormatDialog: Boolean,
)

/** The callbacks [SettingsDialogs] needs; constructed once by [SettingsScreen]. */
internal data class SettingsDialogsActions(
    val onSelectMistakesHighlight: (Int) -> Unit,
    val onDismissMistakesDialog: () -> Unit,
    val onSelectDarkTheme: (Int) -> Unit,
    val onDismissDarkModeDialog: () -> Unit,
    val onSelectFontSize: (Int) -> Unit,
    val onDismissFontSizeDialog: () -> Unit,
    val onSelectInputMethod: (Int) -> Unit,
    val onDismissInputMethodDialog: () -> Unit,
    val onConfirmResetStats: () -> Unit,
    val onDismissResetStatsDialog: () -> Unit,
    val onDismissLanguagePickDialog: () -> Unit,
    val onSelectDateFormat: (String) -> Unit,
    val onDismissDateFormatDialog: () -> Unit,
    val onCheckCustomDateFormat: (String) -> Boolean,
    val onConfirmCustomDateFormat: (String) -> Unit,
    val onDismissCustomFormatDialog: () -> Unit,
)

@Composable
internal fun SettingsDialogs(
    visibility: SettingsDialogsVisibility,
    actions: SettingsDialogsActions,
    state: SettingsPreferencesState,
    scope: CoroutineScope,
    snackbarHostState: SnackbarHostState,
    context: Context,
) {
    when {
        visibility.mistakesDialog -> {
            SettingsMistakesDialog(
                state.assistance.highlightMistakes,
                actions.onSelectMistakesHighlight,
                actions.onDismissMistakesDialog,
            )
        }

        visibility.darkModeDialog -> {
            SettingsDarkModeDialog(state.appearance.darkTheme, actions.onSelectDarkTheme, actions.onDismissDarkModeDialog)
        }

        visibility.fontSizeDialog -> {
            SettingsFontSizeDialog(state.appearance.fontSize, actions.onSelectFontSize, actions.onDismissFontSizeDialog)
        }

        visibility.inputMethodDialog -> {
            SettingsInputMethodDialog(
                state.gameplay.inputMethod,
                actions.onSelectInputMethod,
                actions.onDismissInputMethodDialog,
            )
        }

        visibility.resetStatsDialog -> {
            SettingsResetStatsDialog(
                scope,
                snackbarHostState,
                context,
                actions.onConfirmResetStats,
                actions.onDismissResetStatsDialog,
            )
        }

        visibility.languagePickDialog -> {
            SettingsLanguageDialog(context, actions.onDismissLanguagePickDialog)
        }

        visibility.dateFormatDialog -> {
            SettingsDateFormatDialogSection(
                state.appearance.dateFormat,
                actions.onSelectDateFormat,
                actions.onDismissDateFormatDialog,
            )
        }
    }

    if (visibility.customFormatDialog) {
        SettingsCustomFormatDialogSection(
            state.appearance.dateFormat,
            actions.onCheckCustomDateFormat,
            actions.onConfirmCustomDateFormat,
            actions.onDismissCustomFormatDialog,
        )
    }
}
