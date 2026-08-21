package com.batodev.sudoku

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navOptions
import com.batodev.sudoku.core.PreferencesConstants
import com.batodev.sudoku.core.utils.AdSupportedActivity
import com.batodev.sudoku.data.datastore.AppSettingsManager
import com.batodev.sudoku.data.datastore.ThemeSettingsManager
import com.batodev.sudoku.ui.components.board.previewSudokuBoardColors
import com.batodev.sudoku.ui.gallery.GalleryActivity
import com.batodev.sudoku.ui.theme.BoardCellColors
import com.batodev.sudoku.ui.theme.BoardColors
import com.batodev.sudoku.ui.theme.BoardLineColors
import com.batodev.sudoku.ui.theme.SudokuBoardColorsImpl
import com.batodev.sudoku.ui.theme.SudokuTheme
import com.batodev.sudoku.ui.theme.resolveAppTheme
import com.batodev.sudoku.ui.theme.resolveDarkTheme
import com.batodev.sudoku.ui.util.Route
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

val LocalBoardColors = staticCompositionLocalOf { SudokuBoardColorsImpl() }

private const val AD_CHECK_INTERVAL_MS = 60000L * 5
private const val NOTES_COLOR_ALPHA = 0.85f
private const val ALT_FOREGROUND_COLOR_ALPHA = 0.7f
private const val THICK_LINE_COLOR_ALPHA = 0.55f
private const val THIN_LINE_COLOR_ALPHA = 0.25f

@AndroidEntryPoint
class MainActivity : AdSupportedActivity() {
    @Inject
    lateinit var settings: AppSettingsManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handlerAdPosting(AD_CHECK_INTERVAL_MS)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            val mainViewModel: MainActivityViewModel = hiltViewModel()
            MainActivityRoot(mainViewModel)
        }
    }
}

@Composable
private fun rememberBoardColors(monetSudokuBoard: Boolean): SudokuBoardColorsImpl =
    if (monetSudokuBoard) {
        previewSudokuBoardColors()
    } else {
        SudokuBoardColorsImpl(
            cellColors =
                BoardCellColors(
                    foregroundColor = MaterialTheme.colorScheme.onSurface,
                    notesColor = MaterialTheme.colorScheme.onSurface.copy(alpha = NOTES_COLOR_ALPHA),
                    altForegroundColor = MaterialTheme.colorScheme.onSurface.copy(alpha = ALT_FOREGROUND_COLOR_ALPHA),
                    errorColor = BoardColors.errorColor,
                    highlightColor = MaterialTheme.colorScheme.outline,
                ),
            lineColors =
                BoardLineColors(
                    thickLineColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(THICK_LINE_COLOR_ALPHA),
                    thinLineColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(THIN_LINE_COLOR_ALPHA),
                ),
        )
    }

@Composable
private fun MainActivityNavEffects(
    navController: NavController,
    firstLaunch: Boolean,
    onBottomBarStateChange: (Boolean) -> Unit,
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentOnBottomBarStateChange by rememberUpdatedState(onBottomBarStateChange)

    LaunchedEffect(navBackStackEntry) {
        currentOnBottomBarStateChange(
            when (navBackStackEntry?.destination?.route) {
                Route.STATISTICS, Route.HOME, Route.MORE -> true
                else -> false
            },
        )
    }
    LaunchedEffect(firstLaunch) {
        if (firstLaunch) {
            navController.navigate(
                route = Route.WELCOME_SCREEN,
                navOptions =
                    navOptions {
                        popUpTo(Route.HOME) {
                            inclusive = true
                        }
                    },
            )
        }
    }
}

@Composable
private fun MainActivityScaffold(
    navController: NavHostController,
    bottomBarState: Boolean,
) {
    val context = LocalContext.current
    Scaffold(
        bottomBar = {
            NavigationBar(
                navController = navController,
                bottomBarState = bottomBarState,
            )
        },
        contentWindowInsets = WindowInsets(0.dp),
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Route.HOME,
            modifier = Modifier.padding(paddingValues),
        ) {
            buildSudokuNavigationGraph(navController, context)
        }
    }
}

