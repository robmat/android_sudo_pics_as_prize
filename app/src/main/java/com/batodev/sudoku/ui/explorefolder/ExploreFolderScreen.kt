package com.batodev.sudoku.ui.explorefolder

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.DriveFileMove
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.rounded.AddCircleOutline
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.EditOff
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.batodev.sudoku.LocalBoardColors
import com.batodev.sudoku.R
import com.batodev.sudoku.core.utils.toFormattedString
import com.batodev.sudoku.data.database.model.Folder
import com.batodev.sudoku.data.database.model.SavedGame
import com.batodev.sudoku.data.database.model.SudokuBoard
import com.batodev.sudoku.ui.components.BackIconButton
import com.batodev.sudoku.ui.components.CanContinueBadge
import com.batodev.sudoku.ui.components.EdgeIndicatedLazyColumn
import com.batodev.sudoku.ui.components.OverflowMenuButton
import com.batodev.sudoku.ui.components.board.BoardPreview
import com.batodev.sudoku.ui.components.board.BoardPreviewContent
import kotlin.math.sqrt
import kotlin.time.toKotlinDuration

data class ExploreFolderNavigation(
    val navigateBack: () -> Unit,
    val navigatePlayGame: (Triple<Long, Boolean, Long>) -> Unit,
    val navigateImportFromFile: (Pair<String, Long>) -> Unit,
    val navigateEditGame: (Pair<Long, Long>) -> Unit,
    val navigateCreateSudoku: (Long) -> Unit,
)

@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalAnimationApi::class,
    ExperimentalFoundationApi::class,
)
@Composable
fun ExploreFolderScreen(
    viewModel: ExploreFolderViewModel,
    navigation: ExploreFolderNavigation,
) {
    val coroutineScope = rememberCoroutineScope()
    val lazyListState = rememberLazyListState()

    val dialogState =
        ExploreFolderDialogState(
            addSudokuBottomSheet = rememberSaveable { mutableStateOf(false) },
            moveSelectedDialog = rememberSaveable { mutableStateOf(false) },
            deleteBoardDialog = rememberSaveable { mutableStateOf(false) },
            // used for a delete dialog when deleting
            deleteBoardDialogBoard = remember { mutableStateOf<SudokuBoard?>(null) },
        )

    val folders by viewModel.folders.collectAsStateWithLifecycle(initialValue = emptyList())
    val folder by viewModel.folder.collectAsStateWithLifecycle(null)
    val games by viewModel.games.collectAsStateWithLifecycle(emptyMap())

    var contentUri by remember { mutableStateOf<Uri?>(null) }
    val openDocumentLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.OpenDocument(),
            onResult = {
                contentUri = it
            },
        )

    LaunchedEffect(contentUri) {
        contentUri?.let { uri ->
            folder?.let { folder ->
                navigation.navigateImportFromFile(Pair(Uri.encode(uri.toString()), folder.uid))
            }
        }
    }

    BackHandler(viewModel.inSelectionMode) {
        viewModel.inSelectionMode = false
    }

    Scaffold(
        topBar = { ExploreFolderTopBar(viewModel, folder, games, navigation.navigateBack, dialogState) },
        floatingActionButton = { ExploreFolderFab(lazyListState, coroutineScope) },
    ) { paddingValues ->
        ExploreFolderBody(
            viewModel,
            ExploreFolderListState(folder, games, lazyListState),
            paddingValues,
            navigation,
            dialogState,
        )
    }

    ExploreFolderEffects(viewModel, folder, navigation.navigatePlayGame)

    ExploreFolderDeleteDialog(viewModel, dialogState)
    if (!dialogState.deleteBoardDialog.value && dialogState.moveSelectedDialog.value) {
        MoveSudokuToFolderDialog(
            availableFolders = folders.filter { it != folder },
            onDismiss = { dialogState.moveSelectedDialog.value = false },
            onConfirmMove = { folderUid -> viewModel.moveBoards(folderUid) },
        )
    }

    ExploreFolderAddSheet(dialogState, folder, navigation.navigateCreateSudoku, openDocumentLauncher)
}

private const val EXPAND_ANIMATION_DURATION_MS = 200

data class GameInFolderInfo(
    val board: String,
    val difficulty: String,
    val type: String,
    val gameId: Long,
    val savedGame: SavedGame?,
)

data class GameInFolderActions(
    val onClick: () -> Unit,
    val onPlayClick: () -> Unit,
    val onEditClick: () -> Unit,
    val onDeleteClick: () -> Unit,
    val onLongClick: () -> Unit = { },
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun GameInFolderWidget(
    info: GameInFolderInfo,
    actions: GameInFolderActions,
    expanded: Boolean,
    selected: Boolean,
    modifier: Modifier = Modifier,
) {
    ElevatedCard(
        modifier =
            modifier
                .clip(CardDefaults.elevatedShape)
                .combinedClickable(onClick = actions.onClick, onLongClick = actions.onLongClick),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
        ) {
            AnimatedVisibility(
                visible = selected,
                enter = fadeIn() + expandHorizontally(clip = false),
                exit = fadeOut() + shrinkHorizontally(clip = false),
                modifier = Modifier.align(Alignment.CenterVertically),
            ) {
                Icon(
                    imageVector = Icons.Rounded.CheckCircle,
                    tint = MaterialTheme.colorScheme.primary,
                    contentDescription = null,
                    modifier = Modifier.padding(end = 12.dp),
                )
            }
            Column {
                GameInFolderSummary(info)
                AnimatedVisibility(
                    visible = expanded,
                    modifier = Modifier.animateContentSize(),
                    enter =
                        slideInVertically(tween(EXPAND_ANIMATION_DURATION_MS)) +
                            expandVertically(tween(EXPAND_ANIMATION_DURATION_MS)),
                    exit =
                        slideOutVertically(tween(EXPAND_ANIMATION_DURATION_MS)) +
                            shrinkVertically(tween(EXPAND_ANIMATION_DURATION_MS)),
                ) {
                    GameInFolderExpandedActions(info.savedGame, actions)
                }
            }
        }
    }
}

