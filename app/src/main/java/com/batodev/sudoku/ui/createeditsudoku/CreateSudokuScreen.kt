package com.batodev.sudoku.ui.createeditsudoku

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material.icons.rounded.Redo
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.batodev.sudoku.LocalBoardColors
import com.batodev.sudoku.R
import com.batodev.sudoku.core.PreferencesConstants
import com.batodev.sudoku.core.qqwing.GameType
import com.batodev.sudoku.ui.components.BackIconButton
import com.batodev.sudoku.ui.components.DifficultyDropdownMenu
import com.batodev.sudoku.ui.components.OverflowMenuButton
import com.batodev.sudoku.ui.components.board.Board
import com.batodev.sudoku.ui.components.board.BoardData
import com.batodev.sudoku.ui.components.board.BoardDisplayOptions
import com.batodev.sudoku.ui.components.board.BoardInteraction
import com.batodev.sudoku.ui.components.board.BoardStyle
import com.batodev.sudoku.ui.components.board.BoardTextSizes
import com.batodev.sudoku.ui.game.components.DefaultGameKeyboard
import com.batodev.sudoku.ui.game.components.KeyboardState
import com.batodev.sudoku.ui.game.components.ToolBarItem
import com.batodev.sudoku.ui.game.components.ToolbarItem
import com.batodev.sudoku.ui.game.components.keyboardClickHandlers
import com.batodev.sudoku.ui.util.ReverseArrangement

private const val DROPDOWN_EXPANDED_ROTATION_DEGREES = 180f
private const val UNDO_REDO_ITEM_WEIGHT = 0.5f

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreateSudokuTopBar(
    viewModel: CreateSudokuViewModel,
    navigateBack: () -> Unit,
    onImportStringClick: () -> Unit,
) {
    TopAppBar(
        title = {
            Text(
                text =
                    if (viewModel.gameUid == -1L) {
                        stringResource(R.string.create_sudoku_title)
                    } else {
                        stringResource(R.string.edit_sudoku)
                    },
            )
        },
        navigationIcon = {
            BackIconButton(onClick = navigateBack)
        },
        actions = {
            OverflowMenuButton { closeMenu ->
                DropdownMenuItem(
                    text = {
                        Text(stringResource(R.string.create_set_from_string))
                    },
                    onClick = {
                        onImportStringClick()
                        closeMenu()
                    },
                )
            }
        },
    )
}

@Composable
private fun DifficultyMenuButton(viewModel: CreateSudokuViewModel) {
    var difficultyMenu by remember { mutableStateOf(false) }
    val dropDownIconRotation by animateFloatAsState(
        if (difficultyMenu) DROPDOWN_EXPANDED_ROTATION_DEGREES else 0f,
    )
    TextButton(onClick = { difficultyMenu = !difficultyMenu }) {
        Text(stringResource(viewModel.gameDifficulty.resName))
        Icon(
            modifier = Modifier.rotate(dropDownIconRotation),
            imageVector = Icons.Rounded.ArrowDropDown,
            contentDescription = null,
        )
    }
    DifficultyDropdownMenu(
        expanded = difficultyMenu,
        onDismissRequest = { difficultyMenu = false },
        onClick = {
            viewModel.changeGameDifficulty(it)
        },
    )
}

@Composable
private fun GameTypeMenuButton(viewModel: CreateSudokuViewModel) {
    var gameTypeMenuExpanded by remember { mutableStateOf(false) }
    val dropDownIconRotation by animateFloatAsState(
        if (gameTypeMenuExpanded) DROPDOWN_EXPANDED_ROTATION_DEGREES else 0f,
    )
    TextButton(onClick = { gameTypeMenuExpanded = !gameTypeMenuExpanded }) {
        Text(stringResource(viewModel.gameType.resName))
        Icon(
            modifier = Modifier.rotate(dropDownIconRotation),
            imageVector = Icons.Rounded.ArrowDropDown,
            contentDescription = null,
        )
    }
    GameTypeMenu(
        expanded = gameTypeMenuExpanded,
        onDismissRequest = { gameTypeMenuExpanded = false },
        onClick = {
            viewModel.changeGameType(it)
        },
    )
}

