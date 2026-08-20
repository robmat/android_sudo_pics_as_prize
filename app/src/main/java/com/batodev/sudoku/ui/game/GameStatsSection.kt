package com.batodev.sudoku.ui.game

import android.text.format.DateUtils
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Cancel
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Grade
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.batodev.sudoku.R
import com.batodev.sudoku.core.PreferencesConstants
import com.batodev.sudoku.core.utils.toFormattedString
import com.batodev.sudoku.data.database.model.Record
import kotlin.time.toKotlinDuration

@Composable
private fun ColumnScope.AfterGameStatsTitle(info: AfterGameStatsInfo) {
    Text(
        text =
            if (info.giveUp) {
                if (info.mistakesLimit && info.mistakesLimitCount >= PreferencesConstants.MISTAKES_LIMIT) {
                    stringResource(R.string.saved_game_mistakes_limit)
                } else {
                    stringResource(R.string.saved_game_give_up)
                }
            } else {
                stringResource(R.string.game_completed)
            },
        style = MaterialTheme.typography.titleMedium,
        modifier =
            Modifier
                .align(Alignment.CenterHorizontally)
                .padding(bottom = 8.dp),
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AfterGameTimeSection(
    timeText: String,
    records: List<Record>,
) {
    Text(
        text = stringResource(R.string.time),
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.padding(bottom = 8.dp),
    )

    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        StatBoxWithBottomPadding(
            text = {
                Text(
                    stringResource(
                        R.string.stat_time_current,
                        timeText,
                    ),
                )
            },
        )

        if (records.isNotEmpty()) {
            StatBoxWithBottomPadding(
                text = {
                    Text(
                        text =
                            stringResource(
                                R.string.stat_time_average,
                                DateUtils.formatElapsedTime(records.sumOf { it.time.seconds } / records.count()),
                            ),
                    )
                },
            )
            StatBoxWithBottomPadding(
                text = {
                    Text(
                        text =
                            stringResource(
                                R.string.stat_time_best,
                                records
                                    .first()
                                    .time
                                    .toKotlinDuration()
                                    .toFormattedString(),
                            ),
                    )
                },
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AfterGameSummarySection(info: AfterGameStatsInfo) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        StatBoxWithBottomPadding(
            text = {
                Text(
                    "${stringResource(info.difficulty.resName)} ${
                        stringResource(
                            info.type.resName,
                        )
                    }",
                )
            },
            icon = { Icon(Icons.Rounded.Grade, contentDescription = null) },
        )
        StatBoxWithBottomPadding(
            text = {
                Text(
                    stringResource(
                        R.string.hints_used,
                        info.hintsUsed,
                    ),
                )
            },
            icon = { Icon(Icons.Rounded.Lightbulb, contentDescription = null) },
        )
        StatBoxWithBottomPadding(
            text = {
                Text(
                    stringResource(
                        R.string.mistakes_made,
                        info.mistakesMade,
                    ),
                )
            },
            icon = { Icon(Icons.Rounded.Cancel, contentDescription = null) },
        )
        StatBoxWithBottomPadding(
            text = {
                Text(
                    stringResource(
                        R.string.notes_taken,
                        info.notesTaken,
                    ),
                )
            },
            icon = { Icon(Icons.Rounded.Edit, contentDescription = null) },
        )
    }
}

@Composable
fun AfterGameStats(
    info: AfterGameStatsInfo,
    modifier: Modifier = Modifier,
) {
    Column(modifier) {
        AfterGameStatsTitle(info)

        if (!info.giveUp) {
            AfterGameTimeSection(info.timeText, info.records)
        }

        Text(
            text = stringResource(R.string.statistics),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(top = 12.dp, bottom = 8.dp),
        )
        AfterGameSummarySection(info)
    }
}

@Composable
fun StatBox(
    text: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    icon: @Composable () -> Unit = { },
) {
    Box(
        modifier =
            modifier
                .clip(MaterialTheme.shapes.medium)
                .background(MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Row(
            modifier = Modifier.padding(vertical = 6.dp, horizontal = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            icon()
            text()
        }
    }
}

// Workaround: FlowRow does not yet support cross-axis arrangement, so bottom padding is
// applied manually to each item instead. Tracked upstream at:
// https://android-review.googlesource.com/c/platform/frameworks/support/+/2478295
// Once Compose Foundation adds cross-axis arrangement support to FlowRow, this wrapper
// can be replaced by passing that arrangement directly to FlowRow and StatBox can be
// used everywhere instead.
@Composable
fun StatBoxWithBottomPadding(
    text: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    icon: @Composable () -> Unit = { },
) {
    StatBox(
        text = text,
        icon = icon,
        modifier = modifier.padding(bottom = 8.dp),
    )
}
