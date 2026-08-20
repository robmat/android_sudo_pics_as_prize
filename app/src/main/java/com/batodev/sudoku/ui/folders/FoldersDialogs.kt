package com.batodev.sudoku.ui.folders

import android.net.Uri
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.rounded.CreateNewFolder
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Help
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.batodev.sudoku.R
import com.batodev.sudoku.data.database.model.Folder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/** The title/icon shown at the top of a [NameActionDialog] or [FolderNameEntryDialog]. */
data class DialogHeader(
    val title: @Composable () -> Unit,
    val icon: @Composable (() -> Unit)? = null,
)

/** The current text and validation state of a [NameActionDialog]'s text field. */
data class DialogTextFieldState(
    val value: TextFieldValue,
    val onValueChange: (TextFieldValue) -> Unit,
    val isError: Boolean,
)

/** The confirm/dismiss callbacks for a [NameActionDialog]. */
data class DialogActions(
    val onConfirm: () -> Unit,
    val onDismiss: () -> Unit,
)

/** The validation-result callbacks for a [FolderNameEntryDialog]. */
data class FolderNameEntryCallbacks(
    val onValidName: (String) -> Unit,
    val onInvalidName: () -> Unit = {},
    val onDismiss: () -> Unit,
)

private const val MAX_FOLDER_NAME_LENGTH = 128

/**
 * A [NameActionDialog] pre-wired with the folder-name validation rule (non-empty, under
 * [MAX_FOLDER_NAME_LENGTH] characters) shared by the create-folder and rename-folder dialogs.
 * [FolderNameEntryCallbacks.onValidName] is invoked with the trimmed-length-checked text on
 * success; [FolderNameEntryCallbacks.onInvalidName] runs instead when validation fails (in
 * addition to surfacing the error state on the text field).
 */
