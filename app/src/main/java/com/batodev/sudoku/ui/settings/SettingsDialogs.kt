package com.batodev.sudoku.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.batodev.sudoku.R
import com.batodev.sudoku.ui.components.EdgeIndicatedLazyColumn

@Composable
fun SelectionDialog(
    title: String,
    selections: List<String>,
    onSelect: (Int) -> Unit,
    onDismiss: () -> Unit,
    selected: Int = 0,
) {
    AlertDialog(
        title = {
            Column(Modifier.fillMaxWidth()) {
                Text(
                    text = title,
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                )
            }
        },
        text = {
            Column {
                selections.forEachIndexed { index, text ->
                    RadioListRow(
                        selected = selected == index,
                        text = text,
                        onClick = {
                            onSelect(index)
                            onDismiss()
                        },
                    )
                }
            }
        },
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_cancel))
            }
        },
    )
}

@Composable
fun SelectionDialog(
    title: String,
    entries: Map<String, String>,
    selected: String,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    EntriesListDialog(title = title, onDismiss = onDismiss) {
        entryRadioItems(entries, selected, onSelect)
    }
}

/** The entries and current selection shown by [DateFormatDialog]. */
data class DateFormatDialogInfo(
    val title: String,
    val entries: Map<String, String>,
    val customDateFormatText: String,
    val selected: String,
)

@Composable
fun DateFormatDialog(
    info: DateFormatDialogInfo,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    EntriesListDialog(title = info.title, onDismiss = onDismiss) {
        entryRadioItems(info.entries, info.selected, onSelect)
        item {
            RadioListRow(
                selected = !info.entries.containsKey(info.selected),
                text = info.customDateFormatText,
                onClick = { onSelect("custom") },
            )
        }
    }
}

/** A [RadioListRow] per [entries] value, shared by [SelectionDialog] and [DateFormatDialog]. */
private fun LazyListScope.entryRadioItems(
    entries: Map<String, String>,
    selected: String,
    onSelect: (String) -> Unit,
) {
    items(entries.toList()) { item ->
        RadioListRow(
            selected = selected == item.first,
            text = item.second,
            onClick = { onSelect(item.first) },
        )
    }
}

/**
 * The [AlertDialog] shell shared by [SelectionDialog] (the [Map]-based overload) and
 * [DateFormatDialog]: a title, a scrollable [EdgeIndicatedLazyColumn] of entries, supplied via
 * [content], and a single cancel button.
 */
@Composable
private fun EntriesListDialog(
    title: String,
    onDismiss: () -> Unit,
    content: LazyListScope.() -> Unit,
) {
    AlertDialog(
        title = {
            Column(Modifier.fillMaxWidth()) {
                Text(
                    text = title,
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                )
            }
        },
        text = {
            EdgeIndicatedLazyColumn(content = content)
        },
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_cancel))
            }
        },
    )
}

/** A single selectable row: a [RadioButton] followed by [text], used inside selection dialogs. */
@Composable
private fun RadioListRow(
    selected: Boolean,
    text: String,
    onClick: () -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.small)
                .clickable(onClick = onClick),
        verticalAlignment = CenterVertically,
    ) {
        RadioButton(
            selected = selected,
            onClick = onClick,
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}

/** The text field state shown by [SetDateFormatPatternDialog]. */
data class CustomDateFormatState(
    val customDateFormat: String,
    val invalidCustomDateFormat: Boolean,
    val datePreview: String = "",
)

/** The callbacks used by [SetDateFormatPatternDialog]. */
data class CustomDateFormatCallbacks(
    val onConfirm: () -> Unit,
    val onDismissRequest: () -> Unit,
    val onTextValueChange: (String) -> Unit,
)

@Composable
fun SetDateFormatPatternDialog(
    state: CustomDateFormatState,
    callbacks: CustomDateFormatCallbacks,
) {
    AlertDialog(
        title = {
            Column(Modifier.fillMaxWidth()) {
                Text(
                    text = stringResource(R.string.pref_date_format_custom),
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                )
            }
        },
        text = {
            Column {
                Text(
                    text = stringResource(R.string.pref_date_format_custom_summ),
                    modifier = Modifier.padding(bottom = 8.dp),
                )
                Text(
                    text = state.datePreview,
                    modifier = Modifier.padding(bottom = 6.dp),
                )
                OutlinedTextField(
                    value = state.customDateFormat,
                    onValueChange = callbacks.onTextValueChange,
                    singleLine = true,
                    isError = state.invalidCustomDateFormat,
                    label = {
                        Text(stringResource(R.string.pref_date_format_custom_textfield_label))
                    },
                    keyboardActions =
                        KeyboardActions(
                            onDone = { callbacks.onConfirm() },
                        ),
                )
            }
        },
        onDismissRequest = callbacks.onDismissRequest,
        confirmButton = {
            TextButton(
                onClick = { callbacks.onConfirm() },
            ) {
                Text(stringResource(R.string.action_save))
            }
        },
        dismissButton = {
            TextButton(onClick = callbacks.onDismissRequest) {
                Text(stringResource(R.string.action_cancel))
            }
        },
    )
}
