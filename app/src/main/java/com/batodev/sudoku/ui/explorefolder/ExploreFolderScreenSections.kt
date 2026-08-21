package com.batodev.sudoku.ui.explorefolder

import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Create
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.NoteAdd
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.batodev.sudoku.R
import com.batodev.sudoku.data.database.model.Folder
import com.batodev.sudoku.data.database.model.SavedGame
import com.batodev.sudoku.data.database.model.SudokuBoard
import com.batodev.sudoku.ui.components.EmptyScreen
import com.batodev.sudoku.ui.components.LazyColumnBehavior
import com.batodev.sudoku.ui.components.ScrollbarLazyColumn
import com.batodev.sudoku.ui.util.isScrolledToStart
import com.batodev.sudoku.ui.util.isScrollingUp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalAnimationApi::class)
@Composable
internal fun ExploreFolderTopBar(
    inSelectionMode: Boolean,
    selectedCount: Int,
    folder: Folder?,
    navigateBack: () -> Unit,
    dialogState: ExploreFolderDialogState,
    onCloseSelectionMode: () -> Unit,
    onSelectAllClick: () -> Unit,
) {
    AnimatedContent(inSelectionMode) { inSelection ->
        if (inSelection) {
            SelectionTopAppbar(
                title = { Text(selectedCount.toString()) },
                onCloseClick = onCloseSelectionMode,
                onClickMoveSelectedItems = { dialogState.moveSelectedDialog.value = true },
                onClickDeleteSelectedItems = { dialogState.deleteBoardDialog.value = true },
                onClickSelectAll = onSelectAllClick,
            )
        } else {
            DefaultTopAppBar(
                title = {
                    folder?.let {
                        Text(
                            text = it.name,
                            modifier = Modifier.basicMarquee(),
                        )
                    }
                },
                navigateBack = navigateBack,
                onImportMenuClick = {
                    dialogState.addSudokuBottomSheet.value = true
                },
            )
        }
    }
}

@Composable
internal fun ExploreFolderFab(
    lazyListState: LazyListState,
    coroutineScope: CoroutineScope,
) {
    AnimatedVisibility(
        visible = lazyListState.isScrollingUp() && !lazyListState.isScrolledToStart(),
        enter = fadeIn() + scaleIn(),
        exit = fadeOut() + scaleOut(),
    ) {
        FloatingActionButton(
            onClick = {
                coroutineScope.launch { lazyListState.animateScrollToItem(0) }
            },
        ) {
            Icon(Icons.Rounded.KeyboardArrowUp, contentDescription = null)
        }
    }
}

/** The values [ExploreFolderGamesList] needs; read once from [ExploreFolderViewModel] by [ExploreFolderScreen]. */
internal data class ExploreFolderSelectionState(
    val inSelectionMode: Boolean,
    val selectedBoardsList: List<SudokuBoard>,
)

