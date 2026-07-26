package com.batodev.sudoku.ui.settings.boardtheme

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.rounded.GridGoldenratio
import androidx.compose.material.icons.rounded.GridOn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.batodev.sudoku.LocalBoardColors
import com.batodev.sudoku.R
import com.batodev.sudoku.core.Cell
import com.batodev.sudoku.core.PreferencesConstants
import com.batodev.sudoku.core.qqwing.GameType
import com.batodev.sudoku.core.utils.SudokuParser
import com.batodev.sudoku.ui.components.PreferenceRowSwitch
import com.batodev.sudoku.ui.components.board.Board
import com.batodev.sudoku.ui.components.collapsingtopappbar.CollapsingTitle
import com.batodev.sudoku.ui.components.collapsingtopappbar.CollapsingTopAppBar
import com.batodev.sudoku.ui.components.collapsingtopappbar.rememberTopAppBarScrollBehavior

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsBoardTheme(
    viewModel: SettingsBoardThemeViewModel,
    navigateBack: () -> Unit
) {
    val scrollBehavior = rememberTopAppBarScrollBehavior()
    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            CollapsingTopAppBar(
                collapsingTitle = CollapsingTitle.medium(titleText = stringResource(R.string.board_theme_title)),
                navigationIcon = {
                    IconButton(onClick = navigateBack) {
                        Icon(
                            painter = painterResource(R.drawable.ic_round_arrow_back_24),
                            contentDescription = null
                        )
                    }
                },
                scrollBehavior = scrollBehavior
            )
        }
    ) { paddingValues ->
        BoardThemeSettingsContent(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            viewModel = viewModel
        )
    }
}

@Composable
private fun BoardThemeSettingsContent(modifier: Modifier, viewModel: SettingsBoardThemeViewModel) {
    Column(modifier = modifier) {
        val positionLines by viewModel.positionLines.collectAsStateWithLifecycle(
            initialValue = PreferencesConstants.DEFAULT_POSITION_LINES
        )
        val highlightMistakes by viewModel.highlightMistakes.collectAsState(
            initial = PreferencesConstants.DEFAULT_HIGHLIGHT_MISTAKES
        )
        val boardCrossHighlight by viewModel.crossHighlight.collectAsState(
            initial = PreferencesConstants.DEFAULT_BOARD_CROSS_HIGHLIGHT
        )
        BoardPreviewTheme(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 16.dp),
            positionLines = positionLines,
            errosHighlight = highlightMistakes != 0,
            crossHighlight = boardCrossHighlight
        )

        val monetSudokuBoard by viewModel.monetSudokuBoard.collectAsStateWithLifecycle(
            PreferencesConstants.DEFAULT_MONET_SUDOKU_BOARD
        )
        PreferenceRowSwitch(
            title = stringResource(R.string.pref_boardtheme_accent),
            subtitle = stringResource(R.string.pref_boardtheme_accent_subtitle),
            checked = monetSudokuBoard,
            painter = rememberVectorPainter(Icons.Outlined.Palette),
            onClick = {
                viewModel.updateMonetSudokuBoardSetting(!monetSudokuBoard)
            }
        )

        PreferenceRowSwitch(
            title = stringResource(R.string.pref_position_lines),
            subtitle = stringResource(R.string.pref_position_lines_summ),
            checked = positionLines,
            painter = rememberVectorPainter(Icons.Rounded.GridGoldenratio),
            onClick = { viewModel.updatePositionLinesSetting(!positionLines) }
        )

        PreferenceRowSwitch(
            title = stringResource(R.string.pref_cross_highlight),
            subtitle = stringResource(R.string.pref_cross_highlight_subtitle),
            checked = boardCrossHighlight,
            painter = rememberVectorPainter(Icons.Rounded.GridOn),
            onClick = { viewModel.updateBoardCrossHighlight(!boardCrossHighlight) }
        )
    }
}

@Composable
private fun BoardPreviewTheme(
    positionLines: Boolean,
    errosHighlight: Boolean,
    crossHighlight: Boolean,
    modifier: Modifier = Modifier
) {
    val sudokuParser = SudokuParser()
    val previewBoard = sudokuParser.parseBoard(
        board = "..1...9...2..17.545...24..328.....9...529..47.74.9...1...........9..5.....3.4....",
        gameType = GameType.Default9x9,
        locked = true,
        emptySeparator = '.'
    ).also { grid ->
        listOf(1 to 1, 2 to 0, 5 to 4, 5 to 8).forEach { (row, col) ->
            grid[row][col].locked = false
        }
        listOf(4 to 4).forEach { (row, col) ->
            grid[row][col].error = true
        }
    }
    var selectedCell by remember { mutableStateOf(Cell(-1, -1, 0)) }
    Board(
        modifier = modifier,
        board = previewBoard,
        size = 9,
        selectedCell = selectedCell,
        onClick = { cell -> selectedCell = if (selectedCell == cell) Cell(-1, -1, 0) else cell },
        boardColors = LocalBoardColors.current,
        positionLines = positionLines,
        errorsHighlight = errosHighlight,
        crossHighlight = crossHighlight
    )
}
