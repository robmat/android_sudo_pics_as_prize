package com.batodev.sudoku.ui.statistics

import android.text.format.DateUtils
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.batodev.sudoku.R
import com.batodev.sudoku.core.qqwing.GameDifficulty
import com.batodev.sudoku.core.qqwing.GameType
import com.batodev.sudoku.data.database.model.Record
import com.batodev.sudoku.data.database.model.SavedGame
import com.batodev.sudoku.ui.components.EmptyScreen
import com.batodev.sudoku.ui.components.HelpCard
import kotlinx.coroutines.flow.Flow
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    navigateHistory: () -> Unit,
    navigateSavedGame: (Long) -> Unit,
    viewModel: StatisticsViewModel,
    modifier: Modifier = Modifier,
) {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())
    val dateFormat by viewModel.dateFormat.collectAsStateWithLifecycle(initialValue = "")
    Scaffold(
        modifier =
            modifier
                .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = { StatisticsTopBar(scrollBehavior, navigateHistory) },
    ) { scaffoldPadding ->
        val recordList by viewModel.recordList.collectAsState(initial = emptyList())
        val savedGameList by viewModel.savedGamesList.collectAsState(initial = emptyList())

        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(scaffoldPadding)
                    .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            ChipRowDifficulty(
                items = statisticsDifficultyFilters(),
                selected = viewModel.selectedDifficulty,
                onSelect = { viewModel.setDifficulty(it) },
            )
            ChipRowType(
                types = statisticsTypeFilters(),
                selected = viewModel.selectedType,
                onSelect = { viewModel.setType(it) },
            )

            if (recordList.isNotEmpty()) {
                RecordsStatisticsContent(
                    recordList = recordList,
                    savedGameList = savedGameList,
                    filterState =
                        StatisticsFilterState(
                            selectedDifficulty = viewModel.selectedDifficulty,
                            selectedType = viewModel.selectedType,
                        ),
                    tipCards =
                        StatisticsTipCards(
                            recordTipCard = viewModel.recordTipCard,
                            onSetRecordTipCard = viewModel::setRecordTipCard,
                            streakTipCard = viewModel.streakTipCard,
                            onSetStreakTipCard = viewModel::setStreakTipCard,
                        ),
                    streakActions =
                        StatisticsStreakActions(
                            getWinRate = viewModel::getWinRate,
                            getCurrentStreak = viewModel::getCurrentStreak,
                            getMaxStreak = viewModel::getMaxStreak,
                        ),
                    bestGamesState = BestGamesState(showDeleteDialog = viewModel.showDeleteDialog),
                    bestGamesActions =
                        BestGamesActions(
                            onShowDeleteDialog = { viewModel.showDeleteDialog = true },
                            onDismissDeleteDialog = { viewModel.showDeleteDialog = false },
                            onDeleteRecord = viewModel::deleteRecord,
                        ),
                    dateFormat = dateFormat,
                    navigateSavedGame = navigateSavedGame,
                )
            } else {
                EmptyScreen(stringResource(R.string.statistics_no_records))
            }
        }
    }
}

private const val BEST_GAMES_SHOWN_COUNT = 5

internal data class StatisticsFilterState(
    val selectedDifficulty: GameDifficulty,
    val selectedType: GameType,
)

internal data class StatisticsTipCards(
    val recordTipCard: Flow<Boolean>,
    val onSetRecordTipCard: (Boolean) -> Unit,
    val streakTipCard: Flow<Boolean>,
    val onSetStreakTipCard: (Boolean) -> Unit,
)

internal data class StatisticsStreakActions(
    val getWinRate: (List<SavedGame>) -> Float,
    val getCurrentStreak: (List<SavedGame>) -> Int,
    val getMaxStreak: (List<SavedGame>) -> Int,
)

internal data class BestGamesState(
    val showDeleteDialog: Boolean,
)

internal data class BestGamesActions(
    val onShowDeleteDialog: () -> Unit,
    val onDismissDeleteDialog: () -> Unit,
    val onDeleteRecord: (Record) -> Unit,
)

