package com.batodev.sudoku

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.LaunchedEffect
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.batodev.sudoku.ui.components.animatedComposable
import com.batodev.sudoku.ui.explorefolder.ExploreFolderNavigation
import com.batodev.sudoku.ui.explorefolder.ExploreFolderScreen
import com.batodev.sudoku.ui.folders.FoldersScreen
import com.batodev.sudoku.ui.game.GameScreen
import com.batodev.sudoku.ui.gameshistory.savedgame.SavedGameScreen
import com.batodev.sudoku.ui.importfromfile.ImportFromFileScreen
import com.batodev.sudoku.ui.util.Route
import com.batodev.sudoku.ui.util.findActivity

internal fun NavGraphBuilder.gameDestination(navController: NavController) {
    animatedComposable(
        route = Route.GAME,
        arguments =
            listOf(
                navArgument(name = "uid") { type = NavType.LongType },
                navArgument(name = "saved") {
                    type = NavType.BoolType
                    defaultValue = false
                },
            ),
    ) {
        GameScreen(
            navigateBack = { navController.popBackStack() },
            navigateSettings = {
                navController.navigate("settings/?fromGame=true")
            },
            hiltViewModel(),
        )
    }
}

internal fun NavGraphBuilder.savedGameDestination(navController: NavController) {
    animatedComposable(
        route = Route.SAVED_GAME,
        arguments = listOf(navArgument("uid") { type = NavType.LongType }),
    ) {
        SavedGameScreen(
            navigateBack = { navController.popBackStack() },
            navigatePlayGame = { uid ->
                navController.navigate(
                    "game/$uid/${true}",
                ) {
                    popUpTo(Route.HISTORY)
                }
            },
            navigateToFolder = { uid ->
                navController.navigate("explorefolder/$uid") {
                    popUpTo("history")
                }
            },
            hiltViewModel(),
        )
    }
}

internal fun NavGraphBuilder.foldersDestination(navController: NavController) {
    animatedComposable(Route.FOLDERS) {
        FoldersScreen(
            viewModel = hiltViewModel(),
            navigateBack = { navController.popBackStack() },
            navigateExploreFolder = { uid ->
                navController.navigate("explorefolder/$uid")
            },
            navigateImportSudokuFile = { uri ->
                navController.navigate("import_sudoku_file?$uri?-1")
            },
            navigateViewSavedGame = { uid ->
                navController.navigate("saved_game/$uid")
            },
        )
    }
}

internal fun NavGraphBuilder.importFromFileDestination(
    navController: NavController,
    context: Context,
) {
    animatedComposable(
        route = "import_sudoku_file?{uri}?{folder_uid}",
        arguments =
            listOf(
                navArgument("uri") { type = NavType.StringType },
                navArgument("folder_uid") { type = NavType.LongType },
            ),
    ) {
        ImportFromFileScreen(
            viewModel = hiltViewModel(),
            navigateBack = {
                val activity = context.findActivity()
                activity?.intent?.data = null

                navController.navigateUp()
            },
        )
    }
}

internal fun NavGraphBuilder.exploreFolderDestination(navController: NavController) {
    animatedComposable(
        route = "explorefolder/{uid}",
        arguments = listOf(navArgument("uid") { type = NavType.LongType }),
    ) {
        ExploreFolderScreen(
            viewModel = hiltViewModel(),
            navigation =
                ExploreFolderNavigation(
                    navigateBack = { navController.popBackStack() },
                    navigatePlayGame = { args ->
                        navController.navigate(
                            "game/${args.first}/${args.second}",
                        ) {
                            popUpTo("explorefolder/${args.third}")
                        }
                    },
                    navigateImportFromFile = { args ->
                        // First - uri. Second = folder uid
                        navController.navigate("import_sudoku_file?${args.first}?${args.second}")
                    },
                    navigateEditGame = { args ->
                        navController.navigate("createeditsudoku/${args.first}/${args.second}")
                    },
                    navigateCreateSudoku = { folderUid ->
                        navController.navigate("createeditsudoku/-1/$folderUid")
                    },
                ),
        )
    }
}

internal fun NavGraphBuilder.importDeepLinkDestination(
    navController: NavController,
    context: Context,
) {
    animatedComposable(
        route = "import_sudoku_file_deeplink",
        deepLinks =
            listOf(
                navDeepLink {
                    uriPattern = "content://"
                    mimeType = "*/*"
                    action = Intent.ACTION_VIEW
                },
            ),
    ) {
        val activity = context.findActivity()
        if (activity != null) {
            val intentData = activity.intent.data
            if (intentData != null) {
                navController.navigate(
                    "import_sudoku_file?${Uri.encode(intentData.toString())}?-1",
                )
            }
            LaunchedEffect(intentData) {
                if (activity.intent.data == null) {
                    activity.finish()
                }
            }
        }
    }
}