@Composable
private fun CreateSudokuHeaderRow(
    viewModel: CreateSudokuViewModel,
    navigateBack: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row {
            Box {
                DifficultyMenuButton(viewModel)
            }
            // allow changing a game type only when creating a new sudoku
            if (viewModel.gameUid == -1L) {
                Box {
                    GameTypeMenuButton(viewModel)
                }
            }
        }
        FilledTonalButton(
            enabled = !viewModel.gameBoard.flatten().all { it.value == 0 },
            onClick = {
                if (viewModel.saveGame()) {
                    navigateBack()
                }
            },
        ) {
            Text(stringResource(R.string.action_save))
        }
    }
}

@Composable
private fun CreateSudokuBoardSection(viewModel: CreateSudokuViewModel) {
    val highlightIdentical by viewModel.highlightIdentical.collectAsState(
        initial = PreferencesConstants.DEFAULT_HIGHLIGHT_IDENTICAL,
    )
    val fontSizeFactor by viewModel.fontSize.collectAsState(
        initial = PreferencesConstants.DEFAULT_FONT_SIZE_FACTOR,
    )
    val fontSizeValue by remember(fontSizeFactor, viewModel.gameType) {
        mutableStateOf(
            viewModel.getFontSize(factor = fontSizeFactor),
        )
    }

    val positionLines by viewModel.positionLines.collectAsState(
        initial = PreferencesConstants.DEFAULT_POSITION_LINES,
    )
    val crossHighlight by viewModel.crossHighlight.collectAsState(
        initial = PreferencesConstants.DEFAULT_BOARD_CROSS_HIGHLIGHT,
    )
    Board(
        modifier = Modifier.padding(vertical = 12.dp),
        data = BoardData(board = viewModel.gameBoard, size = viewModel.gameType.size),
        interaction =
            BoardInteraction(
                selectedCell = viewModel.currCell,
                onClick = { cell -> viewModel.processInput(cell = cell) },
            ),
        style =
            BoardStyle(
                boardColors = LocalBoardColors.current,
                textSizes = BoardTextSizes(mainTextSize = fontSizeValue),
                displayOptions =
                    BoardDisplayOptions(
                        identicalNumbersHighlight = highlightIdentical,
                        positionLines = positionLines,
                        crossHighlight = crossHighlight,
                    ),
            ),
    )
}

@Composable
private fun CreateSudokuKeyboardSection(viewModel: CreateSudokuViewModel) {
    val funKeyboardOverNum by viewModel.funKeyboardOverNum.collectAsStateWithLifecycle(
        initialValue = PreferencesConstants.DEFAULT_FUN_KEYBOARD_OVER_NUM,
    )

    Column(
        verticalArrangement = if (funKeyboardOverNum) ReverseArrangement else Arrangement.Top,
    ) {
        DefaultGameKeyboard(
            size = viewModel.gameType.size,
            state =
                KeyboardState(
                    remainingUses = null,
                    selected = viewModel.digitFirstNumber,
                    handlers = keyboardClickHandlers(viewModel::processInputKeyboard),
                ),
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(vertical = 8.dp),
        ) {
            ToolbarItem(
                modifier = Modifier.weight(UNDO_REDO_ITEM_WEIGHT),
                painter = painterResource(R.drawable.ic_round_undo_24),
                onClick = { viewModel.toolbarClick(ToolBarItem.Undo) },
            )

            ToolbarItem(
                modifier = Modifier.weight(UNDO_REDO_ITEM_WEIGHT),
                painter = rememberVectorPainter(Icons.Rounded.Redo),
                onClick = { viewModel.toolbarClick(ToolBarItem.Redo) },
            )

            ToolbarItem(
                modifier = Modifier.weight(1f),
                painter = painterResource(R.drawable.ic_eraser_24),
                onClick = {
                    viewModel.toolbarClick(ToolBarItem.Remove)
                },
            )
        }
    }
}

