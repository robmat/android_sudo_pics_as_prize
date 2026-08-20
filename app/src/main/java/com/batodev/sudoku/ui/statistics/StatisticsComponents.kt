package com.batodev.sudoku.ui.statistics

import android.text.format.DateUtils
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ElevatedFilterChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.batodev.sudoku.R
import com.batodev.sudoku.core.qqwing.GameDifficulty
import com.batodev.sudoku.core.qqwing.GameType
import com.batodev.sudoku.data.datastore.AppSettingsManager
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsTopBar(scrollBehavior: TopAppBarScrollBehavior, navigateHistory: () -> Unit) {
    TopAppBar(
        title = { Text(stringResource(R.string.statistics)) },
        scrollBehavior = scrollBehavior,
        actions = {
            IconButton(onClick = navigateHistory) {
                Icon(
                    painter = painterResource(R.drawable.ic_round_history_24),
                    contentDescription = null
                )
            }
        }
    )
}

fun statisticsDifficultyFilters(): List<GameDifficulty> = listOf(
    GameDifficulty.Unspecified,
    GameDifficulty.Easy,
    GameDifficulty.Moderate,
    GameDifficulty.Hard,
    GameDifficulty.Challenge,
    GameDifficulty.Custom
)

@Composable
fun statisticsTypeFilters(): List<Pair<GameType, String>> = listOf(
    Pair(GameType.Default9x9, stringResource(R.string.type_default_9x9)),
    Pair(GameType.Default6x6, stringResource(R.string.type_default_6x6)),
    Pair(GameType.Default12x12, stringResource(R.string.type_default_12x12))
)

@Composable
fun ShowDeleteDialog(
    modifier: Modifier = Modifier,
    onDismissRequest: () -> Unit,
    onConfirm: () -> Unit,
    index: Int
) {
    AlertDialog(
        modifier = modifier,
        onDismissRequest = onDismissRequest,
        confirmButton = {
            TextButton(onClick = {
                onConfirm()
                onDismissRequest()
            }) {
                Text(stringResource(R.string.dialog_yes))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(stringResource(R.string.dialog_no))
            }
        },
        title = {
            Text(stringResource(R.string.delete_question))
        },
        text = {
            Text(
                text = stringResource(R.string.delete_record_dialog, index + 1)
            )
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChipRowType(
    modifier: Modifier = Modifier,
    types: List<Pair<GameType, String>>,
    selected: GameType,
    onSelected: (GameType) -> Unit
) {
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(types) { type ->
            val selectedColor by animateColorAsState(
                targetValue = if (type.first == selected) {
                    MaterialTheme.colorScheme.secondaryContainer
                } else {
                    MaterialTheme.colorScheme.surface
                }
            )
            ElevatedFilterChip(
                modifier = Modifier.padding(horizontal = 2.dp),
                selected = type.first == selected,
                onClick = { onSelected(type.first) },
                label = { Text(type.second) },
                shape = RoundedCornerShape(16.dp),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = selectedColor,
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = FilterChipDefaults.elevatedFilterChipElevation(4.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChipRowDifficulty(
    items: List<GameDifficulty>,
    selected: GameDifficulty,
    onSelected: (GameDifficulty) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(horizontal = 8.dp)
    ) {
        items(items) { item ->
            val selectedColor by animateColorAsState(
                targetValue = if (selected == item) {
                    MaterialTheme.colorScheme.secondaryContainer
                } else {
                    MaterialTheme.colorScheme.surface
                }
            )
            ElevatedFilterChip(
                selected = selected == item,
                onClick = { onSelected(item) },
                label = {
                    Text(
                        if (item != GameDifficulty.Unspecified) {
                            stringResource(item.resName)
                        } else {
                            stringResource(R.string.statistics_difficulty_filter_all)
                        }
                    )
                },
                shape = RoundedCornerShape(16.dp),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = selectedColor,
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = FilterChipDefaults.elevatedFilterChipElevation(4.dp)
            )
        }
    }
}

data class RecordItemInfo(
    val time: Duration,
    val date: LocalDateTime,
    val difficulty: String,
    val type: String,
    val dateFormat: String
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun RecordItem(
    info: RecordItemInfo,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = { },
    onLongClick: () -> Unit = { }
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${info.difficulty} ${info.type}"
                )
                Spacer(modifier = Modifier.width(14.dp))
                Text(
                    text = stringResource(R.string.time) + ": ${DateUtils.formatElapsedTime(info.time.seconds)}"
                )
            }
            Row {
                Text(
                    text = info.date.format(AppSettingsManager.dateFormat(info.dateFormat))
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = info.date.format(DateTimeFormatter.ofPattern("HH:mm"))
                )
            }
        }
    }
}
