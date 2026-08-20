package com.batodev.sudoku.ui.settings

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.batodev.sudoku.R
import java.time.ZonedDateTime
import java.time.chrono.IsoChronology
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.time.format.FormatStyle
import java.util.Locale

internal val DateFormats =
    listOf(
        "",
        "dd/MM/yy",
        "dd.MM.yy",
        "MM/dd/yy",
        "yyyy-MM-dd",
        "dd MMM yyyy",
        "MMM dd, yyyy",
    )

@Composable
internal fun buildDateFormatEntries(): Map<String, String> =
    DateFormats.associateWith { dateFormatEntry ->
        val dateString =
            ZonedDateTime.now().format(
                when (dateFormatEntry) {
                    "" -> {
                        DateTimeFormatter.ofPattern(
                            DateTimeFormatterBuilder.getLocalizedDateTimePattern(
                                FormatStyle.SHORT,
                                null,
                                IsoChronology.INSTANCE,
                                Locale.getDefault(),
                            ),
                        )
                    }

                    else -> {
                        DateTimeFormatter.ofPattern(dateFormatEntry)
                    }
                },
            )
        "${dateFormatEntry.ifEmpty { stringResource(R.string.label_default) }} ($dateString)"
    }

@Composable
internal fun buildCustomDateFormatText(dateFormat: String): String =
    if (!DateFormats.contains(dateFormat)) {
        "$dateFormat (${
            ZonedDateTime.now().format(DateTimeFormatter.ofPattern(dateFormat))
        })"
    } else {
        stringResource(R.string.pref_date_format_custom_label)
    }
