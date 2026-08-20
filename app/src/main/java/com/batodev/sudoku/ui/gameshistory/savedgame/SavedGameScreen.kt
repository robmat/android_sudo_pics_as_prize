package com.batodev.sudoku.ui.gameshistory.savedgame

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.batodev.sudoku.R
import com.batodev.sudoku.data.datastore.AppSettingsManager
import com.batodev.sudoku.ui.components.EmptyScreen

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun SavedGameScreen(
    navigateBack: () -> Unit,
    navigatePlayGame: (Long) -> Unit,
    navigateToFolder: (Long) -> Unit,
    viewModel: SavedGameViewModel,
) {
    val dateFormat by viewModel.dateFormat.collectAsStateWithLifecycle(
        initialValue = "",
    )
    val dateTimeFormatter by remember(dateFormat) {
        mutableStateOf(
            AppSettingsManager.dateFormat(dateFormat),
        )
    }
    Scaffold(
        topBar = { SavedGameTopBar(viewModel, navigateBack) },
    ) { innerPadding ->
        LaunchedEffect(Unit) { viewModel.updateGameDetails() }

        val hasLoadedGame =
            viewModel.savedGame != null && viewModel.boardEntity != null &&
                viewModel.parsedCurrentBoard.isNotEmpty() && viewModel.parsedInitialBoard.isNotEmpty()
        if (hasLoadedGame) {
            SavedGameContent(viewModel, innerPadding, dateTimeFormatter, navigateToFolder, navigatePlayGame)
        } else {
            EmptyScreen(stringResource(R.string.empty_screen_something_went_wrong))
        }

        SavedGameExportDialogHost(viewModel)
    }
}

@Composable
internal fun ExportDialog(
    onDismiss: () -> Unit,
    boardString: String,
    onClickCopy: () -> Unit,
) {
    AlertDialog(
        title = { Text(stringResource(R.string.export_string_title)) },
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_cancel))
            }
        },
        text = {
            Column {
                OutlinedTextField(
                    modifier =
                        Modifier
                            .padding(top = 8.dp),
                    value = boardString,
                    onValueChange = { },
                    readOnly = true,
                )
                FilledTonalButton(
                    modifier =
                        Modifier
                            .padding(top = 8.dp)
                            .align(Alignment.CenterHorizontally),
                    onClick = {
                        onClickCopy()
                        onDismiss()
                    },
                    contentPadding = ButtonDefaults.ButtonWithIconContentPadding,
                ) {
                    Icon(Icons.Rounded.ContentCopy, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.export_string_copy))
                }
            }
        },
    )
}
