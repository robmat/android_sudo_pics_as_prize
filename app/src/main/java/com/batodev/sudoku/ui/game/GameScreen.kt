package com.batodev.sudoku.ui.game

import android.view.View
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Redo
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.batodev.sudoku.R
import com.batodev.sudoku.core.Cell
import com.batodev.sudoku.core.PreferencesConstants
import com.batodev.sudoku.ui.components.RoundedDropdownMenu
import com.batodev.sudoku.ui.onboarding.FirstGameDialog

private const val RESTART_BUTTON_ANIMATION_DURATION_MS = 250

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(
    navigateBack: () -> Unit,
    navigateSettings: () -> Unit,
    viewModel: GameViewModel,
) {
    val localView = LocalView.current // vibration
    GameFirstGameOverlay(viewModel)

    val restartButtonAngleState = remember { mutableFloatStateOf(0f) }
    val restartButtonAnimation: Float by animateFloatAsState(
        targetValue = restartButtonAngleState.floatValue,
        animationSpec = tween(durationMillis = RESTART_BUTTON_ANIMATION_DURATION_MS),
        label = "this_label_makes_no_sense_to_me_but_i_added_to_overcome_a_warning",
    )

    val resetTimer by viewModel.resetTimerOnRestart.collectAsStateWithLifecycle(
        initialValue = PreferencesConstants.DEFAULT_GAME_RESET_TIMER,
    )

    val mistakesLimit by viewModel.mistakesLimit.collectAsStateWithLifecycle(
        initialValue = PreferencesConstants.DEFAULT_MISTAKES_LIMIT,
    )

    val renderNotesState = remember { mutableStateOf(true) }

    Scaffold(
        topBar = {
            GameTopBar(viewModel, navigateBack, navigateSettings, restartButtonAnimation)
        },
    ) { scaffoldPaddings ->
        Box {
            GamePrizeImage(viewModel)
            Column(
                modifier =
                    Modifier
                        .padding(scaffoldPaddings)
                        .padding(horizontal = 12.dp),
                verticalArrangement = Arrangement.SpaceEvenly,
            ) {
                GameStatsRow(viewModel, mistakesLimit)
                GameBoardArea(viewModel, renderNotesState, localView)
                GameBottomContent(viewModel, renderNotesState, localView, mistakesLimit)
            }
        }
    }

    val keepScreenOn by viewModel.keepScreenOn.collectAsStateWithLifecycle(
        initialValue = PreferencesConstants.DEFAULT_KEEP_SCREEN_ON,
    )
    if (keepScreenOn) {
        KeepScreenOn()
    }

    // dialogs
    GameDialogs(viewModel, resetTimer, restartButtonAngleState)

    GameLifecycleEffects(viewModel)
}

@Composable
private fun GameFirstGameOverlay(viewModel: GameViewModel) {
    val firstGame by viewModel.firstGame.collectAsStateWithLifecycle(initialValue = false)
    if (firstGame) {
        viewModel.pauseTimer()
        FirstGameDialog(
            onFinished = {
                viewModel.setFirstGameFalse()
                viewModel.startTimer()
            },
        )
    }
}

@Composable
private fun GameLifecycleEffects(viewModel: GameViewModel) {
    LaunchedEffect(Unit) {
        if (!viewModel.endGame && !viewModel.gameCompleted) {
            viewModel.startTimer()
        }
    }

    LaunchedEffect(viewModel.gameCompleted) {
        if (viewModel.gameCompleted) {
            viewModel.onGameComplete()
            viewModel.endGame = true
        }
    }

    LaunchedEffect(viewModel.mistakesMethod) {
        viewModel.checkMistakesAll()
    }

    // so that the timer doesn't run in the background
    // https://stackoverflow.com/questions/66546962/jetpack-compose-how-do-i-refresh-a-screen-when-app-returns-to-foreground/66807899#66807899
    OnLifecycleEvent { _, event ->
        when (event) {
            Lifecycle.Event.ON_RESUME -> {
                if (viewModel.gamePlaying) viewModel.startTimer()
            }

            Lifecycle.Event.ON_PAUSE -> {
                viewModel.pauseTimer()
                viewModel.currCell = Cell(-1, -1, 0)
            }

            Lifecycle.Event.ON_DESTROY -> {
                viewModel.pauseTimer()
            }

            else -> {}
        }
    }
}

data class NotesMenuActions(
    val onDismiss: () -> Unit,
    val onComputeNotesClick: () -> Unit,
    val onClearNotesClick: () -> Unit,
    val onRenderNotesClick: () -> Unit,
)

@Composable
fun NotesMenu(
    expanded: Boolean,
    renderNotes: Boolean,
    actions: NotesMenuActions,
    modifier: Modifier = Modifier,
) {
    RoundedDropdownMenu(
        modifier = modifier,
        expanded = expanded,
        onDismissRequest = { actions.onDismiss() },
    ) {
        DropdownMenuItem(
            text = { Text(stringResource(R.string.action_compute_notes)) },
            onClick = {
                actions.onComputeNotesClick()
                actions.onDismiss()
            },
        )
        DropdownMenuItem(
            text = {
                Text(stringResource(R.string.action_clear_notes))
            },
            onClick = {
                actions.onClearNotesClick()
                actions.onDismiss()
            },
        )
        DropdownMenuItem(
            text = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(stringResource(R.string.action_show_notes))
                    Checkbox(
                        checked = renderNotes,
                        onCheckedChange = { actions.onRenderNotesClick() },
                    )
                }
            },
            onClick = actions.onRenderNotesClick,
        )
    }
}

@Composable
fun UndoRedoMenu(
    expanded: Boolean,
    onDismiss: () -> Unit,
    onRedoClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    RoundedDropdownMenu(
        modifier = modifier,
        expanded = expanded,
        onDismissRequest = { onDismiss() },
    ) {
        DropdownMenuItem(
            text = { Text(stringResource(R.string.redo)) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Rounded.Redo,
                    contentDescription = null,
                )
            },
            onClick = {
                onRedoClick()
                onDismiss()
            },
        )
    }
}

@Composable
fun GameMenu(
    expanded: Boolean,
    onDismiss: () -> Unit,
    onGiveUpClick: () -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    RoundedDropdownMenu(
        modifier = modifier,
        expanded = expanded,
        onDismissRequest = onDismiss,
    ) {
        DropdownMenuItem(
            text = { Text(stringResource(R.string.action_give_up)) },
            onClick = {
                onGiveUpClick()
                onDismiss()
            },
        )
        DropdownMenuItem(
            text = { Text(stringResource(R.string.settings_title)) },
            onClick = {
                onSettingsClick()
                onDismiss()
            },
        )
    }
}

@Composable
fun TopBoardSection(
    text: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 4.dp),
        )
    }
}

@Composable
fun OnLifecycleEvent(onEvent: (owner: LifecycleOwner, event: Lifecycle.Event) -> Unit) {
    val eventHandler = rememberUpdatedState(onEvent)
    val lifecycleOwner = rememberUpdatedState(LocalLifecycleOwner.current)

    DisposableEffect(lifecycleOwner.value) {
        val lifecycle = lifecycleOwner.value.lifecycle
        val observer =
            LifecycleEventObserver { owner, event ->
                eventHandler.value(owner, event)
            }

        lifecycle.addObserver(observer)
        onDispose {
            lifecycle.removeObserver(observer)
        }
    }
}

@Composable
fun KeepScreenOn() = AndroidView({ View(it).apply { keepScreenOn = true } })
