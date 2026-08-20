package com.batodev.sudoku.ui.components.board

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.dp
import com.batodev.sudoku.ui.theme.BoardCellColors
import com.batodev.sudoku.ui.theme.BoardColors
import com.batodev.sudoku.ui.theme.BoardLineColors
import com.batodev.sudoku.ui.theme.SudokuBoardColorsImpl
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.sqrt

private const val CONTAINER_PADDING_DP = 4

/**
 * Common [BoxWithConstraints] wrapper used by both [Board] and [BoardPreview] to lay out a
 * square sudoku grid area.
 */
@Composable
internal fun SudokuBoardContainer(
    modifier: Modifier = Modifier,
    content: @Composable BoxWithConstraintsScope.() -> Unit,
) {
    BoxWithConstraints(
        modifier =
            modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .padding(CONTAINER_PADDING_DP.dp),
        content = content,
    )
}

/**
 * The interval (in cells) at which thick section-separator lines should be drawn, for both the
 * vertical and horizontal grid lines, derived from the board [size].
 */
@Composable
internal fun rememberGridThickIntervals(size: Int): Pair<Int, Int> {
    val vertThick by remember(size) { mutableIntStateOf(floor(sqrt(size.toFloat())).toInt()) }
    val horThick by remember(size) { mutableIntStateOf(ceil(sqrt(size.toFloat())).toInt()) }
    return vertThick to horThick
}

/** The board dimensions needed to lay out sudoku grid lines. */
internal data class GridGeometry(
    val size: Int,
    val cellSize: Float,
    val maxWidth: Float,
    val horThick: Int,
    val vertThick: Int,
)

/** The colors and stroke widths used to draw sudoku grid lines. */
internal data class GridLineStyle(
    val thickLineColor: Color,
    val thinLineColor: Color,
    val thickLineWidth: Float,
    val thinLineWidth: Float,
)

/**
 * Draws the horizontal and vertical sudoku grid lines, alternating between thick section
 * separators and thin cell separators.
 *
 * [boundsCheckVerticalLines] preserves a pre-existing (effectively always-true) safety check that
 * only [Board] performed on its vertical lines.
 */
internal fun DrawScope.drawSudokuGridLines(
    geometry: GridGeometry,
    style: GridLineStyle,
    boundsCheckVerticalLines: Boolean = false,
) {
    // horizontal line
    for (i in 1 until geometry.size) {
        val isThickLine = i % geometry.horThick == 0
        drawLine(
            color = if (isThickLine) style.thickLineColor else style.thinLineColor,
            start = Offset(geometry.cellSize * i.toFloat(), 0f),
            end = Offset(geometry.cellSize * i.toFloat(), geometry.maxWidth),
            strokeWidth = if (isThickLine) style.thickLineWidth else style.thinLineWidth,
        )
    }
    // vertical line
    for (i in 1 until geometry.size) {
        val isThickLine = i % geometry.vertThick == 0
        if (!boundsCheckVerticalLines || geometry.maxWidth >= geometry.cellSize * i) {
            drawLine(
                color = if (isThickLine) style.thickLineColor else style.thinLineColor,
                start = Offset(0f, geometry.cellSize * i.toFloat()),
                end = Offset(geometry.maxWidth, geometry.cellSize * i.toFloat()),
                strokeWidth = if (isThickLine) style.thickLineWidth else style.thinLineWidth,
            )
        }
    }
}

/** Builds the [SudokuBoardColorsImpl] used by board preview composables from [BoardColors]. */
@Composable
internal fun previewSudokuBoardColors(): SudokuBoardColorsImpl =
    SudokuBoardColorsImpl(
        cellColors =
            BoardCellColors(
                foregroundColor = BoardColors.foregroundColor,
                notesColor = BoardColors.notesColor,
                altForegroundColor = BoardColors.altForegroundColor,
                errorColor = BoardColors.errorColor,
                highlightColor = BoardColors.highlightColor,
            ),
        lineColors =
            BoardLineColors(
                thickLineColor = BoardColors.thickLineColor,
                thinLineColor = BoardColors.thinLineColor,
            ),
    )