@Composable
private fun GameInFolderSummary(info: GameInFolderInfo) {
    Row {
        Box(
            modifier =
                Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .size(130.dp),
        ) {
            BoardPreview(
                content =
                    BoardPreviewContent(
                        size = sqrt(info.board.length.toFloat()).toInt(),
                        boardString = info.board,
                        boardColors = LocalBoardColors.current,
                    ),
            )
        }
        Column(
            modifier =
                Modifier
                    .padding(horizontal = 12.dp),
        ) {
            Text("${info.difficulty} ${info.type}")

            val savedGame = info.savedGame
            if (savedGame != null) {
                Text(
                    stringResource(
                        R.string.saved_game_time,
                        savedGame.timer
                            .toKotlinDuration()
                            .toFormattedString(),
                    ),
                )
            } else {
                Text(stringResource(R.string.game_not_started))
            }

            Text(stringResource(R.string.game_id, info.gameId))

            if (savedGame != null && savedGame.canContinue) {
                Spacer(modifier = Modifier.height(12.dp))
                CanContinueBadge()
            }
        }
    }
}

@Composable
private fun GameInFolderExpandedActions(
    savedGame: SavedGame?,
    actions: GameInFolderActions,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceAround,
    ) {
        IconWithText(
            imageVector = Icons.Rounded.PlayArrow,
            text =
                if (savedGame == null || !savedGame.canContinue) {
                    stringResource(R.string.action_play)
                } else {
                    stringResource(R.string.action_continue)
                },
            onClick = actions.onPlayClick,
            enabled = savedGame?.canContinue ?: true,
        )
        IconWithText(
            imageVector = if (savedGame == null) Icons.Rounded.Edit else Icons.Rounded.EditOff,
            text = stringResource(R.string.action_edit),
            onClick = actions.onEditClick,
            enabled = savedGame == null,
        )
        IconWithText(
            imageVector = Icons.Outlined.Delete,
            text = stringResource(R.string.action_delete),
            onClick = actions.onDeleteClick,
        )
    }
}

@Composable
private fun IconWithText(
    imageVector: ImageVector,
    text: String,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        IconButton(
            onClick = onClick,
            enabled = enabled,
        ) {
            Icon(imageVector, contentDescription = null)
        }
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun DefaultTopAppBar(
    title: @Composable () -> Unit,
    navigateBack: () -> Unit,
    onImportMenuClick: () -> Unit,
) {
    TopAppBar(
        title = title,
        navigationIcon = {
            BackIconButton(onClick = navigateBack)
        },
        actions = {
            OverflowMenuButton { closeMenu ->
                DropdownMenuItem(
                    leadingIcon = {
                        Icon(
                            Icons.Rounded.AddCircleOutline,
                            contentDescription = null,
                        )
                    },
                    text = {
                        Text(stringResource(R.string.explore_folder_add_sudoku))
                    },
                    onClick = {
                        onImportMenuClick()
                        closeMenu()
                    },
                )
            }
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SelectionTopAppbar(
    title: @Composable () -> Unit,
    onCloseClick: () -> Unit,
    onClickMoveSelected: () -> Unit,
    onClickDeleteSelected: () -> Unit,
    onClickSelectAll: () -> Unit,
) {
    TopAppBar(
        title = title,
        navigationIcon = {
            IconButton(onClick = onCloseClick) {
                Icon(Icons.Rounded.Close, contentDescription = null)
            }
        },
        actions = {
            IconButton(onClick = onClickMoveSelected) {
                Icon(
                    imageVector = Icons.Outlined.DriveFileMove,
                    contentDescription = null,
                )
            }
            IconButton(onClick = onClickDeleteSelected) {
                Icon(
                    imageVector = Icons.Rounded.Delete,
                    contentDescription = null,
                )
            }
            IconButton(onClick = onClickSelectAll) {
                Icon(
                    painterResource(R.drawable.ic_outline_select_all_24),
                    contentDescription = null,
                )
            }
        },
        colors =
            TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(6.dp),
            ),
    )
}

@Composable
private fun MoveSudokuToFolderDialog(
    availableFolders: List<Folder>,
    onDismiss: () -> Unit,
    onConfirmMove: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    AlertDialog(
        modifier = modifier,
        icon = { Icon(Icons.Outlined.DriveFileMove, contentDescription = null) },
        title = { Text(stringResource(R.string.action_move_selected)) },
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_cancel))
            }
        },
        text = {
            Column {
                Text(
                    text = stringResource(R.string.move_games_to_folder_subtitle),
                    modifier = Modifier.padding(bottom = 8.dp),
                )
                EdgeIndicatedLazyColumn {
                    items(availableFolders) { folder ->
                        Row(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 48.dp)
                                    .clip(MaterialTheme.shapes.small)
                                    .clickable {
                                        onConfirmMove(folder.uid)
                                        onDismiss()
                                    },
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Folder,
                                contentDescription = null,
                                modifier = Modifier.padding(horizontal = 12.dp),
                            )
                            Text(
                                text = folder.name,
                                style = MaterialTheme.typography.bodyLarge,
                            )
                        }
                    }
                }
            }
        },
    )
}