@Composable
private fun RecordsStatisticsContent(
    recordList: List<Record>,
    savedGameList: List<SavedGame>,
    filterState: StatisticsFilterState,
    tipCards: StatisticsTipCards,
    streakActions: StatisticsStreakActions,
    bestGamesState: BestGamesState,
    bestGamesActions: BestGamesActions,
    dateFormat: String,
    navigateSavedGame: (Long) -> Unit,
) = Column {
    var averageTime by remember {
        mutableStateOf(
            DateUtils.formatElapsedTime(
                recordList.sumOf { it.time.seconds } / recordList.count(),
            ),
        )
    }
    var bestTime by remember {
        mutableStateOf(
            DateUtils.formatElapsedTime(
                recordList
                    .first()
                    .time.seconds,
            ),
        )
    }
    LaunchedEffect(recordList) {
        averageTime =
            DateUtils.formatElapsedTime(
                recordList
                    .sumOf { it.time.seconds } / recordList.count(),
            )
        bestTime =
            DateUtils.formatElapsedTime(
                recordList
                    .first()
                    .time.seconds,
            )
    }
    StatisticsSection(
        title = stringResource(R.string.time),
        painter = painterResource(R.drawable.ic_round_hourglass_empty_24),
        statRows =
            listOf(
                listOf(stringResource(R.string.best_time), bestTime),
                listOf(stringResource(R.string.average_time), averageTime),
            ),
    )
    if (filterState.selectedDifficulty == GameDifficulty.Unspecified) {
        OverallStatisticsContent(savedGameList, streakActions, tipCards)
    }
    BestGamesList(recordList, filterState, bestGamesState, bestGamesActions, dateFormat, navigateSavedGame)
    val recordCard by tipCards.recordTipCard.collectAsState(initial = false)
    AnimatedVisibility(visible = recordCard) {
        HelpCard(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            title = stringResource(R.string.tip_card_records_list_title),
            details = stringResource(R.string.tip_card_records_list_summ),
            painter = painterResource(R.drawable.ic_outline_help_outline_24),
            onClose = { tipCards.onSetRecordTipCard(false) },
        )
    }
}

@Composable
private fun OverallStatisticsContent(
    savedGameList: List<SavedGame>,
    streakActions: StatisticsStreakActions,
    tipCards: StatisticsTipCards,
) = Column {
    GamesOverallStatistics(savedGameList, streakActions)
    WinStreakStatistics(savedGameList, streakActions, tipCards)
}

@Composable
private fun GamesOverallStatistics(
    savedGameList: List<SavedGame>,
    streakActions: StatisticsStreakActions,
) {
    val gamesStarted by remember {
        mutableStateOf(savedGameList.count().toString())
    }
    val gamesCompleted by remember {
        mutableStateOf(
            savedGameList
                .count { it.completed && !it.giveUp && !it.canContinue }
                .toString(),
        )
    }
    val winRate =
        if (savedGameList.isNotEmpty()) {
            stringResource(
                R.string.win_rate_percentage,
                streakActions.getWinRate(savedGameList).roundToInt(),
            )
        } else {
            stringResource(R.string.no_value_default)
        }
    OverallStatistics(
        statsRow =
            listOf(
                listOf(stringResource(R.string.games_started), gamesStarted),
                listOf(stringResource(R.string.games_completed), gamesCompleted),
                listOf(stringResource(R.string.win_rate), winRate),
            ),
    )
}

@Composable
private fun WinStreakStatistics(
    savedGameList: List<SavedGame>,
    streakActions: StatisticsStreakActions,
    tipCards: StatisticsTipCards,
) = Column {
    val currentStreak by remember {
        mutableStateOf(
            streakActions.getCurrentStreak(savedGameList).toString(),
        )
    }
    val maxStreak by remember {
        mutableStateOf(
            streakActions.getMaxStreak(savedGameList).toString(),
        )
    }

    StatisticsSection(
        title = stringResource(R.string.win_streak),
        painter = painterResource(R.drawable.ic_outline_verified_24),
        statRows =
            listOf(
                listOf(stringResource(R.string.current_streak), currentStreak),
                listOf(stringResource(R.string.best_streak), maxStreak),
            ),
    )
    val streakCard by tipCards.streakTipCard.collectAsState(initial = false)
    AnimatedVisibility(visible = streakCard) {
        HelpCard(
            modifier = Modifier.padding(horizontal = 12.dp),
            title = stringResource(R.string.win_streak),
            details = stringResource(R.string.tip_card_win_streak_summ),
            painter = painterResource(R.drawable.ic_outline_verified_24),
            onClose = { tipCards.onSetStreakTipCard(false) },
        )
    }
}

