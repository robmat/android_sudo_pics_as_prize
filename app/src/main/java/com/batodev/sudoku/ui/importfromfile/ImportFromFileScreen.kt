package com.batodev.sudoku.ui.importfromfile

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.batodev.sudoku.LocalBoardColors
import com.batodev.sudoku.R
import com.batodev.sudoku.core.qqwing.GameDifficulty
import com.batodev.sudoku.ui.components.DifficultyDropdownMenu
import com.batodev.sudoku.ui.components.LazyGridBehavior
import com.batodev.sudoku.ui.components.LazyGridScrollbarState
import com.batodev.sudoku.ui.components.ScrollbarLazyVerticalGrid
import com.batodev.sudoku.ui.components.board.BoardPreview
import com.batodev.sudoku.ui.components.board.BoardPreviewContent
import com.batodev.sudoku.ui.home.GeneratingDialog
import com.batodev.sudoku.ui.util.isScrolledToStart
import com.batodev.sudoku.ui.util.isScrollingUp
import kotlinx.coroutines.launch
import java.io.InputStreamReader

private const val DROPDOWN_EXPANDED_ROTATION_DEGREES = 180f
private const val MAX_FOLDER_NAME_LENGTH = 128
private const val PREVIEW_BOARD_SIZE = 9

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ImportFromFileTopBar(navigateBack: () -> Unit) {
    TopAppBar(
        title = {
            Text(
                text = stringResource(R.string.import_from_file_title),
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
private fun ImportFromFileFab(lazyGridState: LazyGridState) {
    val coroutineScope = rememberCoroutineScope()
    AnimatedVisibility(
        visible = lazyGridState.isScrollingUp() && !lazyGridState.isScrolledToStart(),
        enter = fadeIn() + scaleIn(),
        exit = fadeOut() + scaleOut(),
    ) {
        FloatingActionButton(
            onClick = {
                coroutineScope.launch { lazyGridState.animateScrollToItem(0) }
            },
        ) {
            Icon(Icons.Rounded.KeyboardArrowUp, contentDescription = null)
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun ImportFromFileDifficultyRow(
    difficultyRes: Int,
    gamesToImport: List<String>,
    onChangeDifficulty: (GameDifficulty) -> Unit,
    onSaveClick: () -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Box {
            var gameTypeMenuExpanded by remember { mutableStateOf(false) }
            val dropDownIconRotation by animateFloatAsState(
                if (gameTypeMenuExpanded) DROPDOWN_EXPANDED_ROTATION_DEGREES else 0f,
            )
            TextButton(onClick = { gameTypeMenuExpanded = !gameTypeMenuExpanded }) {
                AnimatedContent(stringResource(difficultyRes)) { text ->
                    Text(text)
                }
                Icon(
                    modifier = Modifier.rotate(dropDownIconRotation),
                    imageVector = Icons.Rounded.ArrowDropDown,
                    contentDescription = null,
                )
            }
            DifficultyDropdownMenu(
                expanded = gameTypeMenuExpanded,
                onDismissRequest = { gameTypeMenuExpanded = false },
                onClick = onChangeDifficulty,
            )
        }
        FilledTonalButton(
            enabled = gamesToImport.isNotEmpty(),
            onClick = onSaveClick,
        ) {
            Text(stringResource(R.string.action_save))
        }
    }
}

@Composable
private fun ImportFromFileGrid(
    lazyGridState: LazyGridState,
    gamesToImport: List<String>,
) {
    var span by remember { mutableIntStateOf(1) }
    ScrollbarLazyVerticalGrid(
        columns = GridCells.Adaptive(130.dp),
        scrollbarState = LazyGridScrollbarState(state = lazyGridState, spanCount = span),
        behavior = LazyGridBehavior(horizontalArrangement = Arrangement.spacedBy(12.dp)),
        content = {
            items(
                items = gamesToImport,
                span = { GridItemSpan(1).also { span = maxLineSpan } },
            ) { item ->
                Column(
                    modifier = Modifier.padding(8.dp),
                ) {
                    BoardPreview(
                        content =
                            BoardPreviewContent(
                                size = PREVIEW_BOARD_SIZE,
                                boardString = item,
                                boardColors = LocalBoardColors.current,
                            ),
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider()
                }
            }
        },
    )
}

@Composable
private fun ImportFromFileLoadingDialog() {
    Dialog(onDismissRequest = { }) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surfaceColorAtElevation(6.dp),
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.padding(24.dp),
            ) {
                CircularProgressIndicator(Modifier.align(Alignment.Center))
            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun ImportFromFileContent(
    paddingValues: PaddingValues,
    difficultyRes: Int,
    isLoading: Boolean,
    gamesToImport: List<String>,
    lazyGridState: LazyGridState,
    onChangeDifficulty: (GameDifficulty) -> Unit,
    onSaveClick: () -> Unit,
) {
    Column(Modifier.padding(paddingValues)) {
        Column(
            modifier =
                Modifier
                    .padding(horizontal = 12.dp),
        ) {
            Text(
                pluralStringResource(
                    R.plurals.number_puzzles_to_import,
                    gamesToImport.size,
                    gamesToImport.size,
                ),
            )
            ImportFromFileDifficultyRow(difficultyRes, gamesToImport, onChangeDifficulty, onSaveClick)
        }
        HorizontalDivider()
        ImportFromFileGrid(lazyGridState, gamesToImport)
        if (isLoading) {
            ImportFromFileLoadingDialog()
        }
    }
}

@Composable
private fun ImportFromFileFolderNameDialog(
    onDismiss: () -> Unit,
    onSaveImport: (String) -> Unit,
) {
    var value by remember { mutableStateOf("") }
    var isError by rememberSaveable { mutableStateOf(false) }

    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    AlertDialog(
        title = { Text(stringResource(R.string.create_folder)) },
        text = {
            OutlinedTextField(
                modifier = Modifier.focusRequester(focusRequester),
                isError = isError,
                singleLine = true,
                value = value,
                onValueChange = { value = it },
                label = { Text(stringResource(R.string.create_folder_name)) },
            )
        },
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    if (value.isNotEmpty() && value.length < MAX_FOLDER_NAME_LENGTH) {
                        onSaveImport(value)
                        onDismiss()
                    } else {
                        isError = true
                    }
                },
            ) {
                Text(stringResource(R.string.dialog_ok))
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
            ) {
                Text(stringResource(R.string.action_cancel))
            }
        },
    )
}

@Composable
private fun ImportFromFileEffects(
    fileUri: Uri?,
    isSaved: Boolean,
    importError: Boolean,
    onReadFile: (InputStreamReader) -> Unit,
    navigateBack: () -> Unit,
) {
    val currentNavigateBack by rememberUpdatedState(navigateBack)
    val currentOnReadFile by rememberUpdatedState(onReadFile)
    val context = LocalContext.current
    val importFromFileFailMessage = stringResource(R.string.import_from_file_fail)
    LaunchedEffect(fileUri) {
        fileUri?.let {
            val inputStream = context.contentResolver.openInputStream(it)
            inputStream?.let { stream ->
                currentOnReadFile(InputStreamReader(stream))
            }
        }
    }

    LaunchedEffect(isSaved) {
        if (isSaved) {
            currentNavigateBack()
        }
    }
    LaunchedEffect(importError) {
        if (importError) {
            Toast
                .makeText(
                    context,
                    importFromFileFailMessage,
                    Toast.LENGTH_SHORT,
                ).show()
            currentNavigateBack()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun ImportFromFileScreen(
    viewModel: ImportFromFileViewModel,
    navigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    BackHandler {
        navigateBack()
    }

    val importError by viewModel.importError.collectAsStateWithLifecycle()
    ImportFromFileEffects(
        fileUri = viewModel.fileUri,
        isSaved = viewModel.isSaved,
        importError = importError,
        onReadFile = viewModel::readData,
        navigateBack = navigateBack,
    )

    val gamesToImport by viewModel.sudokuListToImport.collectAsStateWithLifecycle(emptyList())
    var setFolderNameDialog by rememberSaveable { mutableStateOf(false) }
    val lazyGridState = rememberLazyGridState()

    Scaffold(
        modifier = modifier,
        topBar = { ImportFromFileTopBar(navigateBack) },
        floatingActionButton = { ImportFromFileFab(lazyGridState) },
    ) { paddingValues ->
        ImportFromFileContent(
            paddingValues = paddingValues,
            difficultyRes = viewModel.difficultyForImport.resName,
            isLoading = viewModel.isLoading,
            gamesToImport = gamesToImport,
            lazyGridState = lazyGridState,
            onChangeDifficulty = viewModel::setDifficulty,
            onSaveClick = {
                if (viewModel.folderUid == -1L) {
                    setFolderNameDialog = true
                } else {
                    viewModel.saveImported()
                }
            },
        )
    }

    if (setFolderNameDialog) {
        ImportFromFileFolderNameDialog(
            onDismiss = { setFolderNameDialog = false },
            onSaveImport = { viewModel.saveImported(it) },
        )
    }

    if (viewModel.isSaving) {
        GeneratingDialog(onDismiss = { }, text = stringResource(R.string.import_saving))
    }
}