@Composable
private fun MainActivityRoot(mainViewModel: MainActivityViewModel) {
    val dynamicColors by mainViewModel.dc.collectAsState(isSystemInDarkTheme())
    val darkTheme by mainViewModel.darkTheme.collectAsState(PreferencesConstants.DEFAULT_DARK_THEME)
    val amoledBlack by mainViewModel.amoledBlack.collectAsState(PreferencesConstants.DEFAULT_AMOLED_BLACK)
    val firstLaunch by mainViewModel.firstLaunch.collectAsState(false)
    val currentTheme by mainViewModel.currentTheme.collectAsState(PreferencesConstants.DEFAULT_SELECTED_THEME)
    SudokuTheme(
        darkTheme = resolveDarkTheme(darkTheme),
        dynamicColor = dynamicColors,
        amoled = amoledBlack,
        appTheme = resolveAppTheme(currentTheme),
    ) {
        val navController = rememberNavController()
        var bottomBarState by rememberSaveable { mutableStateOf(false) }

        MainActivityNavEffects(
            navController = navController,
            firstLaunch = firstLaunch,
            onBottomBarStateChange = { bottomBarState = it },
        )

        val monetSudokuBoard by mainViewModel.monetSudokuBoard.collectAsStateWithLifecycle(
            initialValue = PreferencesConstants.DEFAULT_MONET_SUDOKU_BOARD,
        )
        val boardColors = rememberBoardColors(monetSudokuBoard)

        CompositionLocalProvider(LocalBoardColors provides boardColors) {
            MainActivityScaffold(navController, bottomBarState)
        }
    }
}

@Composable
private fun RowScope.GalleryNavItem(selectedScreen: String) {
    val context = LocalContext.current
    NavigationBarItem(
        icon = {
            Icon(
                painter = painterResource(R.drawable.ic_round_gallery_24),
                contentDescription = null,
            )
        },
        selected = selectedScreen == Route.GALLERY,
        label = {
            Text(
                text = stringResource(R.string.nav_bar_gallery),
                fontWeight = FontWeight.Bold,
            )
        },
        onClick = {
            ContextCompat.startActivity(context, Intent(context, GalleryActivity::class.java), null)
        },
    )
}

@Composable
private fun RowScope.MainNavItem(
    navController: NavController,
    selectedScreen: String,
    icon: Painter,
    route: String,
    labelRes: Int,
) {
    NavigationBarItem(
        icon = {
            Icon(
                painter = icon,
                contentDescription = null,
            )
        },
        selected = selectedScreen == route,
        label = {
            Text(
                text = stringResource(labelRes),
                fontWeight = FontWeight.Bold,
            )
        },
        onClick = {
            navController.navigate(route) {
                launchSingleTop = true
            }
        },
    )
}

@Composable
private fun BottomNavigationBarContent(navController: NavController) {
    var selectedScreen by remember { mutableStateOf(Route.HOME) }
    val navBarScreens =
        listOf(
            Pair(Route.HOME, R.string.nav_bar_home),
            Pair(Route.MORE, R.string.nav_bar_more),
        )
    val navBarIcons =
        listOf(
            painterResource(R.drawable.ic_round_home_24),
            painterResource(R.drawable.ic_round_more_horiz_24),
        )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    LaunchedEffect(currentDestination) {
        currentDestination?.let {
            selectedScreen = it.route ?: ""
        }
    }

    NavigationBar {
        GalleryNavItem(selectedScreen)
        navBarScreens.forEachIndexed { index, item ->
            MainNavItem(
                navController = navController,
                selectedScreen = selectedScreen,
                icon = navBarIcons[index],
                route = item.first,
                labelRes = item.second,
            )
        }
    }
}

@Composable
fun NavigationBar(
    navController: NavController,
    bottomBarState: Boolean,
) {
    AnimatedContent(
        targetState = bottomBarState,
        label = "this_label_makes_no_sense_to_me_but_i_added_to_overcome_a_warning",
    ) { visible ->
        if (visible) {
            BottomNavigationBarContent(navController)
        }
    }
}

@HiltViewModel
class MainActivityViewModel
    @Inject
    constructor(
        themeSettingsManager: ThemeSettingsManager,
        appSettingsManager: AppSettingsManager,
    ) : ViewModel() {
        val dc = themeSettingsManager.dynamicColors
        val darkTheme = themeSettingsManager.darkTheme
        val amoledBlack = themeSettingsManager.amoledBlack
        val firstLaunch = appSettingsManager.firstLaunch
        val currentTheme = themeSettingsManager.currentTheme
        val monetSudokuBoard = themeSettingsManager.monetSudokuBoard
    }