@Composable
fun FolderNameEntryDialog(
    header: DialogHeader,
    initialValue: TextFieldValue,
    callbacks: FolderNameEntryCallbacks,
) {
    var textFieldValue by remember { mutableStateOf(initialValue) }
    var isError by rememberSaveable { mutableStateOf(false) }

    NameActionDialog(
        header = header,
        textFieldState =
            DialogTextFieldState(
                value = textFieldValue,
                onValueChange = {
                    textFieldValue = it
                    if (isError) isError = false
                },
                isError = isError,
            ),
        actions =
            DialogActions(
                onConfirm = {
                    if (textFieldValue.text.isNotEmpty() && textFieldValue.text.length < MAX_FOLDER_NAME_LENGTH) {
                        callbacks.onValidName(textFieldValue.text)
                    } else {
                        isError = true
                        callbacks.onInvalidName()
                    }
                },
                onDismiss = callbacks.onDismiss,
            ),
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NameActionDialog(
    header: DialogHeader,
    textFieldState: DialogTextFieldState,
    actions: DialogActions,
    modifier: Modifier = Modifier,
) {
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    AlertDialog(
        modifier = modifier,
        title = header.title,
        icon = header.icon,
        text = {
            OutlinedTextField(
                modifier = Modifier.focusRequester(focusRequester),
                isError = textFieldState.isError,
                singleLine = true,
                value = textFieldState.value,
                onValueChange = textFieldState.onValueChange,
                label = { Text(stringResource(R.string.create_folder_name)) },
            )
        },
        onDismissRequest = actions.onDismiss,
        confirmButton = {
            TextButton(
                onClick = actions.onConfirm,
            ) {
                Text(stringResource(R.string.dialog_ok))
            }
        },
        dismissButton = {
            TextButton(
                onClick = actions.onDismiss,
            ) {
                Text(stringResource(R.string.action_cancel))
            }
        },
    )
}

/** The dialog-visibility flags managed by [FoldersManagementDialogs]. */
data class FoldersDialogsState(
    val createFolderDialog: MutableState<Boolean>,
    val renameFolderDialog: MutableState<Boolean>,
    val deleteFolderDialog: MutableState<Boolean>,
    val helpDialog: MutableState<Boolean>,
)

@Composable
private fun FoldersCreateDialog(
    viewModel: FoldersViewModel,
    createFolderDialog: MutableState<Boolean>,
) {
    FolderNameEntryDialog(
        header =
            DialogHeader(
                title = { Text(stringResource(R.string.create_folder)) },
                icon = {
                    Icon(Icons.Rounded.CreateNewFolder, contentDescription = null)
                },
            ),
        initialValue = TextFieldValue(""),
        callbacks =
            FolderNameEntryCallbacks(
                onValidName = {
                    viewModel.createFolder(it)
                    createFolderDialog.value = false
                },
                onDismiss = {
                    createFolderDialog.value = false
                },
            ),
    )
}

@Composable
private fun FoldersRenameDialog(
    viewModel: FoldersViewModel,
    renameFolderDialog: MutableState<Boolean>,
) {
    FolderNameEntryDialog(
        header =
            DialogHeader(
                title = { Text(stringResource(R.string.edit_name)) },
                icon = {
                    Icon(Icons.Rounded.Edit, contentDescription = null)
                },
            ),
        initialValue =
            TextFieldValue(
                text = viewModel.selectedFolder?.name ?: "",
                selection = TextRange((viewModel.selectedFolder?.name ?: "").length),
            ),
        callbacks =
            FolderNameEntryCallbacks(
                onValidName = {
                    viewModel.renameFolder(it)
                    renameFolderDialog.value = false
                },
                onInvalidName = {
                    renameFolderDialog.value = false
                },
                onDismiss = {
                    renameFolderDialog.value = false
                },
            ),
    )
}

@Composable
private fun FoldersDeleteDialog(
    viewModel: FoldersViewModel,
    deleteFolderDialog: MutableState<Boolean>,
) {
    AlertDialog(
        title = { Text(stringResource(R.string.delete_folder)) },
        text = {
            viewModel.selectedFolder?.let {
                Text(stringResource(R.string.dialog_delete_folder_text, it.name))
            }
        },
        confirmButton = {
            TextButton(onClick = {
                viewModel.deleteFolder()
                deleteFolderDialog.value = false
            }) {
                Text(stringResource(R.string.action_delete))
            }
        },
        dismissButton = {
            TextButton(onClick = {
                deleteFolderDialog.value = false
            }) {
                Text(stringResource(R.string.action_cancel))
            }
        },
        onDismissRequest = {
            deleteFolderDialog.value = false
        },
    )
}

@Composable
private fun FoldersHelpDialog(helpDialog: MutableState<Boolean>) {
    AlertDialog(
        icon = {
            Icon(Icons.Rounded.Help, contentDescription = null)
        },
        title = { Text(stringResource(R.string.help)) },
        text = {
            Column {
                Text(stringResource(R.string.folders_help))
                Text(stringResource(R.string.folder_import_supported_types))
            }
        },
        confirmButton = {
            TextButton(onClick = {
                helpDialog.value = false
            }) {
                Text(stringResource(R.string.dialog_ok))
            }
        },
        onDismissRequest = {
            helpDialog.value = false
        },
    )
}

@Composable
fun FoldersManagementDialogs(
    viewModel: FoldersViewModel,
    dialogsState: FoldersDialogsState,
) {
    when {
        dialogsState.createFolderDialog.value -> {
            FoldersCreateDialog(viewModel, dialogsState.createFolderDialog)
        }

        dialogsState.renameFolderDialog.value -> {
            FoldersRenameDialog(viewModel, dialogsState.renameFolderDialog)
        }

        dialogsState.deleteFolderDialog.value -> {
            FoldersDeleteDialog(viewModel, dialogsState.deleteFolderDialog)
        }

        dialogsState.helpDialog.value -> {
            FoldersHelpDialog(dialogsState.helpDialog)
        }
    }
}

/** The dialog-visibility flags [FolderActionBottomSheet] can toggle from its action list. */
data class FolderActionSheetState(
    val folderActionBottomSheet: MutableState<Boolean>,
    val renameFolderDialog: MutableState<Boolean>,
    val deleteFolderDialog: MutableState<Boolean>,
)

@Composable
private fun ColumnScope.FolderActionSheetHeader(folder: Folder) {
    Row(
        modifier = Modifier.align(Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Icons.Outlined.Folder, contentDescription = null)
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = stringResource(R.string.folder_name, folder.name),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
    Spacer(modifier = Modifier.height(12.dp))
}

@Composable
private fun FolderActionSheetItems(
    viewModel: FoldersViewModel,
    coroutineScope: CoroutineScope,
    sheetState: FolderActionSheetState,
    createDocumentLauncher: ManagedActivityResultLauncher<String, Uri?>,
) {
    val actions =
        listOf(
            Pair(Icons.Rounded.Edit, stringResource(R.string.edit_name)),
            Pair(Icons.Rounded.Share, stringResource(R.string.export)),
            Pair(Icons.Rounded.Delete, stringResource(R.string.action_delete)),
        )
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(vertical = 8.dp, horizontal = 12.dp),
    ) {
        actions.forEachIndexed { index, action ->
            Row(
                modifier =
                    Modifier
                        .clip(MaterialTheme.shapes.small)
                        .fillMaxWidth()
                        .clickable {
                            when (index) {
                                0 -> {
                                    sheetState.renameFolderDialog.value = true
                                }

                                1 -> {
                                    var fileName = ""
                                    viewModel.selectedFolder?.let {
                                        fileName += it.name + "-"
                                    }
                                    fileName += LocalDateTime
                                        .now()
                                        .format(
                                            DateTimeFormatter.ofPattern("yyyy-dd-MM-HH-mm"),
                                        ) ?: ""
                                    createDocumentLauncher.launch("$fileName.sdm")
                                }

                                2 -> {
                                    sheetState.deleteFolderDialog.value = true
                                }
                            }
                            coroutineScope.launch {
                                sheetState.folderActionBottomSheet.value = false
                            }
                        },
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    modifier = Modifier.padding(12.dp),
                    imageVector = action.first,
                    contentDescription = null,
                )
                Text(action.second)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FolderActionBottomSheet(
    viewModel: FoldersViewModel,
    coroutineScope: CoroutineScope,
    sheetState: FolderActionSheetState,
    createDocumentLauncher: ManagedActivityResultLauncher<String, Uri?>,
) {
    ModalBottomSheet(onDismissRequest = { sheetState.folderActionBottomSheet.value = false }) {
        viewModel.selectedFolder?.let { FolderActionSheetHeader(it) }
        FolderActionSheetItems(viewModel, coroutineScope, sheetState, createDocumentLauncher)
    }
}
