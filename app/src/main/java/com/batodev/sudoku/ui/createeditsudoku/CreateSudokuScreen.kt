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
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.batodev.sudoku.LocalBoardColors
import com.batodev.sudoku.R
import com.batodev.sudoku.core.Cell
import com.batodev.sudoku.core.PreferencesConstants
import com.batodev.sudoku.core.qqwing.GameDifficulty
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
    isNewSudoku: Boolean,
    navigateBack: () -> Unit,
    onImportStringClick: () -> Unit,
) {
    TopAppBar(
        title = {
            Text(
                text =
                    if (isNewSudoku) {
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
private fun DifficultyMenuButton(
    gameDifficultyRes: Int,
    onChangeDifficulty: (GameDifficulty) -> Unit,
) {
    var difficultyMenu by remember { mutableStateOf(false) }
    val dropDownIconRotation by animateFloatAsState(
        if (difficultyMenu) DROPDOWN_EXPANDED_ROTATION_DEGREES else 0f,
    )
    TextButton(onClick = { difficultyMenu = !difficultyMenu }) {
        Text(stringResource(gameDifficultyRes))
        Icon(
            modifier = Modifier.rotate(dropDownIconRotation),
            imageVector = Icons.Rounded.ArrowDropDown,
            contentDescription = null,
        )
    }
    DifficultyDropdownMenu(
        expanded = difficultyMenu,
        onDismissRequest = { difficultyMenu = false },
        onClick = onChangeDifficulty,
    )
}

@Composable
private fun GameTypeMenuButton(
    gameTypeRes: Int,
    onChangeGameType: (GameType) -> Unit,
) {
    var gameTypeMenuExpanded by remember { mutableStateOf(false) }
    val dropDownIconRotation by animateFloatAsState(
        if (gameTypeMenuExpanded) DROPDOWN_EXPANDED_ROTATION_DEGREES else 0f,
    )
    TextButton(onClick = { gameTypeMenuExpanded = !gameTypeMenuExpanded }) {
        Text(stringResource(gameTypeRes))
        Icon(
            modifier = Modifier.rotate(dropDownIconRotation),
            imageVector = Icons.Rounded.ArrowDropDown,
            contentDescription = null,
        )
    }
    GameTypeMenu(
        expanded = gameTypeMenuExpanded,
        onDismissRequest = { gameTypeMenuExpanded = false },
        onClick = onChangeGameType,
    )
}

internal data class CreateSudokuHeaderState(
    val isNewSudoku: Boolean,
    val gameDifficultyRes: Int,
    val gameTypeRes: Int,
    val canSave: Boolean,
)

internal data class CreateSudokuHeaderActions(
    val onChangeDifficulty: (GameDifficulty) -> Unit,
    val onChangeGameType: (GameType) -> Unit,
    val onSaveClick: () -> Unit,
)

@Composable
private fun CreateSudokuHeaderRow(
    state: CreateSudokuHeaderState,
    actions: CreateSudokuHeaderActions,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row {
            Box {
                DifficultyMenuButton(state.gameDifficultyRes, actions.onChangeDifficulty)
            }
            // allow changing a game type only when creating a new sudoku
            if (state.isNewSudoku) {
                Box {
                    GameTypeMenuButton(state.gameTypeRes, actions.onChangeGameType)
                }
            }
        }
        FilledTonalButton(
            enabled = state.canSave,
            onClick = actions.onSaveClick,
        ) {
            Text(stringResource(R.string.action_save))
        }
    }
}

internal data class CreateSudokuBoardPrefs(
    val highlightIdentical: Boolean,
    val positionLines: Boolean,
    val crossHighlight: Boolean,
    val fontSizeValue: TextUnit,
)

@Composable
private fun rememberCreateSudokuBoardPrefs(viewModel: CreateSudokuViewModel): CreateSudokuBoardPrefs {
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
    return CreateSudokuBoardPrefs(highlightIdentical, positionLines, crossHighlight, fontSizeValue)
}

internal data class CreateSudokuBoardState(
    val gameBoard: List<List<Cell>>,
    val gameTypeSize: Int,
    val currCell: Cell,
)

@Composable
private fun CreateSudokuBoardSection(
    boardState: CreateSudokuBoardState,
    prefs: CreateSudokuBoardPrefs,
    onCellClick: (Cell) -> Unit,
) {
    Board(
        modifier = Modifier.padding(vertical = 12.dp),
        data = BoardData(board = boardState.gameBoard, size = boardState.gameTypeSize),
        interaction =
            BoardInteraction(
                selectedCell = boardState.currCell,
                onClick = onCellClick,
            ),
        style =
            BoardStyle(
                boardColors = LocalBoardColors.current,
                textSizes = BoardTextSizes(mainTextSize = prefs.fontSizeValue),
                displayOptions =
                    BoardDisplayOptions(
                        identicalNumbersHighlight = prefs.highlightIdentical,
                        positionLines = prefs.positionLines,
                        crossHighlight = prefs.crossHighlight,
                    ),
            ),
    )
}

internal data class CreateSudokuKeyboardState(
    val gameTypeSize: Int,
    val digitFirstNumber: Int,
    val funKeyboardOverNum: Boolean,
)

internal data class CreateSudokuKeyboardActions(
    val onProcessInputKeyboard: (number: Int, longTap: Boolean) -> Unit,
    val onUndoClick: () -> Unit,
    val onRedoClick: () -> Unit,
    val onEraseClick: () -> Unit,
)

@Composable
private fun CreateSudokuKeyboardSection(
    state: CreateSudokuKeyboardState,
    actions: CreateSudokuKeyboardActions,
) {
    Column(
        verticalArrangement = if (state.funKeyboardOverNum) ReverseArrangement else Arrangement.Top,
    ) {
        DefaultGameKeyboard(
            size = state.gameTypeSize,
            state =
                KeyboardState(
                    remainingUses = null,
                    selected = state.digitFirstNumber,
                    handlers = keyboardClickHandlers(actions.onProcessInputKeyboard),
                ),
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(vertical = 8.dp),
        ) {
            ToolbarItem(
                modifier = Modifier.weight(UNDO_REDO_ITEM_WEIGHT),
                painter = painterResource(R.drawable.ic_round_undo_24),
                onClick = actions.onUndoClick,
            )

            ToolbarItem(
                modifier = Modifier.weight(UNDO_REDO_ITEM_WEIGHT),
                painter = rememberVectorPainter(Icons.Rounded.Redo),
                onClick = actions.onRedoClick,
            )

            ToolbarItem(
                modifier = Modifier.weight(1f),
                painter = painterResource(R.drawable.ic_eraser_24),
                onClick = actions.onEraseClick,
            )
        }
    }
}

internal data class CreateSudokuDialogsState(
    val importStringDialog: Boolean,
    val importStringValue: String,
    val importTextFieldError: Boolean,
    val multipleSolutionsDialog: Boolean,
    val noSolutionsDialog: Boolean,
)

internal data class CreateSudokuDialogsActions(
    val onImportStringValueChange: (String) -> Unit,
    val onConfirmImportString: () -> Unit,
    val onDismissImportStringDialog: () -> Unit,
    val onDismissMultipleSolutionsDialog: () -> Unit,
    val onDismissNoSolutionsDialog: () -> Unit,
)

@Composable
private fun CreateSudokuDialogs(
    state: CreateSudokuDialogsState,
    actions: CreateSudokuDialogsActions,
) {
    if (state.importStringDialog) {
        ImportStringSudokuDialog(
            textValue = state.importStringValue,
            onTextChange = actions.onImportStringValueChange,
            isError = state.importTextFieldError,
            onConfirm = actions.onConfirmImportString,
            onDismiss = actions.onDismissImportStringDialog,
        )
    } else if (state.multipleSolutionsDialog) {
        AlertDialog(
            title = { Text(stringResource(R.string.create_incorrect_puzzle)) },
            text = {
                Text(stringResource(R.string.multiple_solution_text))
            },
            onDismissRequest = actions.onDismissMultipleSolutionsDialog,
            confirmButton = {
                TextButton(onClick = actions.onDismissMultipleSolutionsDialog) {
                    Text(stringResource(R.string.dialog_ok))
                }
            },
        )
    } else if (state.noSolutionsDialog) {
        AlertDialog(
            title = { Text(stringResource(R.string.create_incorrect_puzzle)) },
            text = {
                Text(stringResource(R.string.no_solution_text))
            },
            onDismissRequest = actions.onDismissNoSolutionsDialog,
            confirmButton = {
                TextButton(onClick = actions.onDismissNoSolutionsDialog) {
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
    modifier: Modifier = Modifier,
) {
    var importStringDialog by remember { mutableStateOf(false) }
    val isNewSudoku = viewModel.gameUid == -1L
    val boardPrefs = rememberCreateSudokuBoardPrefs(viewModel)
    val funKeyboardOverNum by viewModel.funKeyboardOverNum.collectAsStateWithLifecycle(
        initialValue = PreferencesConstants.DEFAULT_FUN_KEYBOARD_OVER_NUM,
    )

    Scaffold(
        modifier = modifier,
        topBar = {
            CreateSudokuTopBar(isNewSudoku, navigateBack) { importStringDialog = true }
        },
    ) { paddingValues ->
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(paddingValues)
                    .padding(horizontal = 12.dp),
        ) {
            CreateSudokuHeaderRow(
                state =
                    CreateSudokuHeaderState(
                        isNewSudoku = isNewSudoku,
                        gameDifficultyRes = viewModel.gameDifficulty.resName,
                        gameTypeRes = viewModel.gameType.resName,
                        canSave = !viewModel.gameBoard.flatten().all { it.value == 0 },
                    ),
                actions =
                    CreateSudokuHeaderActions(
                        onChangeDifficulty = viewModel::changeGameDifficulty,
                        onChangeGameType = viewModel::changeGameType,
                        onSaveClick = {
                            if (viewModel.saveGame()) {
                                navigateBack()
                            }
                        },
                    ),
            )
            CreateSudokuBoardSection(
                boardState =
                    CreateSudokuBoardState(
                        gameBoard = viewModel.gameBoard,
                        gameTypeSize = viewModel.gameType.size,
                        currCell = viewModel.currCell,
                    ),
                prefs = boardPrefs,
                onCellClick = { cell -> viewModel.processInput(cell = cell) },
            )
            CreateSudokuKeyboardSection(
                state =
                    CreateSudokuKeyboardState(
                        gameTypeSize = viewModel.gameType.size,
                        digitFirstNumber = viewModel.digitFirstNumber,
                        funKeyboardOverNum = funKeyboardOverNum,
                    ),
                actions =
                    CreateSudokuKeyboardActions(
                        onProcessInputKeyboard = viewModel::processInputKeyboard,
                        onUndoClick = { viewModel.toolbarClick(ToolBarItem.Undo) },
                        onRedoClick = { viewModel.toolbarClick(ToolBarItem.Redo) },
                        onEraseClick = { viewModel.toolbarClick(ToolBarItem.Remove) },
                    ),
            )
            CreateSudokuDialogs(
                state =
                    CreateSudokuDialogsState(
                        importStringDialog = importStringDialog,
                        importStringValue = viewModel.importStringValue,
                        importTextFieldError = viewModel.importTextFieldError,
                        multipleSolutionsDialog = viewModel.multipleSolutionsDialog,
                        noSolutionsDialog = viewModel.noSolutionsDialog,
                    ),
                actions =
                    CreateSudokuDialogsActions(
                        onImportStringValueChange = {
                            viewModel.importStringValue = it
                            viewModel.importTextFieldError = false
                        },
                        onConfirmImportString = {
                            viewModel.setFromString(viewModel.importStringValue.trim()).also {
                                viewModel.importTextFieldError = !it
                                if (it) {
                                    importStringDialog = false
                                    viewModel.importStringValue = ""
                                }
                            }
                        },
                        onDismissImportStringDialog = { importStringDialog = false },
                        onDismissMultipleSolutionsDialog = { viewModel.multipleSolutionsDialog = false },
                        onDismissNoSolutionsDialog = { viewModel.noSolutionsDialog = false },
                    ),
            )
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
