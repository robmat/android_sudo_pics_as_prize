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
import com.batodev.sudoku.ui.components.PreferenceRowInfo
import com.batodev.sudoku.ui.components.PreferenceRowSwitch
import com.batodev.sudoku.ui.components.board.Board
import com.batodev.sudoku.ui.components.board.BoardData
import com.batodev.sudoku.ui.components.board.BoardDisplayOptions
import com.batodev.sudoku.ui.components.board.BoardInteraction
import com.batodev.sudoku.ui.components.board.BoardStyle
import com.batodev.sudoku.ui.components.collapsingtopappbar.CollapsingTitle
import com.batodev.sudoku.ui.components.collapsingtopappbar.CollapsingTopAppBar
import com.batodev.sudoku.ui.components.collapsingtopappbar.CollapsingTopAppBarConfig
import com.batodev.sudoku.ui.components.collapsingtopappbar.CollapsingTopAppBarContent
import com.batodev.sudoku.ui.components.collapsingtopappbar.rememberTopAppBarScrollBehavior

// Cells shown unlocked/erroring in the board theme preview, as (row, col) pairs.
private const val UNLOCKED_ROW_1 = 1
private const val UNLOCKED_COL_1 = 1
private const val UNLOCKED_ROW_2 = 2
private const val UNLOCKED_COL_2 = 0
private const val UNLOCKED_ROW_3 = 5
private const val UNLOCKED_COL_3 = 4
private const val UNLOCKED_ROW_4 = 5
private const val UNLOCKED_COL_4 = 8
private const val ERROR_ROW_1 = 4
private const val ERROR_COL_1 = 4

private val PREVIEW_UNLOCKED_CELLS = listOf(
    UNLOCKED_ROW_1 to UNLOCKED_COL_1,
    UNLOCKED_ROW_2 to UNLOCKED_COL_2,
    UNLOCKED_ROW_3 to UNLOCKED_COL_3,
    UNLOCKED_ROW_4 to UNLOCKED_COL_4
)
private val PREVIEW_ERROR_CELLS = listOf(ERROR_ROW_1 to ERROR_COL_1)

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
                content = CollapsingTopAppBarContent(
                    collapsingTitle = CollapsingTitle.medium(titleText = stringResource(R.string.board_theme_title)),
                    navigationIcon = {
                        IconButton(onClick = navigateBack) {
                            Icon(
                                painter = painterResource(R.drawable.ic_round_arrow_back_24),
                                contentDescription = null
                            )
                        }
                    }
                ),
                config = CollapsingTopAppBarConfig(scrollBehavior = scrollBehavior)
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
            info = PreferenceRowInfo(
                title = stringResource(R.string.pref_boardtheme_accent),
                subtitle = stringResource(R.string.pref_boardtheme_accent_subtitle),
                painter = rememberVectorPainter(Icons.Outlined.Palette)
            ),
            checked = monetSudokuBoard,
            onClick = {
                viewModel.updateMonetSudokuBoardSetting(!monetSudokuBoard)
            }
        )

        PreferenceRowSwitch(
            info = PreferenceRowInfo(
                title = stringResource(R.string.pref_position_lines),
                subtitle = stringResource(R.string.pref_position_lines_summ),
                painter = rememberVectorPainter(Icons.Rounded.GridGoldenratio)
            ),
            checked = positionLines,
            onClick = { viewModel.updatePositionLinesSetting(!positionLines) }
        )

        PreferenceRowSwitch(
            info = PreferenceRowInfo(
                title = stringResource(R.string.pref_cross_highlight),
                subtitle = stringResource(R.string.pref_cross_highlight_subtitle),
                painter = rememberVectorPainter(Icons.Rounded.GridOn)
            ),
            checked = boardCrossHighlight,
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
        PREVIEW_UNLOCKED_CELLS.forEach { (row, col) ->
            grid[row][col].locked = false
        }
        PREVIEW_ERROR_CELLS.forEach { (row, col) ->
            grid[row][col].error = true
        }
    }
    var selectedCell by remember { mutableStateOf(Cell(-1, -1, 0)) }
    Board(
        modifier = modifier,
        data = BoardData(board = previewBoard, size = 9),
        interaction = BoardInteraction(
            selectedCell = selectedCell,
            onClick = { cell -> selectedCell = if (selectedCell == cell) Cell(-1, -1, 0) else cell }
        ),
        style = BoardStyle(
            boardColors = LocalBoardColors.current,
            displayOptions = BoardDisplayOptions(
                positionLines = positionLines,
                errorsHighlight = errosHighlight,
                crossHighlight = crossHighlight
            )
        )
    )
}