@Composable
private fun BestGamesList(
    recordList: List<Record>,
    filterState: StatisticsFilterState,
    bestGamesState: BestGamesState,
    bestGamesActions: BestGamesActions,
    dateFormat: String,
    navigateSavedGame: (Long) -> Unit,
) = Column {
    StatsSectionName(
        modifier = Modifier.padding(start = 12.dp, top = 12.dp),
        title =
            stringResource(R.string.number_best_games, BEST_GAMES_SHOWN_COUNT) +
                if (filterState.selectedType != GameType.Unspecified &&
                    filterState.selectedDifficulty != GameDifficulty.Unspecified
                ) {
                    " ${stringResource(filterState.selectedType.resName).lowercase()} " +
                        stringResource(filterState.selectedDifficulty.resName).lowercase()
                } else {
                    ""
                },
        painter = painterResource(R.drawable.ic_outline_star_24),
    )
    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(start = 12.dp, end = 12.dp, bottom = 12.dp)
                .clip(MaterialTheme.shapes.large)
                .background(MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Column {
            var selectedIndex by remember { mutableIntStateOf(0) }
            recordList.take(BEST_GAMES_SHOWN_COUNT).forEachIndexed { index, record ->
                RecordItem(
                    info =
                        RecordItemInfo(
                            time = record.time,
                            difficulty = stringResource(record.difficulty.resName),
                            date = record.date.toLocalDateTime(),
                            type = stringResource(record.type.resName),
                            dateFormat = dateFormat,
                        ),
                    onClick = {
                        navigateSavedGame(record.boardUid)
                    },
                    onLongClick = {
                        selectedIndex = index
                        bestGamesActions.onShowDeleteDialog()
                    },
                )
            }
            if (bestGamesState.showDeleteDialog) {
                ShowDeleteDialog(
                    onDismissRequest = bestGamesActions.onDismissDeleteDialog,
                    onConfirm = {
                        bestGamesActions.onDeleteRecord(
                            recordList[selectedIndex],
                        )
                    },
                    index = selectedIndex,
                )
            }
        }
    }
}

@Composable
fun OverallStatistics(
    statsRow: List<List<String>>,
    modifier: Modifier = Modifier,
) {
    StatisticsSection(
        modifier = modifier,
        title = stringResource(R.string.games),
        painter = painterResource(R.drawable.ic_rounded_stadia_controller_24),
        statRows = statsRow,
    )
}

@Composable
fun StatisticsSection(
    title: String,
    painter: Painter,
    statRows: List<List<String>>,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(12.dp),
    ) {
        StatsSectionName(
            title = title,
            painter = painter,
        )
        Card(
            modifier =
                Modifier
                    .fillMaxWidth(),
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
            ) {
                statRows.forEachIndexed { index, arr ->
                    StatRow(
                        startText = arr[0],
                        endText = arr[1],
                        modifier = Modifier.padding(vertical = 8.dp),
                    )
                    if (index + 1 != statRows.size) {
                        Divider(
                            modifier = Modifier.fillMaxWidth(),
                            color = MaterialTheme.colorScheme.outline,
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StatsSectionName(
    title: String,
    painter: Painter,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painter,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.secondary,
        )
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(start = 12.dp),
        )
    }
}

private const val STAT_ROW_END_TEXT_FONT_WEIGHT = 700

@Composable
fun StatRow(
    startText: String,
    endText: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = startText,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f),
        )
        Text(
            text = endText,
            fontWeight = FontWeight(STAT_ROW_END_TEXT_FONT_WEIGHT),
        )
    }
}
