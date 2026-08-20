package com.batodev.sudoku.ui.components.board

import android.graphics.Paint
import android.graphics.Rect
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import com.batodev.sudoku.core.Cell
import com.batodev.sudoku.core.Note

private const val SELECTED_CELL_ALPHA = 0.2f
private const val POSITION_LINE_ALPHA = 0.1f
private const val HIGHLIGHT_CELL_ALPHA = 0.3f
private const val CROSS_HIGHLIGHT_ALPHA = 0.1f
private const val NOTE_VALUE_HEX_RADIX = 16

internal fun DrawScope.drawBoardFrame(
    thickLineColor: Color,
    thickLineWidth: Float,
    maxWidth: Float,
    cornerRadius: CornerRadius,
) {
    drawRoundRect(
        color = thickLineColor,
        topLeft = Offset.Zero,
        size = Size(maxWidth, maxWidth),
        cornerRadius = cornerRadius,
        style = Stroke(width = thickLineWidth),
    )
}

internal fun DrawScope.drawSelectionHighlights(
    selectedCell: Cell,
    highlightColor: Color,
    cellSize: Float,
    maxWidth: Float,
    positionLines: Boolean,
) {
    if (selectedCell.row < 0 || selectedCell.col < 0) return
    // current cell
    drawRect(
        color = highlightColor.copy(alpha = SELECTED_CELL_ALPHA),
        topLeft = Offset(x = selectedCell.col * cellSize, y = selectedCell.row * cellSize),
        size = Size(cellSize, cellSize),
    )
    if (!positionLines) return
    // vertical position line
    drawRect(
        color = highlightColor.copy(alpha = POSITION_LINE_ALPHA),
        topLeft = Offset(x = selectedCell.col * cellSize, y = 0f),
        size = Size(cellSize, maxWidth),
    )
    // horizontal position line
    drawRect(
        color = highlightColor.copy(alpha = POSITION_LINE_ALPHA),
        topLeft = Offset(x = 0f, y = selectedCell.row * cellSize),
        size = Size(maxWidth, cellSize),
    )
}

internal fun DrawScope.drawIdenticalNumbersHighlight(
    board: List<List<Cell>>,
    size: Int,
    selectedCell: Cell,
    highlightColor: Color,
    cellSize: Float,
) {
    for (i in 0 until size) {
        for (j in 0 until size) {
            val cell = board[i][j]
            if (cell.value != selectedCell.value || cell.value == 0) continue
            drawRect(
                color = highlightColor.copy(alpha = SELECTED_CELL_ALPHA),
                topLeft = Offset(x = cell.col * cellSize, y = cell.row * cellSize),
                size = Size(cellSize, cellSize),
            )
        }
    }
}

internal fun DrawScope.drawCellsToHighlight(
    cellsToHighlight: List<Cell>?,
    highlightColor: Color,
    cellSize: Float,
) {
    cellsToHighlight?.forEach {
        drawRect(
            color = highlightColor.copy(alpha = HIGHLIGHT_CELL_ALPHA),
            topLeft = Offset(x = it.col * cellSize, y = it.row * cellSize),
            size = Size(cellSize, cellSize),
        )
    }
}

internal fun DrawScope.drawCrossHighlight(
    size: Int,
    highlightColor: Color,
    cellSize: Float,
) {
    val sectionHeight = getSectionHeightForSize(size)
    val sectionWidth = getSectionWidthForSize(size)
    for (i in 0 until size / sectionWidth) {
        for (j in 0 until size / sectionHeight) {
            if ((i % 2 == 0) == (j % 2 == 0)) continue
            drawRect(
                color = highlightColor.copy(alpha = CROSS_HIGHLIGHT_ALPHA),
                topLeft = Offset(x = i * sectionWidth * cellSize, y = j * sectionHeight * cellSize),
                size = Size(cellSize * sectionWidth, cellSize * sectionHeight),
            )
        }
    }
}

internal data class DrawNumbersOptions(
    val highlightErrors: Boolean,
    val questions: Boolean,
)

internal fun DrawScope.drawNumbers(
    size: Int,
    board: List<List<Cell>>,
    paints: BoardPaints,
    options: DrawNumbersOptions,
    cellSize: Float,
) {
    drawIntoCanvas { canvas ->
        for (i in 0 until size) {
            for (j in 0 until size) {
                if (board[i][j].value != 0) {
                    val paint =
                        when {
                            board[i][j].error && options.highlightErrors -> paints.errorTextPaint
                            board[i][j].locked -> paints.lockedTextPaint
                            else -> paints.textPaint
                        }

                    val textToDraw =
                        if (options.questions) {
                            "?"
                        } else {
                            board[i][j].value.toString(NOTE_VALUE_HEX_RADIX).uppercase()
                        }
                    val textBounds = Rect()
                    paints.textPaint.getTextBounds(textToDraw, 0, 1, textBounds)
                    val textWidth = paint.measureText(textToDraw)

                    canvas.nativeCanvas.drawText(
                        textToDraw,
                        board[i][j].col * cellSize + (cellSize - textWidth) / 2f,
                        board[i][j].row * cellSize + (cellSize + textBounds.height()) / 2f,
                        paint,
                    )
                }
            }
        }
    }
}

internal data class NoteCellMetrics(
    val cellSize: Float,
    val cellSizeDivWidth: Float,
    val cellSizeDivHeight: Float,
)

internal fun DrawScope.drawNotes(
    size: Int,
    paint: Paint,
    notes: List<Note>,
    metrics: NoteCellMetrics,
) {
    val noteBounds = Rect()
    paint.getTextBounds("1", 0, 1, noteBounds)

    drawIntoCanvas { canvas ->
        notes.forEach { note ->
            val textToDraw = note.value.toString(NOTE_VALUE_HEX_RADIX).uppercase()
            val noteTextMeasure = paint.measureText(textToDraw)
            canvas.nativeCanvas.drawText(
                textToDraw,
                note.col * metrics.cellSize + metrics.cellSizeDivWidth / 2f + (
                    metrics.cellSizeDivWidth *
                        getNoteRowNumber(
                            note.value,
                            size,
                        )
                ) - noteTextMeasure / 2f,
                note.row * metrics.cellSize + metrics.cellSizeDivHeight / 2f + (
                    metrics.cellSizeDivHeight *
                        getNoteColumnNumber(
                            note.value,
                            size,
                        )
                ) + noteBounds.height() / 2f,
                paint,
            )
        }
    }
}
