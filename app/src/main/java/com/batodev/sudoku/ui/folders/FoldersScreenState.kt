package com.batodev.sudoku.ui.folders

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.CreateDocument
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/** The document-picker launchers and pending-import state used by [FoldersScreen]. */
internal class FolderLaunchers(
    val openDocumentLauncher: ManagedActivityResultLauncher<Array<String>, Uri?>,
    val createDocumentLauncher: ManagedActivityResultLauncher<String, Uri?>,
    val contentUri: MutableState<Uri?>,
)

@Composable
internal fun rememberFolderLaunchers(
    viewModel: FoldersViewModel,
    coroutineScope: CoroutineScope,
    context: Context,
): FolderLaunchers {
    val contentUri = remember { mutableStateOf<Uri?>(null) }
    val openDocumentLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.OpenDocument(),
            onResult = {
                Log.d("FoldersScreen;openDocumentLauncher", "result uri: $it")
                contentUri.value = it
            },
        )

    val createDocumentLauncher =
        rememberLauncherForActivityResult(
            contract = CreateDocument("application/sdm"),
            onResult = { uri ->
                if (uri != null && viewModel.selectedFolder != null) {
                    coroutineScope.launch(Dispatchers.IO) {
                        context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                            outputStream.write(viewModel.generateFolderExportData())
                            outputStream.close()
                        }
                    }
                }
            },
        )
    return FolderLaunchers(openDocumentLauncher, createDocumentLauncher, contentUri)
}

/** The dialog/bottom-sheet visibility flags used across [FoldersScreen]. */
internal class FoldersDialogFlags(
    val createFolderDialog: MutableState<Boolean>,
    val renameFolderDialog: MutableState<Boolean>,
    val deleteFolderDialog: MutableState<Boolean>,
    val helpDialog: MutableState<Boolean>,
    val folderActionBottomSheet: MutableState<Boolean>,
)

@Composable
internal fun rememberFoldersDialogFlags(): FoldersDialogFlags =
    FoldersDialogFlags(
        createFolderDialog = rememberSaveable { mutableStateOf(false) },
        renameFolderDialog = rememberSaveable { mutableStateOf(false) },
        deleteFolderDialog = rememberSaveable { mutableStateOf(false) },
        helpDialog = rememberSaveable { mutableStateOf(false) },
        folderActionBottomSheet = rememberSaveable { mutableStateOf(false) },
    )
