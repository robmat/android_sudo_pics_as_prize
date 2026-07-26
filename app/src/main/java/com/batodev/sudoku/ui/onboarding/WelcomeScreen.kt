package com.batodev.sudoku.ui.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.batodev.sudoku.LocalBoardColors
import com.batodev.sudoku.R
import com.batodev.sudoku.core.Cell
import com.batodev.sudoku.core.qqwing.GameType
import com.batodev.sudoku.core.utils.SudokuParser
import com.batodev.sudoku.data.datastore.AppSettingsManager
import com.batodev.sudoku.ui.components.board.Board
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@Composable
fun WelcomeScreen(
    navigateToGame: () -> Unit,
    viewModel: WelcomeViewModel
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .systemBarsPadding(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                text = stringResource(R.string.onboard_title),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Medium
            )
            FirstPage(
                selectedCellChanged = { viewModel.selectedCell = it },
                selectedCell = viewModel.selectedCell,
                board = viewModel.previewBoard,
                onFinishedClick = {
                    viewModel.setFirstLaunch()
                    navigateToGame()
                }
            )
        }
    }
}

@Composable
fun FirstPage(
    selectedCellChanged: (Cell) -> Unit,
    selectedCell: Cell,
    board: List<List<Cell>>,
    onFinishedClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(stringResource(R.string.intro_what_is_sudoku))
            Text(stringResource(R.string.intro_rules))
            Board(
                board = board,
                size = 9,
                selectedCell = selectedCell,
                onClick = { cell -> selectedCellChanged(cell) },
                boardColors = LocalBoardColors.current
            )
            Text(stringResource(R.string.onboard_recommendation_prefs))
            FilledTonalButton(
                onClick = onFinishedClick,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text(stringResource(R.string.action_start))
            }
        }
    }
}

@HiltViewModel
class WelcomeViewModel
@Inject constructor(
    private val settingsDataManager: AppSettingsManager
) : ViewModel() {
    var selectedCell by mutableStateOf(Cell(-1, -1, 0))

    private val sudokuParser = SudokuParser()
    val previewBoard = sudokuParser.parseBoard(
        board = "..1...9...2..17.545...24..328.....9...52...47.74.9...1...........9..5.....3.4....",
        gameType = GameType.Default9x9,
        emptySeparator = '.'
    )

    fun setFirstLaunch(value: Boolean = false) {
        viewModelScope.launch(Dispatchers.IO) {
            settingsDataManager.setFirstLaunch(value)
        }
    }
}
