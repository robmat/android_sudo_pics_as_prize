package com.batodev.sudoku.ui.gameshistory.savedgame

import android.os.Build.VERSION.SDK_INT
import android.widget.Toast
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.batodev.sudoku.LocalBoardColors
import com.batodev.sudoku.R
import com.batodev.sudoku.core.Cell
import com.batodev.sudoku.core.PreferencesConstants
import com.batodev.sudoku.ui.components.BackIconButton
import com.batodev.sudoku.ui.components.OverflowMenuButton
import com.batodev.sudoku.ui.components.PagerTab
import com.batodev.sudoku.ui.components.board.Board
import com.batodev.sudoku.ui.components.board.BoardData
import com.batodev.sudoku.ui.components.board.BoardDisplayOptions
import com.batodev.sudoku.ui.components.board.BoardInteraction
import com.batodev.sudoku.ui.components.board.BoardStyle
import com.batodev.sudoku.ui.components.board.BoardTextSizes
import com.batodev.sudoku.ui.util.pagerTabIndicatorOffsetM3
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter

private const val PAGE_COUNT = 2
private const val BOARD_SCALE_INITIAL = 0.3f
private const val BOARD_SCALE_ANIMATION_DURATION_MS = 300

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SavedGameTopBar(
    viewModel: SavedGameViewModel,
    navigateBack: () -> Unit,
) {
    TopAppBar(
        title = { Text(stringResource(R.string.game_id, viewModel.boardUid ?: -1)) },
        navigationIcon = {
            BackIconButton(onClick = navigateBack)
        },
        actions = {
            OverflowMenuButton { closeMenu ->
                DropdownMenuItem(
                    text = {
                        Text(stringResource(R.string.export_string_title))
                    },
                    onClick = {
                        viewModel.exportDialog = true
                        closeMenu()
                    },
                )
            }
        },
    )
}

@Composable
internal fun SavedGameContent(
    viewModel: SavedGameViewModel,
    innerPadding: PaddingValues,
    dateTimeFormatter: DateTimeFormatter,
    navigateToFolder: (Long) -> Unit,
    navigatePlayGame: (Long) -> Unit,
) {
    Column(
        modifier =
            Modifier
                .padding(innerPadding)
                .fillMaxWidth(),
    ) {
        SavedGameBoardPager(viewModel)
        SavedGameDetails(viewModel, dateTimeFormatter, navigateToFolder, navigatePlayGame)
    }
}

@Composable
private fun SavedGameBoardPager(viewModel: SavedGameViewModel) {
    val crossHighlight by viewModel.crossHighlight.collectAsStateWithLifecycle(
        initialValue = PreferencesConstants.DEFAULT_BOARD_CROSS_HIGHLIGHT,
    )
    val fontSizeFactor by viewModel.fontSize.collectAsState(
        initial = PreferencesConstants.DEFAULT_FONT_SIZE_FACTOR,
    )
    val fontSizeValue by remember(fontSizeFactor) {
        mutableStateOf(
            viewModel.getFontSize(factor = fontSizeFactor),
        )
    }

    val pagerState = rememberPagerState(pageCount = { PAGE_COUNT })
    SavedGamePagerTabs(pagerState)

    val boardScale = remember { Animatable(BOARD_SCALE_INITIAL) }
    LaunchedEffect(Unit) {
        boardScale.animateTo(
            targetValue = 1f,
            animationSpec =
                tween(
                    durationMillis = BOARD_SCALE_ANIMATION_DURATION_MS,
                    easing = LinearOutSlowInEasing,
                ),
        )
    }
    Column {
        SavedGameBoardsPager(pagerState, viewModel, crossHighlight, fontSizeValue, boardScale.value)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SavedGamePagerTabs(pagerState: PagerState) {
    val pages =
        listOf(
            stringResource(R.string.saved_game_current),
            stringResource(R.string.saved_game_initial),
        )
    TabRow(
        selectedTabIndex = pagerState.currentPage,
        divider = { },
        indicator = { tabPositions ->
            Box(
                modifier =
                    Modifier
                        .pagerTabIndicatorOffsetM3(pagerState, tabPositions)
                        .padding(horizontal = 16.dp)
                        .clip(RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp))
                        .background(color = MaterialTheme.colorScheme.primary)
                        .height(3.dp)
                        .fillMaxWidth(),
            )
        },
    ) {
        pages.forEachIndexed { index, title ->
            val coroutineScope = rememberCoroutineScope()
            PagerTab(
                selected = pagerState.currentPage == index,
                title = title,
                onClick = {
                    coroutineScope.launch {
                        pagerState.animateScrollToPage(index, 0f)
                    }
                },
            )
        }
    }
}

@Composable
private fun SavedGameBoardsPager(
    pagerState: PagerState,
    viewModel: SavedGameViewModel,
    crossHighlight: Boolean,
    fontSizeValue: TextUnit,
    boardScale: Float,
) {
    val boardModifier =
        Modifier
            .padding(10.dp)
            .scale(boardScale)
    val boardInteraction = BoardInteraction(selectedCell = Cell(-1, -1), onClick = { })
    val boardStyle =
        BoardStyle(
            boardColors = LocalBoardColors.current,
            textSizes = BoardTextSizes(mainTextSize = fontSizeValue),
            displayOptions = BoardDisplayOptions(crossHighlight = crossHighlight),
        )
    HorizontalPager(
        state = pagerState,
        modifier =
            Modifier
                .wrapContentHeight()
                .padding(top = 8.dp),
    ) { page ->
        val boardData =
            when (page) {
                0 -> BoardData(board = viewModel.parsedCurrentBoard, notes = viewModel.notes)
                else -> BoardData(board = viewModel.parsedInitialBoard)
            }
        Board(
            data = boardData,
            modifier = boardModifier,
            interaction = boardInteraction,
            style = boardStyle,
        )
    }
}

@Composable
internal fun SavedGameExportDialogHost(viewModel: SavedGameViewModel) {
    if (!viewModel.exportDialog) return
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    viewModel.boardEntity?.let {
        ExportDialog(
            onDismiss = { viewModel.exportDialog = false },
            boardString =
                it.initialBoard
                    .replace('0', '.')
                    .uppercase(),
            onClickCopy = {
                clipboardManager.setText(
                    AnnotatedString(
                        it.initialBoard
                            .replace('0', '.')
                            .uppercase(),
                    ),
                )
                // Android 13 and higher have its own notification when copying
                if (SDK_INT < 33) {
                    Toast
                        .makeText(
                            context,
                            R.string.export_string_state_copied,
                            Toast.LENGTH_SHORT,
                        ).show()
                }
            },
        )
    }
}
