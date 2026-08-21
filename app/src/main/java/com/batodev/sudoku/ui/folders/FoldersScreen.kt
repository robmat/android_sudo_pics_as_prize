package com.batodev.sudoku.ui.folders

import android.net.Uri
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AddCircleOutline
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.CreateNewFolder
import androidx.compose.material.icons.rounded.Help
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.batodev.sudoku.LocalBoardColors
import com.batodev.sudoku.R
import com.batodev.sudoku.data.database.model.Folder
import com.batodev.sudoku.data.database.model.SavedGame
import com.batodev.sudoku.ui.components.BackIconButton
import com.batodev.sudoku.ui.components.OverflowMenuButton
import com.batodev.sudoku.ui.components.ScrollbarLazyColumn
import com.batodev.sudoku.ui.components.board.BoardPreview
import com.batodev.sudoku.ui.components.board.BoardPreviewContent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.math.sqrt

/** The confirm/import/create-folder actions offered by the [FoldersTopBar]. */
private data class FoldersTopBarActions(
    val navigateBack: () -> Unit,
    val onHelpClick: () -> Unit,
    val onImportClick: () -> Unit,
    val onCreateFolderClick: () -> Unit,
)

@Composable
private fun RowScope.FoldersTopBarMenuActions(actions: FoldersTopBarActions) {
    IconButton(onClick = actions.onHelpClick) {
        Icon(Icons.Rounded.Help, contentDescription = null)
    }
    OverflowMenuButton { closeMenu ->
        DropdownMenuItem(
            leadingIcon = {
                Icon(Icons.Rounded.AddCircleOutline, contentDescription = null)
            },
            text = {
                Text(stringResource(R.string.folder_import))
            },
            onClick = {
                actions.onImportClick()
                closeMenu()
            },
        )
        DropdownMenuItem(
            leadingIcon = {
                Icon(Icons.Rounded.CreateNewFolder, contentDescription = null)
            },
            text = {
                Text(stringResource(R.string.create_folder))
            },
            onClick = {
                actions.onCreateFolderClick()
                closeMenu()
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FoldersDefaultTopBar(actions: FoldersTopBarActions) {
    TopAppBar(
        title = {
            Text(stringResource(R.string.title_folders))
        },
        navigationIcon = {
            BackIconButton(onClick = actions.navigateBack)
        },
        actions = { FoldersTopBarMenuActions(actions) },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FoldersImportModeTopBar(
    gamesToImportSize: Int,
    navigateBack: () -> Unit,
) {
    TopAppBar(
        title = {
            Text(
                text =
                    pluralStringResource(
                        R.plurals.number_puzzles_to_import,
                        gamesToImportSize,
                    ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        },
        navigationIcon = {
            IconButton(onClick = navigateBack) {
                Icon(
                    imageVector = Icons.Rounded.Close,
                    contentDescription = null,
                )
            }
        },
    )
}

@Composable
private fun FoldersTopBar(
    gamesToImportEmpty: Boolean,
    gamesToImportSize: Int,
    actions: FoldersTopBarActions,
) {
    if (gamesToImportEmpty) {
        FoldersDefaultTopBar(actions)
    } else {
        FoldersImportModeTopBar(gamesToImportSize, actions.navigateBack)
    }
}

@Composable
private fun FoldersLastPlayedSection(
    lastGames: List<SavedGame>,
    navigateViewSavedGame: (Long) -> Unit,
) {
    Column(Modifier.padding(vertical = 6.dp)) {
        Text(
            text = stringResource(R.string.last_played_section_title),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(start = 12.dp, bottom = 6.dp),
        )
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(start = 12.dp, end = 12.dp),
        ) {
            items(lastGames) {
                ElevatedCard(
                    modifier =
                        Modifier
                            .clip(CardDefaults.elevatedShape)
                            .clickable { navigateViewSavedGame(it.uid) },
                ) {
                    Box(
                        modifier =
                            Modifier
                                .padding(6.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .size(130.dp),
                    ) {
                        BoardPreview(
                            content =
                                BoardPreviewContent(
                                    size = sqrt(it.currentBoard.length.toFloat()).toInt(),
                                    boardString = it.currentBoard,
                                    boardColors = LocalBoardColors.current,
                                ),
                        )
                    }
                }
            }
        }
    }
}

/** Navigation/interaction callbacks used by [FoldersFolderList]. */
private data class FoldersListActions(
    val navigateExploreFolder: (Int) -> Unit,
    val navigateViewSavedGame: (Long) -> Unit,
    val onFolderLongClick: (Folder) -> Unit,
)

@Composable
private fun FoldersFolderList(
    folders: List<Folder>,
    lastGames: List<SavedGame>,
    puzzlesCountInFolder: List<Pair<Long, Int>>,
    onCountPuzzlesInFolders: (List<Folder>) -> Unit,
    actions: FoldersListActions,
) {
    val currentOnCountPuzzlesInFolders by rememberUpdatedState(onCountPuzzlesInFolders)
    LaunchedEffect(folders) {
        currentOnCountPuzzlesInFolders(folders)
    }
    ScrollbarLazyColumn {
        item {
            if (lastGames.isNotEmpty()) {
                FoldersLastPlayedSection(lastGames, actions.navigateViewSavedGame)
            }
        }
        items(folders) { item ->
            val puzzlesCount by remember(puzzlesCountInFolder) {
                mutableIntStateOf(
                    puzzlesCountInFolder
                        .firstOrNull { it.first == item.uid }
                        ?.second ?: 0,
                )
            }
            FolderItem(
                name = item.name,
                puzzlesCount = puzzlesCount,
                onClick = {
                    actions.navigateExploreFolder(item.uid.toInt())
                },
                onLongClick = {
                    actions.onFolderLongClick(item)
                },
            )
            HorizontalDivider()
        }
    }
}

@Composable
private fun FoldersScreenOverlays(
    dialogsState: FoldersDialogsState,
    dialogsActions: FoldersDialogsActions,
    folderActionBottomSheetVisible: Boolean,
    selectedFolderName: String?,
    contentUri: Uri?,
    coroutineScope: CoroutineScope,
    createDocumentLauncher: ManagedActivityResultLauncher<String, Uri?>,
    navigateImportSudokuFile: (String) -> Unit,
    folderActionSheetActions: FolderActionSheetActions,
) {
    FoldersManagementDialogs(
        state = dialogsState,
        actions = dialogsActions,
    )

    val currentNavigateImportSudokuFile by rememberUpdatedState(navigateImportSudokuFile)
    LaunchedEffect(contentUri) {
        contentUri?.let {
            currentNavigateImportSudokuFile(Uri.encode(it.toString()))
        }
    }

    if (folderActionBottomSheetVisible) {
        FolderActionBottomSheet(
            selectedFolderName = selectedFolderName,
            coroutineScope = coroutineScope,
            createDocumentLauncher = createDocumentLauncher,
            actions = folderActionSheetActions,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoldersScreen(
    viewModel: FoldersViewModel,
    navigateBack: () -> Unit,
    navigateExploreFolder: (Int) -> Unit,
    navigateImportSudokuFile: (String) -> Unit,
    navigateViewSavedGame: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val launchers = rememberFolderLaunchers(viewModel, coroutineScope, context)
    val flags = rememberFoldersDialogFlags()

    val snackbarHostState = remember { SnackbarHostState() }
    val gamesToImport by viewModel.sudokuListToImport.collectAsStateWithLifecycle()
    val createFolderDialog by flags.createFolderDialog
    val renameFolderDialog by flags.renameFolderDialog
    val deleteFolderDialog by flags.deleteFolderDialog
    val helpDialog by flags.helpDialog
    val folderActionBottomSheet by flags.folderActionBottomSheet
    val contentUri by launchers.contentUri
    val selectedFolderName = viewModel.selectedFolder?.name

    Scaffold(
        modifier = modifier,
        topBar = {
            FoldersTopBar(
                gamesToImportEmpty = gamesToImport.isEmpty(),
                gamesToImportSize = gamesToImport.size,
                actions =
                    FoldersTopBarActions(
                        navigateBack = navigateBack,
                        onHelpClick = { flags.helpDialog.value = true },
                        onImportClick = { launchers.openDocumentLauncher.launch(arrayOf("*/*")) },
                        onCreateFolderClick = { flags.createFolderDialog.value = true },
                    ),
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { paddingValues ->
        Column(
            modifier =
                Modifier
                    .padding(paddingValues)
                    .fillMaxWidth(),
        ) {
            val folders by viewModel.folders.collectAsStateWithLifecycle(initialValue = emptyList())
            val lastGames by viewModel.lastSavedGames.collectAsStateWithLifecycle(initialValue = emptyList())

            if (folders.isNotEmpty() && gamesToImport.isEmpty()) {
                FoldersFolderList(
                    folders = folders,
                    lastGames = lastGames,
                    puzzlesCountInFolder = viewModel.puzzlesCountInFolder,
                    onCountPuzzlesInFolders = viewModel::countPuzzlesInFolders,
                    actions =
                        FoldersListActions(
                            navigateExploreFolder = navigateExploreFolder,
                            navigateViewSavedGame = navigateViewSavedGame,
                            onFolderLongClick = {
                                viewModel.selectedFolder = it
                                coroutineScope.launch { flags.folderActionBottomSheet.value = true }
                            },
                        ),
                )
            }
        }
    }

    FoldersScreenOverlays(
        dialogsState =
            FoldersDialogsState(
                createFolderDialog = createFolderDialog,
                renameFolderDialog = renameFolderDialog,
                deleteFolderDialog = deleteFolderDialog,
                helpDialog = helpDialog,
                selectedFolderName = selectedFolderName,
            ),
        dialogsActions =
            FoldersDialogsActions(
                onCreateFolder = viewModel::createFolder,
                onDismissCreateFolderDialog = { flags.createFolderDialog.value = false },
                onRenameFolder = viewModel::renameFolder,
                onDismissRenameFolderDialog = { flags.renameFolderDialog.value = false },
                onDeleteFolder = viewModel::deleteFolder,
                onDismissDeleteFolderDialog = { flags.deleteFolderDialog.value = false },
                onDismissHelpDialog = { flags.helpDialog.value = false },
            ),
        folderActionBottomSheetVisible = folderActionBottomSheet,
        selectedFolderName = selectedFolderName,
        contentUri = contentUri,
        coroutineScope = coroutineScope,
        createDocumentLauncher = launchers.createDocumentLauncher,
        navigateImportSudokuFile = navigateImportSudokuFile,
        folderActionSheetActions =
            FolderActionSheetActions(
                onDismiss = { flags.folderActionBottomSheet.value = false },
                onRenameClick = { flags.renameFolderDialog.value = true },
                onDeleteClick = { flags.deleteFolderDialog.value = true },
            ),
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FolderItem(
    name: String,
    puzzlesCount: Int,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .then(modifier)
                .combinedClickable(
                    onClick = onClick,
                    onLongClick = onLongClick,
                ),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.fillMaxHeight(),
            ) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    text =
                        pluralStringResource(
                            R.plurals.puzzles_in_folder,
                            puzzlesCount,
                            puzzlesCount,
                        ),
                    color = LocalContentColor.current.copy(alpha = 0.75f),
                )
            }
        }
    }
}
