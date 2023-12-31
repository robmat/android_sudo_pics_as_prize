package com.batodev.sudoku.ui.home

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.batodev.sudoku.R
import com.batodev.sudoku.ui.theme.SudokuBoardColorsImpl
import kotlinx.coroutines.runBlocking

val LocalBoardColors = staticCompositionLocalOf { SudokuBoardColorsImpl() }

@Composable
fun HomeScreen(
    navigatePlayGame: (Pair<Long, Boolean>) -> Unit,
    viewModel: HomeViewModel,
) {
    var continueGameDialog by rememberSaveable { mutableStateOf(false) }
    Box {
        val context = LocalContext.current
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceAround
        ) {

            val lastGame by viewModel.lastSavedGame.collectAsStateWithLifecycle()

            if (viewModel.readyToPlay) {
                viewModel.readyToPlay = false

                runBlocking {
                    //viewModel.saveToDatabase()
                    val saved = lastGame?.completed ?: false
                    navigatePlayGame(Pair(viewModel.insertedBoardUid, saved))
                }
            }

            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.headlineLarge
            )
            Button(
                onClick = {
                    context.startActivity(
                        Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("https://play.google.com/store/apps/dev?id=8228670503574649511")
                        )
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                shape = RectangleShape,
                modifier = Modifier
                    .border(
                        1.dp,
                        LocalBoardColors.current.thinLineColor,
                        RoundedCornerShape(8.dp)
                    )
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Image(
                        painter = painterResource(id = R.drawable.emberfox),
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .height(130.dp),
                        contentDescription = stringResource(id = R.string.app_name)
                    )
                    Text(
                        text = stringResource(id = R.string.more_games_like_this),
                        style = MaterialTheme.typography.titleMedium,
                        color = LocalBoardColors.current.thinLineColor
                    )
                }
            }
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                HorizontalPicker(
                    text = stringResource(viewModel.selectedDifficulty.resName),
                    onLeftClick = { viewModel.changeDifficulty(-1) },
                    onRightClick = { viewModel.changeDifficulty(1) }
                )
                HorizontalPicker(
                    text = stringResource(viewModel.selectedType.resName),
                    onLeftClick = { viewModel.changeType(-1) },
                    onRightClick = { viewModel.changeType(1) }
                )

                Spacer(Modifier.height(12.dp))

                if (lastGame != null && !lastGame!!.completed) {
                    Button(onClick = {
                        lastGame?.let {
                            navigatePlayGame(Pair(it.uid, true))
                        }
                    }) {
                        Text(stringResource(R.string.action_continue))
                    }
                    FilledTonalButton(onClick = {
                        continueGameDialog = true
                    }) {
                        Text(stringResource(R.string.action_play))
                    }
                } else {
                    Button(onClick = {
                        viewModel.giveUpLastGame()
                        viewModel.startGame()
                    }) {
                        Text(stringResource(R.string.action_play))
                    }
                }
            }
        }


        if (viewModel.isGenerating || viewModel.isSolving) {
            GeneratingDialog(
                onDismiss = { },
                text = when {
                    viewModel.isGenerating -> stringResource(R.string.dialog_generating)
                    viewModel.isSolving -> stringResource(R.string.dialog_solving)
                    else -> ""
                }
            )
        }

        if (continueGameDialog) {
            AlertDialog(
                title = { Text(stringResource(R.string.dialog_new_game)) },
                text = { Text(stringResource(R.string.dialog_new_game_text)) },
                confirmButton = {
                    TextButton(onClick = {
                        continueGameDialog = false
                        viewModel.giveUpLastGame()
                        viewModel.startGame()
                    }) {
                        Text(stringResource(R.string.dialog_new_game_positive))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { continueGameDialog = false }) {
                        Text(stringResource(R.string.action_cancel))
                    }
                },
                onDismissRequest = {
                    continueGameDialog = false
                }
            )
        }

        LaunchedEffect(
            viewModel.lastSelectedGameDifficultyType,
            viewModel.saveSelectedGameDifficultyType
        ) {
            viewModel.restoreDifficultyAndType()
        }
    }
}

@Composable
fun GeneratingDialog(
    onDismiss: () -> Unit,
    text: String,
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surfaceColorAtElevation(6.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.padding(24.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Column {
                        Text(
                            text = text,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                    Column(
                        modifier = Modifier.padding(top = 16.dp)
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun HorizontalPicker(
    modifier: Modifier = Modifier,
    text: String,
    onLeftClick: () -> Unit,
    onRightClick: () -> Unit,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 36.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(onClick = onLeftClick) {
            Icon(
                painter = painterResource(R.drawable.ic_round_keyboard_arrow_left_24),
                contentDescription = null
            )
        }
        AnimatedContent(
            targetState = text,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "this_label_makes_no_sense_to_me_but_i_added_to_overcome_a_warning"
        ) { text ->
            Text(text)
        }
        IconButton(onClick = onRightClick) {
            Icon(
                painter = painterResource(R.drawable.ic_round_keyboard_arrow_right_24),
                contentDescription = null
            )
        }
    }
}