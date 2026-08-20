package com.batodev.sudoku.ui.components

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.runtime.Composable
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDeepLink
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable

private const val ENTER_DURATION_MS = 220
private const val ENTER_DELAY_MS = 90
private const val EXIT_DURATION_MS = 90
private const val ENTER_INITIAL_SCALE = 0.92f

fun NavGraphBuilder.animatedComposable(
    route: String,
    arguments: List<NamedNavArgument> = emptyList(),
    deepLinks: List<NavDeepLink> = emptyList(),
    content: @Composable AnimatedVisibilityScope.(NavBackStackEntry) -> Unit
) = composable(
    route = route,
    arguments = arguments,
    deepLinks = deepLinks,
    enterTransition = {
        fadeIn(animationSpec = tween(ENTER_DURATION_MS, delayMillis = ENTER_DELAY_MS)) +
            scaleIn(
                initialScale = ENTER_INITIAL_SCALE,
                animationSpec = tween(ENTER_DURATION_MS, delayMillis = ENTER_DELAY_MS)
            )
    },
    exitTransition = {
        fadeOut(animationSpec = tween(EXIT_DURATION_MS))
    },
    popEnterTransition = {
        fadeIn(animationSpec = tween(ENTER_DURATION_MS, delayMillis = ENTER_DELAY_MS)) +
            scaleIn(
                initialScale = ENTER_INITIAL_SCALE,
                animationSpec = tween(ENTER_DURATION_MS, delayMillis = ENTER_DELAY_MS)
            )
    },
    popExitTransition = {
        fadeOut(animationSpec = tween(EXIT_DURATION_MS))
    },
    content = content
)
