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
import com.batodev.sudoku.core.Cell
import com.batodev.sudoku.core.PreferencesConstants

internal const val ROTATE_ICON_FULL_DEGREES = 360f

@Composable
internal fun ShowSolutionAction(viewModel: GameViewModel) {
    val reachedMistakesLimitOrGaveUp =
        viewModel.mistakesCount >= PreferencesConstants.MISTAKES_LIMIT ||
            viewModel.giveUp
    AnimatedVisibility(
        visible = viewModel.endGame && reachedMistakesLimitOrGaveUp,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            FilledTonalButton(
                onClick = { viewModel.showSolution = !viewModel.showSolution },
            ) {
                AnimatedContent(
                    if (viewModel.showSolution) {
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
internal fun PlayPauseAction(viewModel: GameViewModel) {
    AnimatedVisibility(visible = !viewModel.endGame) {
        val rotationAngle by animateFloatAsState(
            targetValue = if (viewModel.gamePlaying) 0f else ROTATE_ICON_FULL_DEGREES,
            label = "this_label_makes_no_sense_to_me_but_i_added_to_overcome_a_warning",
        )
        IconButton(onClick = {
            if (!viewModel.gamePlaying) viewModel.startTimer() else viewModel.pauseTimer()
            viewModel.currCell = Cell(-1, -1, 0)
        }) {
            Icon(
                modifier = Modifier.rotate(rotationAngle),
                painter =
                    painterResource(
                        if (viewModel.gamePlaying) {
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
    viewModel: GameViewModel,
    restartButtonAnimation: Float,
) {
    AnimatedVisibility(visible = !viewModel.endGame) {
        IconButton(
            onClick = { viewModel.restartDialog = true },
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
    viewModel: GameViewModel,
    navigateSettings: () -> Unit,
) {
    AnimatedVisibility(visible = !viewModel.endGame) {
        Box {
            IconButton(onClick = { viewModel.showMenu = !viewModel.showMenu }) {
                Icon(
                    Icons.Default.MoreVert,
                    contentDescription = null,
                )
            }
            GameMenu(
                expanded = viewModel.showMenu,
                onDismiss = { viewModel.showMenu = false },
                onGiveUpClick = {
                    viewModel.pauseTimer()
                    viewModel.giveUpDialog = true
                },
                onSettingsClick = {
                    navigateSettings()
                    viewModel.showMenu = false
                },
            )
        }
    }
}
