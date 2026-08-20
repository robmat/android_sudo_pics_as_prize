package com.batodev.sudoku.ui.components.board

import android.content.Context
import android.graphics.Paint
import android.util.TypedValue
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.TextUnit
import com.batodev.sudoku.ui.theme.SudokuBoardColors

private const val ERROR_TEXT_COLOR_RED = 230
private const val ERROR_TEXT_COLOR_GREEN = 67
private const val ERROR_TEXT_COLOR_BLUE = 83

/** The [Paint]s used by [Board] to render its main digits, errors, locked numbers and notes. */
internal data class BoardPaints(
    val textPaint: Paint,
    val errorTextPaint: Paint,
    val lockedTextPaint: Paint,
    val notePaint: Paint
)

private fun buildTextPaint(color: Color, textSizePx: Float): Paint = Paint().apply {
    this.color = color.toArgb()
    isAntiAlias = true
    textSize = textSizePx
}

/** The colors [rememberBoardPaints] uses, bundled so recomposing the [LaunchedEffect] below only
 * needs a single key. */
private data class BoardPaintColors(
    val foregroundColor: Color,
    val errorColor: Color,
    val altForegroundColor: Color,
    val notesColor: Color
)

private fun dimensionPx(context: Context, sp: Float): Float =
    TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, context.resources.displayMetrics)

/**
 * Builds and remembers the [BoardPaints] for [Board], recreating them whenever the text sizes or
 * colors change (since [Paint] is a mutable Android object, not something Compose can observe
 * changes to directly).
 */
@Composable
internal fun rememberBoardPaints(
    mainTextSize: TextUnit,
    noteTextSize: TextUnit,
    boardColors: SudokuBoardColors
): BoardPaints {
    val colors = BoardPaintColors(
        foregroundColor = boardColors.foregroundColor,
        errorColor = boardColors.errorColor,
        altForegroundColor = boardColors.altForegroundColor,
        notesColor = boardColors.notesColor
    )

    var fontSizePx = with(LocalDensity.current) { mainTextSize.toPx() }
    var noteSizePx = with(LocalDensity.current) { noteTextSize.toPx() }

    var textPaint by remember { mutableStateOf(buildTextPaint(colors.foregroundColor, fontSizePx)) }
    var errorTextPaint by remember { mutableStateOf(buildTextPaint(colors.errorColor, fontSizePx)) }
    var lockedTextPaint by remember { mutableStateOf(buildTextPaint(colors.altForegroundColor, fontSizePx)) }
    var notePaint by remember { mutableStateOf(buildTextPaint(colors.notesColor, noteSizePx)) }

    val context = LocalContext.current
    LaunchedEffect(mainTextSize, noteTextSize, boardColors) {
        fontSizePx = dimensionPx(context, mainTextSize.value)
        noteSizePx = dimensionPx(context, noteTextSize.value)
        textPaint = buildTextPaint(colors.foregroundColor, fontSizePx)
        notePaint = buildTextPaint(colors.notesColor, noteSizePx)
        errorTextPaint = buildTextPaint(
            Color(ERROR_TEXT_COLOR_RED, ERROR_TEXT_COLOR_GREEN, ERROR_TEXT_COLOR_BLUE),
            fontSizePx
        )
        lockedTextPaint = buildTextPaint(colors.altForegroundColor, fontSizePx)
    }

    return BoardPaints(textPaint, errorTextPaint, lockedTextPaint, notePaint)
}