@Composable
private fun CreateSudokuDialogs(
    viewModel: CreateSudokuViewModel,
    importStringDialog: Boolean,
    onDismissImportStringDialog: () -> Unit,
) {
    if (importStringDialog) {
        ImportStringSudokuDialog(
            textValue = viewModel.importStringValue,
            onTextChange = {
                viewModel.importStringValue = it
                viewModel.importTextFieldError = false
            },
            isError = viewModel.importTextFieldError,
            onConfirm = {
                viewModel.setFromString(viewModel.importStringValue.trim()).also {
                    viewModel.importTextFieldError = !it
                    if (it) {
                        onDismissImportStringDialog()
                        viewModel.importStringValue = ""
                    }
                }
            },
            onDismiss = onDismissImportStringDialog,
        )
    } else if (viewModel.multipleSolutionsDialog) {
        AlertDialog(
            title = { Text(stringResource(R.string.create_incorrect_puzzle)) },
            text = {
                Text(stringResource(R.string.multiple_solution_text))
            },
            onDismissRequest = { viewModel.multipleSolutionsDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.multipleSolutionsDialog = false
                }) {
                    Text(stringResource(R.string.dialog_ok))
                }
            },
        )
    } else if (viewModel.noSolutionsDialog) {
        AlertDialog(
            title = { Text(stringResource(R.string.create_incorrect_puzzle)) },
            text = {
                Text(stringResource(R.string.no_solution_text))
            },
            onDismissRequest = { viewModel.noSolutionsDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.noSolutionsDialog = false
                }) {
                    Text(stringResource(R.string.dialog_ok))
                }
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateSudokuScreen(
    navigateBack: () -> Unit,
    viewModel: CreateSudokuViewModel,
) {
    var importStringDialog by remember { mutableStateOf(false) }
    Scaffold(
        topBar = {
            CreateSudokuTopBar(viewModel, navigateBack) { importStringDialog = true }
        },
    ) { paddingValues ->
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(paddingValues)
                    .padding(horizontal = 12.dp),
        ) {
            CreateSudokuHeaderRow(viewModel, navigateBack)
            CreateSudokuBoardSection(viewModel)
            CreateSudokuKeyboardSection(viewModel)
            CreateSudokuDialogs(viewModel, importStringDialog) { importStringDialog = false }
        }
    }
}

@Composable
private fun GameTypeMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    onClick: (GameType) -> Unit,
) {
    MaterialTheme(shapes = MaterialTheme.shapes.copy(extraSmall = MaterialTheme.shapes.large)) {
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = onDismissRequest,
        ) {
            listOf(
                GameType.Default9x9,
                GameType.Default6x6,
                GameType.Default12x12,
            ).forEach {
                DropdownMenuItem(
                    text = {
                        Text(stringResource(it.resName))
                    },
                    onClick = {
                        onClick(it)
                        onDismissRequest()
                    },
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ImportStringSudokuDialog(
    textValue: String,
    onTextChange: (String) -> Unit,
    isError: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
    AlertDialog(
        title = { Text(stringResource(R.string.create_set_from_string)) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.create_from_string_text))
                OutlinedTextField(
                    modifier =
                        Modifier
                            .padding(top = 6.dp)
                            .focusRequester(focusRequester),
                    value = textValue,
                    keyboardOptions =
                        KeyboardOptions(
                            imeAction = ImeAction.Done,
                        ),
                    keyboardActions =
                        KeyboardActions(
                            onDone = { onConfirm() },
                        ),
                    isError = isError,
                    onValueChange = onTextChange,
                    label = { Text(stringResource(R.string.create_from_string_hint)) },
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_cancel))
            }
        },
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(stringResource(R.string.create_import_set))
            }
        },
    )
}