/** The callbacks [ExploreFolderGamesList] needs; constructed once by [ExploreFolderScreen]. */
internal data class ExploreFolderGamesListActions(
    val onEnterSelectionMode: (SudokuBoard) -> Unit,
    val onAddToSelection: (SudokuBoard) -> Unit,
    val onPrepareSudokuToPlay: (SudokuBoard) -> Unit,
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun ExploreFolderBody(
    listState: ExploreFolderListState,
    selectionState: ExploreFolderSelectionState,
    gamesListActions: ExploreFolderGamesListActions,
    paddingValues: PaddingValues,
    navigation: ExploreFolderNavigation,
    dialogState: ExploreFolderDialogState,
) {
    val folder = listState.folder
    Column(Modifier.padding(paddingValues)) {
        if (folder != null && listState.games.isNotEmpty()) {
            ExploreFolderGamesList(listState, selectionState, gamesListActions, navigation, dialogState)
        } else if (folder != null) {
            EmptyScreen(
                text = stringResource(R.string.folder_empty_label),
                content = {
                    Button(onClick = {
                        dialogState.addSudokuBottomSheet.value = true
                    }) {
                        Icon(Icons.Rounded.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(stringResource(R.string.add_to_folder))
                    }
                },
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ExploreFolderGamesList(
    listState: ExploreFolderListState,
    selectionState: ExploreFolderSelectionState,
    actions: ExploreFolderGamesListActions,
    navigation: ExploreFolderNavigation,
    dialogState: ExploreFolderDialogState,
) {
    val folder = requireNotNull(listState.folder)
    var expandedGameUid by rememberSaveable { mutableLongStateOf(-1L) }

    LaunchedEffect(selectionState.inSelectionMode) {
        if (selectionState.inSelectionMode) expandedGameUid = -1L
    }

    ScrollbarLazyColumn(
        state = listState.lazyListState,
        behavior = LazyColumnBehavior(verticalArrangement = Arrangement.spacedBy(8.dp)),
    ) {
        items(
            items = listState.games.toList(),
            key = { it.first.uid },
        ) { game ->
            GameInFolderWidget(
                modifier =
                    Modifier
                        .padding(horizontal = 12.dp)
                        .animateItem(),
                info =
                    GameInFolderInfo(
                        board = game.second?.currentBoard ?: game.first.initialBoard,
                        difficulty = stringResource(game.first.difficulty.resName),
                        type = stringResource(game.first.type.resName),
                        gameId = game.first.uid,
                        savedGame = game.second,
                    ),
                expanded = expandedGameUid == game.first.uid,
                selected = selectionState.selectedBoardsList.contains(game.first),
                actions =
                    GameInFolderActions(
                        onClick = {
                            if (!selectionState.inSelectionMode) {
                                expandedGameUid =
                                    if (expandedGameUid != game.first.uid) game.first.uid else -1L
                            } else {
                                actions.onAddToSelection(game.first)
                            }
                        },
                        onLongClick = {
                            actions.onEnterSelectionMode(game.first)
                        },
                        onPlayClick = { actions.onPrepareSudokuToPlay(game.first) },
                        onEditClick = {
                            navigation.navigateEditGame(Pair(game.first.uid, folder.uid))
                        },
                        onDeleteClick = {
                            dialogState.deleteBoardDialogBoard.value = game.first
                            dialogState.deleteBoardDialog.value = true
                        },
                    ),
            )
        }
    }
}

internal data class ExploreFolderEffectsState(
    val readyToPlay: Boolean,
    val gameUidToPlay: Long?,
    val isPlayedBefore: Boolean,
    val folder: Folder?,
    val inSelectionMode: Boolean,
    val selectedBoardsList: List<SudokuBoard>,
)

internal data class ExploreFolderEffectsActions(
    val navigatePlayGame: (Triple<Long, Boolean, Long>) -> Unit,
    val onReadyToPlayHandled: () -> Unit,
    val onExitSelectionMode: () -> Unit,
    val onClearSelection: () -> Unit,
)

@Composable
internal fun ExploreFolderEffects(
    state: ExploreFolderEffectsState,
    actions: ExploreFolderEffectsActions,
) {
    val currentActions by rememberUpdatedState(actions)
    LaunchedEffect(state.readyToPlay, state.gameUidToPlay) {
        if (state.readyToPlay) {
            state.gameUidToPlay?.let {
                currentActions.navigatePlayGame(Triple(it, state.isPlayedBefore, state.folder!!.uid))
                currentActions.onReadyToPlayHandled()
            }
        }
    }

    LaunchedEffect(state.selectedBoardsList) {
        if (state.selectedBoardsList.isEmpty()) {
            currentActions.onExitSelectionMode()
        }
    }

    LaunchedEffect(state.inSelectionMode) {
        if (!state.inSelectionMode) {
            currentActions.onClearSelection()
        }
    }
}

@Composable
internal fun ExploreFolderDeleteDialog(
    selectedCount: Int,
    dialogState: ExploreFolderDialogState,
    onDeleteGame: (SudokuBoard) -> Unit,
    onDeleteSelectedItems: () -> Unit,
) {
    if (!dialogState.deleteBoardDialog.value) return
    val deleteBoardDialogBoard = dialogState.deleteBoardDialogBoard
    AlertDialog(
        icon = { Icon(Icons.Outlined.Delete, contentDescription = null) },
        title = { Text(stringResource(R.string.dialog_delete_selected)) },
        text = {
            Text(
                text =
                    pluralStringResource(
                        id = R.plurals.delete_selected_in_folder,
                        count = if (deleteBoardDialogBoard.value != null) 1 else selectedCount,
                        if (deleteBoardDialogBoard.value != null) 1 else selectedCount,
                    ),
            )
        },
        onDismissRequest = { dialogState.deleteBoardDialog.value = false },
        dismissButton = {
            TextButton(onClick = { dialogState.deleteBoardDialog.value = false }) {
                Text(stringResource(R.string.action_cancel))
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (deleteBoardDialogBoard.value != null) {
                    deleteBoardDialogBoard.value?.let { gameToDelete ->
                        onDeleteGame(gameToDelete)
                        deleteBoardDialogBoard.value = null
                    }
                } else {
                    onDeleteSelectedItems()
                }
                dialogState.deleteBoardDialog.value = false
            }) {
                Text(stringResource(R.string.action_delete))
            }
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ExploreFolderAddSheet(
    dialogState: ExploreFolderDialogState,
    folder: Folder?,
    navigateCreateSudoku: (Long) -> Unit,
    openDocumentLauncher: ManagedActivityResultLauncher<Array<String>, android.net.Uri?>,
) {
    if (!dialogState.addSudokuBottomSheet.value) return
    ModalBottomSheet(onDismissRequest = { dialogState.addSudokuBottomSheet.value = false }) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
        ) {
            Text(
                text = stringResource(R.string.add_to_folder),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.align(Alignment.CenterHorizontally),
            )
            Spacer(modifier = Modifier.height(12.dp))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                val options =
                    listOf(
                        AddSheetOption(stringResource(R.string.add_to_folder_create_new), Icons.Outlined.Create) {
                            folder?.let { navigateCreateSudoku(it.uid) }
                        },
                        AddSheetOption(stringResource(R.string.add_to_folder_from_file), Icons.Outlined.NoteAdd) {
                            openDocumentLauncher.launch(arrayOf("*/*"))
                        },
                    )
                options.forEach { option ->
                    ExploreFolderAddSheetItem(option) { dialogState.addSudokuBottomSheet.value = false }
                }
            }
        }
    }
}

private data class AddSheetOption(
    val label: String,
    val icon: ImageVector,
    val onSelect: () -> Unit,
)

@Composable
private fun ExploreFolderAddSheetItem(
    option: AddSheetOption,
    onDismiss: () -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.small)
                .clickable {
                    option.onSelect()
                    onDismiss()
                },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = option.icon,
            contentDescription = null,
            modifier = Modifier.padding(12.dp),
        )
        Text(
            text = option.label,
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}
