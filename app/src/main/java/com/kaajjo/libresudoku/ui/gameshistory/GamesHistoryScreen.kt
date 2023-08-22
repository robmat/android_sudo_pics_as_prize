package com.kaajjo.libresudoku.ui.gameshistory

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kaajjo.libresudoku.LocalBoardColors
import com.kaajjo.libresudoku.R
import com.kaajjo.libresudoku.core.qqwing.GameDifficulty
import com.kaajjo.libresudoku.core.qqwing.GameType
import com.kaajjo.libresudoku.core.utils.toFormattedString
import com.kaajjo.libresudoku.data.database.model.SavedGame
import com.kaajjo.libresudoku.data.database.model.SudokuBoard
import com.kaajjo.libresudoku.data.datastore.AppSettingsManager
import com.kaajjo.libresudoku.ui.components.AnimatedIconFilterChip
import com.kaajjo.libresudoku.ui.components.EmptyScreen
import com.kaajjo.libresudoku.ui.components.ScrollbarLazyColumn
import com.kaajjo.libresudoku.ui.components.board.BoardPreview
import com.kaajjo.libresudoku.ui.create_edit_sudoku.GameStateFilter
import com.kaajjo.libresudoku.ui.util.disableSplitMotionEvents
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter
import kotlin.math.sqrt
import kotlin.time.toKotlinDuration

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GamesHistoryScreen(
    navigateBack: () -> Unit,
    navigateSavedGame: (Long) -> Unit,
    viewModel: HistoryViewModel
) {
    val coroutineScope = rememberCoroutineScope()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())
    var filterBottomSheet by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.history_title)) },
                navigationIcon = {
                    IconButton(onClick = navigateBack) {
                        Icon(
                            painter = painterResource(R.drawable.ic_round_arrow_back_24),
                            contentDescription = null
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {
                        coroutineScope.launch {
                            filterBottomSheet = true
                        }
                    }) {
                        Icon(
                            painter = painterResource(R.drawable.ic_round_filter_list_24),
                            contentDescription = null
                        )
                    }
                },
                scrollBehavior = scrollBehavior
            )
        },
    ) { innerPadding ->
        val games by viewModel.games.collectAsState(initial = emptyMap())
        val dateFormat by viewModel.dateFormat.collectAsStateWithLifecycle("")

        if (games.isNotEmpty()) {
            Column(
                modifier = Modifier.padding(innerPadding)
            ) {
                val lazyListState = rememberLazyListState()
                var filteredAndSortedBoards by remember {
                    mutableStateOf(
                        emptyList<Pair<SavedGame, SudokuBoard>>()
                    )
                }
                LaunchedEffect(
                    viewModel.sortType,
                    viewModel.sortEntry,
                    viewModel.filterDifficulties,
                    viewModel.filterGameTypes,
                    viewModel.filterByGameState
                ) {
                    filteredAndSortedBoards = viewModel.applySortAndFilter(games.toList())
                    lazyListState.animateScrollToItem(0)
                }

                ScrollbarLazyColumn(
                    modifier = Modifier
                        .disableSplitMotionEvents(),
                    state = lazyListState
                ) {
                    itemsIndexed(
                        filteredAndSortedBoards,
                        key = { _, game -> game.first.uid }
                    ) { index, game ->
                        SudokuHistoryItem(
                            board = game.first.currentBoard,
                            savedGame = game.first,
                            difficulty = stringResource(game.second.difficulty.resName),
                            type = stringResource(game.second.type.resName),
                            onClick = {
                                navigateSavedGame(game.first.uid)

                            },
                            dateTimeFormatter = AppSettingsManager.dateFormat(dateFormat)
                        )
                        if (index < filteredAndSortedBoards.size - 1) {
                            Divider(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(CircleShape)
                                    .padding(horizontal = 12.dp, vertical = 1.dp)
                            )
                        }
                    }
                }
            }
        } else {
            EmptyScreen(stringResource(R.string.history_no_games))
        }
    }

    if (filterBottomSheet) {
        ModalBottomSheet(onDismissRequest = { filterBottomSheet = false }) {
            Column(Modifier.padding(vertical = 12.dp)) {
                Text(
                    text = stringResource(R.string.sort_label),
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(start = 12.dp)
                )
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        FilterChip(
                            selected = viewModel.sortType == SortType.Ascending,
                            label = { Text(stringResource(R.string.sort_ascending)) },
                            onClick = {
                                viewModel.switchSortType()
                            }
                        )
                    }
                    items(enumValues<SortEntry>().toList()) {
                        FilterChip(
                            selected = it == viewModel.sortEntry,
                            label = { Text(stringResource(it.resName)) },
                            onClick = {
                                viewModel.selectSortEntry(it)
                            }
                        )
                    }
                }
                Text(
                    text = stringResource(R.string.filter_label),
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(start = 12.dp)
                )
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(
                        listOf(
                            GameDifficulty.Easy,
                            GameDifficulty.Moderate,
                            GameDifficulty.Hard,
                            GameDifficulty.Challenge,
                            GameDifficulty.Custom,
                        )
                    ) {
                        AnimatedIconFilterChip(
                            selected = viewModel.filterDifficulties.contains(it),
                            label = stringResource(it.resName),
                            onClick = {
                                viewModel.selectFilter(it)
                            }
                        )
                    }
                }
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(
                        listOf(
                            GameType.Default9x9,
                            GameType.Default6x6,
                            GameType.Default12x12,
                        )
                    ) {
                        AnimatedIconFilterChip(
                            selected = viewModel.filterGameTypes.contains(it),
                            label = stringResource(it.resName),
                            onClick = {
                                viewModel.selectFilter(it)
                            }
                        )
                    }
                }
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(
                        listOf(
                            GameStateFilter.All,
                            GameStateFilter.Completed,
                            GameStateFilter.InProgress,
                        )
                    ) {
                        FilterChip(
                            selected = it == viewModel.filterByGameState,
                            label = { Text(stringResource(it.resName)) },
                            onClick = {
                                viewModel.selectFilter(it)
                            }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SudokuHistoryItem(
    board: String,
    difficulty: String,
    type: String,
    savedGame: SavedGame,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = { },
    dateTimeFormatter: DateTimeFormatter
) {
    Box(
        modifier = modifier
            .clip(MaterialTheme.shapes.medium)
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .size(130.dp)
                    .align(Alignment.CenterVertically)
            ) {
                BoardPreview(
                    size = sqrt(board.length.toFloat()).toInt(),
                    boardString = board,
                    boardColors = LocalBoardColors.current
                )
            }
            Column(
                modifier = Modifier
                    .padding(horizontal = 12.dp)
            ) {
                Column {
                    Text("$difficulty $type")
                    Text(
                        text = stringResource(
                            R.string.history_item_time,
                            savedGame.timer
                                .toKotlinDuration()
                                .toFormattedString()
                        )
                    )
                    Text(stringResource(R.string.history_item_id, savedGame.uid))
                }


                if (savedGame.startedAt != null) {
                    val startedAtDate by remember(savedGame) {
                        mutableStateOf(
                            savedGame.startedAt.format(dateTimeFormatter)
                        )
                    }
                    val startedAtTime by remember(savedGame) {
                        mutableStateOf(
                            savedGame.startedAt.format(DateTimeFormatter.ofPattern("HH:mm"))
                        )
                    }
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(startedAtDate)
                        Text(startedAtTime)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                if (savedGame.canContinue) {
                    Box(
                        modifier = Modifier
                            .background(
                                color = MaterialTheme.colorScheme.primaryContainer,
                                shape = MaterialTheme.shapes.large
                            )
                            .padding(vertical = 4.dp, horizontal = 8.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.can_continue_label),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }
    }
}