package com.batodev.sudoku.ui.components.board

import android.graphics.Paint
import android.graphics.Rect
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.batodev.sudoku.core.Cell
import com.batodev.sudoku.ui.theme.SudokuBoardColors
import com.batodev.sudoku.ui.theme.SudokuTheme
import com.batodev.sudoku.ui.util.LightDarkPreview

private val TEXT_SIZE_6X6 = 16.sp
private val TEXT_SIZE_9X9 = 11.sp
private val TEXT_SIZE_12X12 = 9.sp
private val TEXT_SIZE_DEFAULT = 22.sp
private const val BOARD_CORNER_RADIUS = 10f
private const val BOARD_STROKE_WIDTH_DP = 1.1
private const val THIN_LINE_WIDTH_DP = 0.6
private const val THICK_LINE_WIDTH_DP = 1.1
private const val SINGLE_DIGIT_SAMPLE_TEXT = "1"

private fun defaultMainTextSize(size: Int): TextUnit = when (size) {
    BOARD_SIZE_6X6 -> TEXT_SIZE_6X6
    BOARD_SIZE_9X9 -> TEXT_SIZE_9X9
    BOARD_SIZE_12X12 -> TEXT_SIZE_12X12
    else -> TEXT_SIZE_DEFAULT
}

/** The data a [BoardPreview] renders: either a parsed [board] or a raw [boardString]. */
data class BoardPreviewContent(
    val boardColors: SudokuBoardColors,
    val size: Int = 9,
    val boardString: String? = null,
    val board: List<List<Cell>>? = null,
    val mainTextSize: TextUnit? = null
)

/** Everything [drawBoardPreviewNumbers] needs to paint the digit glyphs onto the canvas. */
private class BoardPreviewTextDrawing(
    val canvas: android.graphics.Canvas,
    val cellSize: Float,
    val textWidth: Float,
    val textPaint: Paint,
    val textBounds: Rect
)

private fun BoardPreviewTextDrawing.drawParsedBoardNumbers(size: Int, board: List<List<Cell>>) {
    for (i in 0 until size) {
        for (j in 0 until size) {
            if (board[i][j].value == 0) continue
            canvas.drawText(
                board[i][j].value.toString(),
                board[i][j].col * cellSize + (cellSize - textWidth) / 2f,
                (board[i][j].row * cellSize + cellSize) - (cellSize - textBounds.height()) / 2f,
                textPaint
            )
        }
    }
}

private fun BoardPreviewTextDrawing.drawBoardStringNumbers(size: Int, boardString: String) {
    for (i in 0 until size) {
        for (j in 0 until size) {
            if (boardString[size * j + i] == '0') continue
            canvas.drawText(
                boardString[size * j + i].uppercase(),
                i * cellSize + (cellSize - textWidth) / 2f,
                j * cellSize + cellSize - (cellSize - textBounds.height()) / 2f,
                textPaint
            )
        }
    }
}

private fun drawBoardPreviewNumbers(drawing: BoardPreviewTextDrawing, content: BoardPreviewContent) {
    val size = content.size
    val board = content.board
    val boardString = content.boardString
    if (board != null) {
        drawing.drawParsedBoardNumbers(size, board)
    } else if (boardString != null && boardString.length == size * size) {
        drawing.drawBoardStringNumbers(size, boardString)
    }
}

@Composable
fun BoardPreview(
    content: BoardPreviewContent,
    modifier: Modifier = Modifier
) {
    val size = content.size
    val mainTextSize = content.mainTextSize ?: defaultMainTextSize(size)
    SudokuBoardContainer(modifier = modifier) {
        val maxWidth = constraints.maxWidth.toFloat()

        val cellSize by remember(size) { mutableFloatStateOf(maxWidth / size.toFloat()) }
        val foregroundColor = content.boardColors.altForegroundColor
        val thickLineColor = content.boardColors.thickLineColor
        val thinLineColor = content.boardColors.thinLineColor

        val (vertThick, horThick) = rememberGridThickIntervals(size)

        val fontSizePx = with(LocalDensity.current) { mainTextSize.toPx() }

        val textPaint by remember {
            mutableStateOf(
                Paint().apply {
                    color = foregroundColor.toArgb()
                    isAntiAlias = true
                    textSize = fontSizePx
                }
            )
        }
        val textWidth by remember { mutableFloatStateOf(textPaint.measureText(SINGLE_DIGIT_SAMPLE_TEXT)) }
        val boardStrokeWidth = with(LocalDensity.current) { BOARD_STROKE_WIDTH_DP.dp.toPx() }
        val thinLineWidth = with(LocalDensity.current) { THIN_LINE_WIDTH_DP.dp.toPx() }
        val thickLineWidth = with(LocalDensity.current) { THICK_LINE_WIDTH_DP.dp.toPx() }
        Canvas(
            modifier = Modifier
                .fillMaxSize()
        ) {
            drawRoundRect(
                color = thickLineColor,
                topLeft = Offset.Zero,
                size = Size(maxWidth, maxWidth),
                cornerRadius = CornerRadius(BOARD_CORNER_RADIUS, BOARD_CORNER_RADIUS),
                style = Stroke(width = boardStrokeWidth)
            )

            drawSudokuGridLines(
                geometry = GridGeometry(size, cellSize, maxWidth, horThick, vertThick),
                style = GridLineStyle(thickLineColor, thinLineColor, thickLineWidth, thinLineWidth)
            )

            val textBounds = Rect()
            textPaint.getTextBounds(SINGLE_DIGIT_SAMPLE_TEXT, 0, 1, textBounds)

            drawIntoCanvas { canvas ->
                val drawing = BoardPreviewTextDrawing(canvas.nativeCanvas, cellSize, textWidth, textPaint, textBounds)
                drawBoardPreviewNumbers(drawing, content)
            }
        }
    }
}

@LightDarkPreview
@Composable
private fun BoardPreviewPreview() {
    SudokuTheme {
        Surface {
            BoardPreview(
                content = BoardPreviewContent(
                    boardString = "0000100000040000000000000700000000000900000000680000000000000005000000000000000",
                    boardColors = previewSudokuBoardColors()
                )
            )
        }
    }
}
