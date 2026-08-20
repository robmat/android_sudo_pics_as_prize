package com.batodev.sudoku.ui.settings

import android.content.Context
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.batodev.sudoku.R
import com.batodev.sudoku.data.datastore.AppSettingsManager
import com.batodev.sudoku.ui.components.PreferenceRow
import com.batodev.sudoku.ui.components.PreferenceRowInfo
import com.batodev.sudoku.ui.components.PreferenceRowInteractions
import com.batodev.sudoku.ui.components.PreferenceRowSwitch
import com.batodev.sudoku.ui.settings.components.AppThemePreviewInfo
import com.batodev.sudoku.ui.settings.components.AppThemePreviewItem
import com.batodev.sudoku.ui.theme.AppColorScheme
import com.batodev.sudoku.ui.theme.AppTheme
import com.batodev.sudoku.ui.theme.SudokuTheme
import com.batodev.sudoku.ui.theme.resolveAppTheme
import java.time.ZonedDateTime

@Composable
private fun AppearanceDarkThemeRow(darkTheme: Int, onClick: () -> Unit) {
    PreferenceRow(
        info = PreferenceRowInfo(
            title = stringResource(R.string.pref_dark_theme),
            subtitle = when (darkTheme) {
                0 -> stringResource(R.string.pref_dark_theme_follow)
                1 -> stringResource(R.string.pref_dark_theme_off)
                2 -> stringResource(R.string.pref_dark_theme_on)
                else -> ""
            }
        ),
        interactions = PreferenceRowInteractions(onClick = onClick)
    )
}

