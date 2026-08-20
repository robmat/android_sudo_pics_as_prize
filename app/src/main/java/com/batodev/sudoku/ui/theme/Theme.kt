package com.batodev.sudoku.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.batodev.sudoku.core.PreferencesConstants
import com.google.accompanist.systemuicontroller.rememberSystemUiController

/**
 * Resolves the persisted "dark theme" preference (0 = follow system, 1 = light, 2 = dark)
 * into the effective dark-theme flag, falling back to the system setting.
 */
@Composable
fun resolveDarkTheme(darkThemeSetting: Int): Boolean = when (darkThemeSetting) {
    1 -> false
    2 -> true
    else -> isSystemInDarkTheme()
}

/**
 * Resolves the persisted "selected theme" preference key into the corresponding [AppTheme],
 * defaulting to [AppTheme.Green] when unknown.
 */
fun resolveAppTheme(currentThemeKey: String): AppTheme = when (currentThemeKey) {
    PreferencesConstants.GREEN_THEME_KEY -> AppTheme.Green
    PreferencesConstants.BLUE_THEME_KEY -> AppTheme.Blue
    PreferencesConstants.PEACH_THEME_KEY -> AppTheme.Peach
    PreferencesConstants.YELLOW_THEME_KEY -> AppTheme.Yellow
    PreferencesConstants.LAVENDER_THEME_KEY -> AppTheme.Lavender
    PreferencesConstants.BLACK_AND_WHITE_THEME_KEY -> AppTheme.BlackAndWhite
    else -> AppTheme.Green
}

@Composable
fun SudokuTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    amoled: Boolean = false,
    appTheme: AppTheme = AppTheme.Green,
    content: @Composable () -> Unit,
) {
    val appColorScheme = AppColorScheme()
    val currentTheme = appColorScheme.getTheme(appTheme, darkTheme)
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current

            when {
                darkTheme && amoled -> dynamicDarkColorScheme(context).copy(
                    background = Color.Black,
                    surface = Color.Black
                )

                darkTheme && !amoled -> dynamicDarkColorScheme(context)
                else -> dynamicLightColorScheme(context)
            }
        }

        darkTheme && amoled -> currentTheme.copy(background = Color.Black, surface = Color.Black)
        darkTheme -> currentTheme
        else -> currentTheme
    }
    val systemUiController = rememberSystemUiController()

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = {
            SideEffect {
                systemUiController.setSystemBarsColor(
                    color = Color.Transparent,
                    darkIcons = !darkTheme
                )
            }

            content()
        }
    )
}
