package com.batodev.sudoku.ui.settings

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.batodev.sudoku.R
import com.batodev.sudoku.core.PreferencesConstants
import com.batodev.sudoku.ui.components.ScrollbarLazyColumn
import com.batodev.sudoku.ui.components.collapsingtopappbar.CollapsingTitle
import com.batodev.sudoku.ui.components.collapsingtopappbar.CollapsingTopAppBar
import com.batodev.sudoku.ui.components.collapsingtopappbar.CollapsingTopAppBarConfig
import com.batodev.sudoku.ui.components.collapsingtopappbar.CollapsingTopAppBarContent
import com.batodev.sudoku.ui.components.collapsingtopappbar.CollapsingTopAppBarScrollBehavior
import com.batodev.sudoku.ui.components.collapsingtopappbar.rememberTopAppBarScrollBehavior
import com.batodev.sudoku.ui.settings.components.AppThemePreviewInfo
import com.batodev.sudoku.ui.settings.components.AppThemePreviewItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsTopBar(
    navigateBack: () -> Unit,
    scrollBehavior: CollapsingTopAppBarScrollBehavior,
) {
    CollapsingTopAppBar(
        content =
            CollapsingTopAppBarContent(
                collapsingTitle = CollapsingTitle.medium(titleText = stringResource(R.string.settings_title)),
                navigationIcon = {
                    IconButton(onClick = navigateBack) {
                        Icon(
                            painter = painterResource(R.drawable.ic_round_arrow_back_24),
                            contentDescription = null,
                        )
                    }
                },
            ),
        config = CollapsingTopAppBarConfig(scrollBehavior = scrollBehavior),
    )
}

internal data class SettingsAppearancePrefs(
    val darkTheme: Int,
    val fontSize: Int,
    val dateFormat: String,
    val dynamicColors: Boolean,
    val amoledBlack: Boolean,
    val currentTheme: String,
)

@Composable
private fun rememberSettingsAppearancePrefs(viewModel: SettingsViewModel): SettingsAppearancePrefs {
    val darkTheme by viewModel.darkTheme.collectAsStateWithLifecycle(
        initialValue = PreferencesConstants.DEFAULT_DARK_THEME,
    )
    val fontSize by viewModel.fontSize.collectAsStateWithLifecycle(
        initialValue = PreferencesConstants.DEFAULT_FONT_SIZE_FACTOR,
    )
    val dateFormat by viewModel.dateFormat.collectAsStateWithLifecycle(initialValue = "")
    val dynamicColors by viewModel.dynamicColors.collectAsStateWithLifecycle(
        initialValue = PreferencesConstants.DEFAULT_DYNAMIC_COLORS,
    )
    val amoledBlackState by viewModel.amoledBlack.collectAsStateWithLifecycle(
        initialValue = PreferencesConstants.DEFAULT_AMOLED_BLACK,
    )
    val currentTheme by viewModel.currentTheme.collectAsStateWithLifecycle(
        initialValue = PreferencesConstants.DEFAULT_SELECTED_THEME,
    )
    return SettingsAppearancePrefs(darkTheme, fontSize, dateFormat, dynamicColors, amoledBlackState, currentTheme)
}

internal data class SettingsGameplayPrefs(
    val inputMethod: Int,
    val mistakesLimit: Boolean,
    val hintDisabled: Boolean,
    val timerEnabled: Boolean,
    val resetTimer: Boolean,
)

