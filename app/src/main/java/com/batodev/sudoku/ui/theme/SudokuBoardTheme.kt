package com.batodev.sudoku.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.batodev.sudoku.ui.theme.ColorUtils.blend
import com.batodev.sudoku.ui.theme.ColorUtils.harmonizeWithPrimary

private const val FOREGROUND_BLEND_FRACTION = 0.65f
private const val NOTES_BLEND_FRACTION = 0.4f
private const val ALT_FOREGROUND_BLEND_FRACTION = 0.5f
private const val ALT_FOREGROUND_ALPHA = 0.85f
private const val ERROR_COLOR_RED = 230
private const val ERROR_COLOR_GREEN = 67
private const val ERROR_COLOR_BLUE = 83
private const val THICK_LINE_ALPHA = 0.55f
private const val THIN_LINE_ALPHA = 0.25f

object BoardColors {
    val foregroundColor: Color
        @Composable
        get() =
            MaterialTheme.colorScheme.onSurface.blend(
                MaterialTheme.colorScheme.primary,
                fraction = FOREGROUND_BLEND_FRACTION,
            )

    val notesColor: Color
        @Composable
        get() =
            MaterialTheme.colorScheme.onSurfaceVariant.blend(
                MaterialTheme.colorScheme.secondary,
                NOTES_BLEND_FRACTION,
            )
    val altForegroundColor: Color
        @Composable
        get() =
            MaterialTheme.colorScheme.onSurfaceVariant
                .blend(
                    MaterialTheme.colorScheme.secondary,
                    ALT_FOREGROUND_BLEND_FRACTION,
                ).copy(alpha = ALT_FOREGROUND_ALPHA)

    val errorColor: Color
        @Composable
        get() = Color(ERROR_COLOR_RED, ERROR_COLOR_GREEN, ERROR_COLOR_BLUE).harmonizeWithPrimary()

    val highlightColor: Color
        @Composable
        get() = MaterialTheme.colorScheme.secondary

    val thickLineColor: Color
        @Composable
        get() = MaterialTheme.colorScheme.onSurfaceVariant.copy(THICK_LINE_ALPHA)

    val thinLineColor: Color
        @Composable
        get() = MaterialTheme.colorScheme.onSurfaceVariant.copy(THIN_LINE_ALPHA)
}

interface SudokuBoardColors {
    val foregroundColor: Color
    val notesColor: Color
    val altForegroundColor: Color
    val errorColor: Color
    val highlightColor: Color
    val thickLineColor: Color
    val thinLineColor: Color
}

/** The colors used to draw sudoku cells: digits, notes, and error/highlight tints. */
data class BoardCellColors(
    val foregroundColor: Color = Color.White,
    val notesColor: Color = Color.White,
    val altForegroundColor: Color = Color.White,
    val errorColor: Color = Color.White,
    val highlightColor: Color = Color.White,
)

/** The colors used to draw the sudoku grid lines. */
data class BoardLineColors(
    val thickLineColor: Color = Color.White,
    val thinLineColor: Color = Color.White,
)

class SudokuBoardColorsImpl(
    cellColors: BoardCellColors = BoardCellColors(),
    lineColors: BoardLineColors = BoardLineColors(),
) : SudokuBoardColors {
    override val foregroundColor: Color = cellColors.foregroundColor
    override val notesColor: Color = cellColors.notesColor
    override val altForegroundColor: Color = cellColors.altForegroundColor
    override val errorColor: Color = cellColors.errorColor
    override val highlightColor: Color = cellColors.highlightColor
    override val thickLineColor: Color = lineColors.thickLineColor
    override val thinLineColor: Color = lineColors.thinLineColor
}
