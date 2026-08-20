package com.batodev.sudoku

import android.content.Context
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.batodev.sudoku.ui.components.animatedComposable
import com.batodev.sudoku.ui.createeditsudoku.CreateSudokuScreen
import com.batodev.sudoku.ui.gameshistory.GamesHistoryScreen
import com.batodev.sudoku.ui.home.HomeScreen
import com.batodev.sudoku.ui.learn.LearnScreen
import com.batodev.sudoku.ui.more.MoreScreen
import com.batodev.sudoku.ui.more.about.AboutLibrariesScreen
import com.batodev.sudoku.ui.more.about.AboutScreen
import com.batodev.sudoku.ui.onboarding.WelcomeScreen
import com.batodev.sudoku.ui.settings.SettingsScreen
import com.batodev.sudoku.ui.settings.boardtheme.SettingsBoardTheme
import com.batodev.sudoku.ui.statistics.StatisticsScreen
import com.batodev.sudoku.ui.util.Route

private fun NavGraphBuilder.homeDestination(navController: NavController) {
    animatedComposable(Route.HOME) {
        HomeScreen(
            navigatePlayGame = {
                navController.navigate("game/${it.first}/${it.second}")
            },
            hiltViewModel(),
        )
    }
}

private fun NavGraphBuilder.moreDestination(navController: NavController) {
    animatedComposable(Route.MORE) {
        MoreScreen(
            navigateSettings = { navController.navigate("settings/?fromGame=false") },
            navigateLearn = { navController.navigate(Route.LEARN) },
            navigateAbout = { navController.navigate(Route.ABOUT) },
            navigateImport = { navController.navigate(Route.FOLDERS) },
            navigateStatistics = { navController.navigate(Route.STATISTICS) },
        )
    }
}

private fun NavGraphBuilder.aboutDestinations(navController: NavController) {
    animatedComposable(Route.ABOUT) {
        AboutScreen(
            navigateBack = { navController.popBackStack() },
            navigateOpenSourceLicenses = { navController.navigate(Route.OPEN_SOURCE_LICENSES) },
        )
    }

    animatedComposable(Route.OPEN_SOURCE_LICENSES) {
        AboutLibrariesScreen { navController.popBackStack() }
    }
}

private fun NavGraphBuilder.welcomeDestination(navController: NavController) {
    animatedComposable(Route.WELCOME_SCREEN) {
        WelcomeScreen(
            navigateToGame = {
                navController.popBackStack()
                navController.navigate(Route.HOME)
            },
            hiltViewModel(),
        )
    }
}

private fun NavGraphBuilder.statisticsAndHistoryDestinations(navController: NavController) {
    animatedComposable(Route.STATISTICS) {
        StatisticsScreen(
            navigateHistory = { navController.navigate(Route.HISTORY) },
            navigateSavedGame = { navController.navigate("saved_game/$it") },
            hiltViewModel(),
        )
    }

    animatedComposable(Route.HISTORY) {
        GamesHistoryScreen(
            navigateBack = { navController.popBackStack() },
            navigateSavedGame = { uid ->
                navController.navigate(
                    "saved_game/$uid",
                )
            },
            hiltViewModel(),
        )
    }
}

private fun NavGraphBuilder.learnDestination(navController: NavController) {
    animatedComposable(Route.LEARN) {
        LearnScreen { navController.popBackStack() }
    }
}

private fun NavGraphBuilder.createEditSudokuDestination(navController: NavController) {
    animatedComposable(
        route = "createeditsudoku/{game_uid}/{folder_uid}",
        arguments =
            listOf(
                navArgument("game_uid") {
                    type = NavType.LongType
                }, // used for editing
                navArgument("folder_uid") {
                    type = NavType.LongType
                }, // folder where to save
            ),
    ) {
        CreateSudokuScreen(
            navigateBack = { navController.popBackStack() },
            hiltViewModel(),
        )
    }
}

private fun NavGraphBuilder.settingsDestinations(navController: NavController) {
    animatedComposable(
        route = Route.SETTINGS,
        arguments =
            listOf(
                navArgument("fromGame") {
                    defaultValue = false
                    type = NavType.BoolType
                },
            ),
    ) {
        SettingsScreen(
            navigateBack = { navController.popBackStack() },
            hiltViewModel(),
            navigateBoardSettings = { navController.navigate("settings_board_theme") },
        )
    }

    animatedComposable(Route.SETTINGS_BOARD_THEME) {
        SettingsBoardTheme(
            viewModel = hiltViewModel(),
            navigateBack = { navController.popBackStack() },
        )
    }
}

fun NavGraphBuilder.buildSudokuNavigationGraph(
    navController: NavController,
    context: Context,
) {
    homeDestination(navController)
    moreDestination(navController)
    aboutDestinations(navController)
    welcomeDestination(navController)
    statisticsAndHistoryDestinations(navController)
    learnDestination(navController)
    createEditSudokuDestination(navController)
    settingsDestinations(navController)
    gameDestination(navController)
    savedGameDestination(navController)
    foldersDestination(navController)
    importFromFileDestination(navController, context)
    exploreFolderDestination(navController)
    importDeepLinkDestination(navController, context)
}