@Composable
private fun rememberSettingsGameplayPrefs(viewModel: SettingsViewModel): SettingsGameplayPrefs {
    val inputMethod by viewModel.inputMethod.collectAsStateWithLifecycle(
        initialValue = PreferencesConstants.DEFAULT_INPUT_METHOD,
    )
    val mistakesLimit by viewModel.mistakesLimit.collectAsStateWithLifecycle(
        initialValue = PreferencesConstants.DEFAULT_MISTAKES_LIMIT,
    )
    val hintDisabled by viewModel.disableHints.collectAsStateWithLifecycle(
        initialValue = PreferencesConstants.DEFAULT_HINTS_DISABLED,
    )
    val timerEnabled by viewModel.timer.collectAsStateWithLifecycle(
        initialValue = PreferencesConstants.DEFAULT_SHOW_TIMER,
    )
    val resetTimer by viewModel.canResetTimer.collectAsStateWithLifecycle(
        initialValue = PreferencesConstants.DEFAULT_GAME_RESET_TIMER,
    )
    return SettingsGameplayPrefs(inputMethod, mistakesLimit, hintDisabled, timerEnabled, resetTimer)
}

internal data class SettingsAssistancePrefs(
    val highlightMistakes: Int,
    val highlightIdentical: Boolean,
    val remainingUse: Boolean,
    val autoEraseNotes: Boolean,
)

@Composable
private fun rememberSettingsAssistancePrefs(viewModel: SettingsViewModel): SettingsAssistancePrefs {
    val highlightMistakes by viewModel.highlightMistakes.collectAsStateWithLifecycle(
        initialValue = PreferencesConstants.DEFAULT_HIGHLIGHT_MISTAKES,
    )
    val highlightIdentical by viewModel.highlightIdentical.collectAsStateWithLifecycle(
        initialValue = PreferencesConstants.DEFAULT_HIGHLIGHT_IDENTICAL,
    )
    val remainingUse by viewModel.remainingUse.collectAsStateWithLifecycle(
        initialValue = PreferencesConstants.DEFAULT_REMAINING_USES,
    )
    val autoEraseNotes by viewModel.autoEraseNotes.collectAsStateWithLifecycle(
        initialValue = PreferencesConstants.DEFAULT_AUTO_ERASE_NOTES,
    )
    return SettingsAssistancePrefs(highlightMistakes, highlightIdentical, remainingUse, autoEraseNotes)
}

@Composable
private fun rememberKeepScreenOnPref(viewModel: SettingsViewModel): Boolean {
    val keepScreenOn by viewModel.keepScreenOn.collectAsStateWithLifecycle(
        initialValue = PreferencesConstants.DEFAULT_KEEP_SCREEN_ON,
    )
    return keepScreenOn
}

internal data class SettingsPreferencesState(
    val appearance: SettingsAppearancePrefs,
    val gameplay: SettingsGameplayPrefs,
    val assistance: SettingsAssistancePrefs,
    val keepScreenOn: Boolean,
)

