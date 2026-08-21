package com.batodev.sudoku.ui.game

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.batodev.sudoku.R

internal const val ROTATE_ICON_FULL_DEGREES = 360f

@Composable
internal fun ShowSolutionAction(
    visible: Boolean,
    showSolution: Boolean,
    onToggleShowSolution: () -> Unit,
) {
    AnimatedVisibility(
        visible = visible,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            FilledTonalButton(
                onClick = onToggleShowSolution,
            ) {
                AnimatedContent(
                    if (showSolution) {
                        stringResource(R.string.action_show_mine_sudoku)
                    } else {
                        stringResource(R.string.action_show_solution)
                    },
                    label = "this_label_makes_no_sense_to_me_but_i_added_to_overcome_a_warning",
                ) {
                    Text(it)
                }
            }
        }
    }
}

@Composable
internal fun PlayPauseAction(
    visible: Boolean,
    gamePlaying: Boolean,
    onPlayPauseToggle: () -> Unit,
) {
    AnimatedVisibility(visible = visible) {
        val rotationAngle by animateFloatAsState(
            targetValue = if (gamePlaying) 0f else ROTATE_ICON_FULL_DEGREES,
            label = "this_label_makes_no_sense_to_me_but_i_added_to_overcome_a_warning",
        )
        IconButton(onClick = onPlayPauseToggle) {
            Icon(
                modifier = Modifier.rotate(rotationAngle),
                painter =
                    painterResource(
                        if (gamePlaying) {
                            R.drawable.ic_round_pause_24
                        } else {
                            R.drawable.ic_round_play_24
                        },
                    ),
                contentDescription = null,
            )
        }
    }
}

@Composable
internal fun RestartAction(
    visible: Boolean,
    restartButtonAnimation: Float,
    onRestartClick: () -> Unit,
) {
    AnimatedVisibility(visible = visible) {
        IconButton(
            onClick = onRestartClick,
            modifier = Modifier.testTag("game_restart"),
        ) {
            Icon(
                modifier = Modifier.rotate(restartButtonAnimation),
                painter = painterResource(R.drawable.ic_round_replay_24),
                contentDescription = null,
            )
        }
    }
}

@Composable
internal fun GameMenuAction(
    visible: Boolean,
    showMenu: Boolean,
    onToggleMenu: () -> Unit,
    onDismissMenu: () -> Unit,
    onGiveUpClick: () -> Unit,
    onSettingsClick: () -> Unit,
) {
    AnimatedVisibility(visible = visible) {
        Box {
            IconButton(onClick = onToggleMenu) {
                Icon(
                    Icons.Default.MoreVert,
                    contentDescription = null,
                )
            }
            GameMenu(
                expanded = showMenu,
                onDismiss = onDismissMenu,
                onGiveUpClick = onGiveUpClick,
                onSettingsClick = onSettingsClick,
            )
        }
    }
}