@Composable
private fun DynamicColorThemeOption(
    darkTheme: Int,
    dynamicColors: Boolean,
    amoledBlackState: Boolean,
    onClick: () -> Unit
) {
    SudokuTheme(
        dynamicColor = true,
        darkTheme = when (darkTheme) {
            0 -> isSystemInDarkTheme()
            1 -> false
            else -> true
        },
        amoled = amoledBlackState
    ) {
        Column(
            modifier = Modifier
                .width(115.dp)
                .padding(start = 8.dp, end = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AppThemePreviewItem(
                info = AppThemePreviewInfo(
                    selected = dynamicColors,
                    colorScheme = MaterialTheme.colorScheme,
                    shapes = MaterialTheme.shapes
                ),
                onClick = onClick
            )
            Text(
                text = stringResource(R.string.theme_dynamic),
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}

private fun appThemeTitleRes(theme: AppTheme): Int = when (theme) {
    AppTheme.Green -> R.string.theme_green
    AppTheme.Blue -> R.string.theme_blue
    AppTheme.Peach -> R.string.theme_peach
    AppTheme.Yellow -> R.string.theme_yellow
    AppTheme.Lavender -> R.string.theme_lavender
    AppTheme.BlackAndWhite -> R.string.theme_black_and_white
}

@Composable
private fun AppThemeSelectorRow(
    darkTheme: Int,
    dynamicColors: Boolean,
    currentThemeValue: AppTheme,
    amoledBlackState: Boolean,
    viewModel: SettingsViewModel
) {
    androidx.compose.material3.Text(
        modifier = Modifier.padding(horizontal = 16.dp),
        text = stringResource(R.string.pref_app_theme)
    )
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp, bottom = 8.dp)
    ) {
        val appTheme = AppColorScheme()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            item {
                DynamicColorThemeOption(darkTheme, dynamicColors, amoledBlackState) {
                    viewModel.updateDynamicColors(true)
                }
            }
        }
        items(enumValues<AppTheme>()) { theme ->
            AppThemeItem(
                info = AppThemeItemInfo(
                    title = stringResource(appThemeTitleRes(theme)),
                    colorScheme = appTheme.getTheme(
                        theme,
                        when (darkTheme) {
                            0 -> isSystemInDarkTheme()
                            1 -> false
                            else -> true
                        }
                    ),
                    selected = currentThemeValue == theme && !dynamicColors,
                    amoledBlack = amoledBlackState,
                    darkTheme = darkTheme
                ),
                onClick = {
                    viewModel.updateDynamicColors(false)
                    viewModel.updateCurrentTheme(theme)
                }
            )
        }
    }
}

@Composable
private fun AppearanceAmoledSwitch(amoledBlackState: Boolean, onClick: () -> Unit) {
    PreferenceRowSwitch(
        info = PreferenceRowInfo(title = stringResource(R.string.pref_pure_black)),
        checked = amoledBlackState,
        onClick = onClick
    )
}

@Composable
private fun AppearanceBoardThemeRow(navigateBoardSettings: () -> Unit) {
    PreferenceRow(
        info = PreferenceRowInfo(
            title = stringResource(R.string.pref_board_theme_title),
            subtitle = stringResource(R.string.pref_board_theme_subtitle)
        ),
        interactions = PreferenceRowInteractions(onClick = navigateBoardSettings)
    )
}

@Composable
private fun AppearanceFontSizeRow(fontSize: Int, onClick: () -> Unit) {
    PreferenceRow(
        info = PreferenceRowInfo(
            title = stringResource(R.string.pref_board_font_size),
            subtitle = when (fontSize) {
                0 -> stringResource(R.string.pref_board_font_size_small)
                1 -> stringResource(R.string.pref_board_font_size_medium)
                2 -> stringResource(R.string.pref_board_font_size_large)
                else -> ""
            }
        ),
        interactions = PreferenceRowInteractions(onClick = onClick)
    )
}

@Composable
private fun AppearanceLanguageRow(context: Context, viewModel: SettingsViewModel) {
    var currentLanguage by remember {
        mutableStateOf(getCurrentLocaleString(context))
    }
    LaunchedEffect(viewModel.languagePickDialog) {
        currentLanguage = getCurrentLocaleString(context)
    }
    PreferenceRow(
        info = PreferenceRowInfo(
            title = stringResource(R.string.pref_app_language),
            subtitle = currentLanguage
        ),
        interactions = PreferenceRowInteractions(onClick = { viewModel.languagePickDialog = true })
    )
}

@Composable
private fun AppearanceDateFormatRow(dateFormat: String, onClick: () -> Unit) {
    PreferenceRow(
        info = PreferenceRowInfo(
            title = stringResource(R.string.pref_date_format),
            subtitle = "${dateFormat.ifEmpty { stringResource(R.string.label_default) }} (${
                ZonedDateTime.now().format(AppSettingsManager.dateFormat(dateFormat))
            })"
        ),
        interactions = PreferenceRowInteractions(onClick = onClick)
    )
}

internal fun LazyListScope.settingsAppearanceItems(
    state: SettingsPreferencesState,
    viewModel: SettingsViewModel,
    navigateBoardSettings: () -> Unit,
    context: Context
) {
    item {
        SettingsCategory(title = stringResource(R.string.pref_appearance))
        AppearanceDarkThemeRow(state.appearance.darkTheme) { viewModel.darkModeDialog = true }
    }
    item {
        AppThemeSelectorRow(
            darkTheme = state.appearance.darkTheme,
            dynamicColors = state.appearance.dynamicColors,
            currentThemeValue = resolveAppTheme(state.appearance.currentTheme),
            amoledBlackState = state.appearance.amoledBlack,
            viewModel = viewModel
        )
    }
    item {
        AppearanceAmoledSwitch(state.appearance.amoledBlack) {
            viewModel.updateAmoledBlack(!state.appearance.amoledBlack)
        }
    }
    item { AppearanceBoardThemeRow(navigateBoardSettings) }
    item { AppearanceFontSizeRow(state.appearance.fontSize) { viewModel.fontSizeDialog = true } }
    item { AppearanceLanguageRow(context, viewModel) }
    item { AppearanceDateFormatRow(state.appearance.dateFormat) { viewModel.dateFormatDialog = true } }
}
