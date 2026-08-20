package com.batodev.sudoku.core.utils

import java.util.Locale
import kotlin.time.Duration

fun Duration.toFormattedString(): String {
    return this.toComponents { hours, minutes, seconds, _ ->
        if (hours > 0) {
            String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
        }
    }
}
