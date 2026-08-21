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

/** The dialog-visibility flags and selected folder managed by [FoldersManagementDialogs]. */
data class FoldersDialogsState(
    val createFolderDialog: Boolean,
    val renameFolderDialog: Boolean,
    val deleteFolderDialog: Boolean,
    val helpDialog: Boolean,
    val selectedFolderName: String?,
)

/** The callbacks [FoldersManagementDialogs] needs; constructed once by [FoldersScreen]. */
data class FoldersDialogsActions(
    val onCreateFolder: (String) -> Unit,
    val onDismissCreateFolderDialog: () -> Unit,
    val onRenameFolder: (String) -> Unit,
    val onDismissRenameFolderDialog: () -> Unit,
    val onDeleteFolder: () -> Unit,
    val onDismissDeleteFolderDialog: () -> Unit,
    val onDismissHelpDialog: () -> Unit,
)

@Composable
private fun FoldersCreateDialog(
    onCreateFolder: (String) -> Unit,
    onDismiss: () -> Unit,
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
                    onCreateFolder(it)
                    onDismiss()
                },
                onDismiss = onDismiss,
            ),
    )
}

@Composable
private fun FoldersRenameDialog(
    selectedFolderName: String?,
    onRenameFolder: (String) -> Unit,
    onDismiss: () -> Unit,
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
                text = selectedFolderName ?: "",
                selection = TextRange((selectedFolderName ?: "").length),
            ),
        callbacks =
            FolderNameEntryCallbacks(
                onValidName = {
                    onRenameFolder(it)
                    onDismiss()
                },
                onInvalidName = onDismiss,
                onDismiss = onDismiss,
            ),
    )
}

@Composable
private fun FoldersDeleteDialog(
    selectedFolderName: String?,
    onDeleteFolder: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        title = { Text(stringResource(R.string.delete_folder)) },
        text = {
            selectedFolderName?.let {
                Text(stringResource(R.string.dialog_delete_folder_text, it))
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onDeleteFolder()
                onDismiss()
            }) {
                Text(stringResource(R.string.action_delete))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_cancel))
            }
        },
        onDismissRequest = onDismiss,
    )
}

@Composable
private fun FoldersHelpDialog(onDismiss: () -> Unit) {
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
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.dialog_ok))
            }
        },
        onDismissRequest = onDismiss,
    )
}

@Composable
fun FoldersManagementDialogs(
    state: FoldersDialogsState,
    actions: FoldersDialogsActions,
) {
    when {
        state.createFolderDialog -> {
            FoldersCreateDialog(actions.onCreateFolder, actions.onDismissCreateFolderDialog)
        }

        state.renameFolderDialog -> {
            FoldersRenameDialog(state.selectedFolderName, actions.onRenameFolder, actions.onDismissRenameFolderDialog)
        }

        state.deleteFolderDialog -> {
            FoldersDeleteDialog(state.selectedFolderName, actions.onDeleteFolder, actions.onDismissDeleteFolderDialog)
        }

        state.helpDialog -> {
            FoldersHelpDialog(actions.onDismissHelpDialog)
        }
    }
}

/** The callbacks [FolderActionBottomSheet] can trigger from its action list. */
data class FolderActionSheetActions(
    val onDismiss: () -> Unit,
    val onRenameClick: () -> Unit,
    val onDeleteClick: () -> Unit,
)

@Composable
private fun ColumnScope.FolderActionSheetHeader(folderName: String) {
    Row(
        modifier = Modifier.align(Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Icons.Outlined.Folder, contentDescription = null)
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = stringResource(R.string.folder_name, folderName),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
    Spacer(modifier = Modifier.height(12.dp))
}

@Composable
private fun FolderActionSheetItems(
    selectedFolderName: String?,
    coroutineScope: CoroutineScope,
    createDocumentLauncher: ManagedActivityResultLauncher<String, Uri?>,
    actions: FolderActionSheetActions,
) {
    val items =
        listOf(
            Pair(Icons.Rounded.Edit, stringResource(R.string.edit_name)),
            Pair(Icons.Rounded.Share, stringResource(R.string.export)),
            Pair(Icons.Rounded.Delete, stringResource(R.string.action_delete)),
        )
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(vertical = 8.dp, horizontal = 12.dp),
    ) {
        items.forEachIndexed { index, item ->
            Row(
                modifier =
                    Modifier
                        .clip(MaterialTheme.shapes.small)
                        .fillMaxWidth()
                        .clickable {
                            when (index) {
                                0 -> {
                                    actions.onRenameClick()
                                }

                                1 -> {
                                    var fileName = ""
                                    selectedFolderName?.let {
                                        fileName += "$it-"
                                    }
                                    fileName += LocalDateTime
                                        .now()
                                        .format(
                                            DateTimeFormatter.ofPattern("yyyy-dd-MM-HH-mm"),
                                        ) ?: ""
                                    createDocumentLauncher.launch("$fileName.sdm")
                                }

                                2 -> {
                                    actions.onDeleteClick()
                                }
                            }
                            coroutineScope.launch {
                                actions.onDismiss()
                            }
                        },
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    modifier = Modifier.padding(12.dp),
                    imageVector = item.first,
                    contentDescription = null,
                )
                Text(item.second)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FolderActionBottomSheet(
    selectedFolderName: String?,
    coroutineScope: CoroutineScope,
    createDocumentLauncher: ManagedActivityResultLauncher<String, Uri?>,
    actions: FolderActionSheetActions,
) {
    ModalBottomSheet(onDismissRequest = actions.onDismiss) {
        selectedFolderName?.let { FolderActionSheetHeader(it) }
        FolderActionSheetItems(selectedFolderName, coroutineScope, createDocumentLauncher, actions)
    }
}