@Composable
private fun rememberSettingsPreferencesState(viewModel: SettingsViewModel): SettingsPreferencesState {
    val appearance = rememberSettingsAppearancePrefs(viewModel)
    val gameplay = rememberSettingsGameplayPrefs(viewModel)
    val assistance = rememberSettingsAssistancePrefs(viewModel)
    val keepScreenOn = rememberKeepScreenOnPref(viewModel)
    return SettingsPreferencesState(appearance, gameplay, assistance, keepScreenOn)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navigateBack: () -> Unit,
    viewModel: SettingsViewModel,
    navigateBoardSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val scrollBehavior = rememberTopAppBarScrollBehavior()
    val snackbarHostState = remember { SnackbarHostState() }
    Scaffold(
        modifier =
            modifier
                .nestedScroll(scrollBehavior.nestedScrollConnection),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = { SettingsTopBar(navigateBack, scrollBehavior) },
    ) { paddingValues ->
        val state = rememberSettingsPreferencesState(viewModel)

        ScrollbarLazyColumn(
            modifier =
                Modifier
                    .padding(paddingValues)
                    .fillMaxWidth(),
        ) {
            settingsAppearanceItems(state, viewModel, navigateBoardSettings, context)
            settingsGameplayItems(state, viewModel)
            settingsAssistanceItems(state, viewModel)
            settingsOtherItems(viewModel, state, scope, snackbarHostState, context)
        }

        SettingsDialogs(
            visibility =
                SettingsDialogsVisibility(
                    mistakesDialog = viewModel.mistakesDialog,
                    darkModeDialog = viewModel.darkModeDialog,
                    fontSizeDialog = viewModel.fontSizeDialog,
                    inputMethodDialog = viewModel.inputMethodDialog,
                    resetStatsDialog = viewModel.resetStatsDialog,
                    languagePickDialog = viewModel.languagePickDialog,
                    dateFormatDialog = viewModel.dateFormatDialog,
                    customFormatDialog = viewModel.customFormatDialog,
                ),
            actions =
                SettingsDialogsActions(
                    onSelectMistakesHighlight = {
                        viewModel.updateMistakesHighlight(it)
                        viewModel.mistakesDialog = false
                    },
                    onDismissMistakesDialog = { viewModel.mistakesDialog = false },
                    onSelectDarkTheme = {
                        viewModel.updateDarkTheme(it)
                        viewModel.darkModeDialog = false
                    },
                    onDismissDarkModeDialog = { viewModel.darkModeDialog = false },
                    onSelectFontSize = {
                        viewModel.updateFontSize(it)
                        viewModel.fontSizeDialog = false
                    },
                    onDismissFontSizeDialog = { viewModel.fontSizeDialog = false },
                    onSelectInputMethod = {
                        viewModel.updateInputMethod(it)
                        viewModel.inputMethodDialog = false
                    },
                    onDismissInputMethodDialog = { viewModel.inputMethodDialog = false },
                    onConfirmResetStats = {
                        viewModel.deleteAllTables()
                        viewModel.resetStatsDialog = false
                    },
                    onDismissResetStatsDialog = { viewModel.resetStatsDialog = false },
                    onDismissLanguagePickDialog = { viewModel.languagePickDialog = false },
                    onSelectDateFormat = { format ->
                        if (format == "custom") {
                            viewModel.customFormatDialog = true
                        } else {
                            viewModel.updateDateFormat(format)
                        }
                        viewModel.dateFormatDialog = false
                    },
                    onDismissDateFormatDialog = { viewModel.dateFormatDialog = false },
                    onCheckCustomDateFormat = viewModel::checkCustomDateFormat,
                    onConfirmCustomDateFormat = {
                        viewModel.updateDateFormat(it)
                        viewModel.customFormatDialog = false
                    },
                    onDismissCustomFormatDialog = { viewModel.customFormatDialog = false },
                ),
            state = state,
            scope = scope,
            snackbarHostState = snackbarHostState,
            context = context,
        )
    }
}

@Composable
fun SettingsCategory(
    title: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(start = 16.dp, bottom = 16.dp, top = 16.dp),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}

data class AppThemeItemInfo(
    val title: String,
    val colorScheme: ColorScheme,
    val amoledBlack: Boolean,
    val darkTheme: Int,
    val selected: Boolean,
)

@Composable
private fun isForcedBlackBackground(
    amoledBlack: Boolean,
    darkTheme: Int,
): Boolean {
    if (!amoledBlack) return false
    return darkTheme == 2 || (darkTheme == 0 && isSystemInDarkTheme())
}

@Composable
fun AppThemeItem(
    info: AppThemeItemInfo,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .width(115.dp)
                .padding(start = 8.dp, end = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        AppThemePreviewItem(
            info =
                AppThemePreviewInfo(
                    selected = info.selected,
                    colorScheme =
                        info.colorScheme.copy(
                            background =
                                if (isForcedBlackBackground(info.amoledBlack, info.darkTheme)) {
                                    Color.Black
                                } else {
                                    info.colorScheme.background
                                },
                        ),
                    shapes = MaterialTheme.shapes,
                ),
            onClick = onClick,
        )
        Text(
            text = info.title,
            style = MaterialTheme.typography.labelSmall,
        )
    }
}
