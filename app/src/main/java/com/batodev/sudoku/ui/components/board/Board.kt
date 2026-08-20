package com.batodev.sudoku.ui.components.board

import androidx.compose.foundation.Canvas
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.batodev.sudoku.core.Cell
import com.batodev.sudoku.core.Note
import com.batodev.sudoku.core.qqwing.GameType
import com.batodev.sudoku.core.utils.SudokuParser
import com.batodev.sudoku.ui.theme.SudokuBoardColors
import com.batodev.sudoku.ui.theme.SudokuTheme
import com.batodev.sudoku.ui.util.LightDarkPreview
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.sqrt

private const val MAIN_TEXT_SIZE_6X6_SP = 32
private const val MAIN_TEXT_SIZE_9X9_SP = 26
private const val MAIN_TEXT_SIZE_12X12_SP = 24
private const val MAIN_TEXT_SIZE_DEFAULT_SP = 14
private const val NOTE_TEXT_SIZE_6X6_SP = 18
private const val NOTE_TEXT_SIZE_9X9_SP = 12
private const val NOTE_TEXT_SIZE_12X12_SP = 7
private const val NOTE_TEXT_SIZE_DEFAULT_SP = 14
private const val LINE_WIDTH_DP = 1.3
private const val BOARD_CORNER_RADIUS_PX = 15f

private fun defaultMainTextSizeFor(size: Int): TextUnit =
    when (size) {
        BOARD_SIZE_6X6 -> MAIN_TEXT_SIZE_6X6_SP.sp
        BOARD_SIZE_9X9 -> MAIN_TEXT_SIZE_9X9_SP.sp
        BOARD_SIZE_12X12 -> MAIN_TEXT_SIZE_12X12_SP.sp
        else -> MAIN_TEXT_SIZE_DEFAULT_SP.sp
    }

private fun defaultNoteTextSizeFor(size: Int): TextUnit =
    when (size) {
        BOARD_SIZE_6X6 -> NOTE_TEXT_SIZE_6X6_SP.sp
        BOARD_SIZE_9X9 -> NOTE_TEXT_SIZE_9X9_SP.sp
        BOARD_SIZE_12X12 -> NOTE_TEXT_SIZE_12X12_SP.sp
        else -> NOTE_TEXT_SIZE_DEFAULT_SP.sp
    }

/**
 * Text sizes used to render the board's main digits and pencil-mark notes. Either can be left
 * `null` to fall back to the size-appropriate default (see [defaultMainTextSizeFor] and
 * [defaultNoteTextSizeFor]).
 */
data class BoardTextSizes(
    val mainTextSize: TextUnit? = null,
    val noteTextSize: TextUnit? = null,
)

/** Toggles that control what [Board] highlights/renders, beyond the puzzle data itself. */
data class BoardDisplayOptions(
    val identicalNumbersHighlight: Boolean = true,
    val errorsHighlight: Boolean = true,
    val positionLines: Boolean = true,
    val enabled: Boolean = true,
    val questions: Boolean = false,
    val renderNotes: Boolean = true,
    val zoomable: Boolean = false,
    val crossHighlight: Boolean = false,
)

/** Cell selection/interaction callbacks and the currently-selected/highlighted cells. */
data class BoardInteraction(
    val selectedCell: Cell,
    val onClick: (Cell) -> Unit,
    val onLongClick: (Cell) -> Unit = { },
    val cellsToHighlight: List<Cell>? = null,
)

/** Everything that controls how [Board] looks: colors, text sizes and display toggles. */
data class BoardStyle(
    val boardColors: SudokuBoardColors,
    val textSizes: BoardTextSizes? = null,
    val displayOptions: BoardDisplayOptions = BoardDisplayOptions(),
)

/** The puzzle data [Board] renders: the [board] itself, its [size], and any pencil-mark [notes]. */
data class BoardData(
    val board: List<List<Cell>>,
    val size: Int = board.size,
    val notes: List<Note>? = null,
)

/** Precomputed geometry/paint state needed to draw one frame of the sudoku board canvas. */
private data class BoardCanvasContext(
    val data: BoardData,
    val interaction: BoardInteraction,
    val style: BoardStyle,
    val maxWidth: Float,
    val cellSize: Float,
    val cellSizeDivWidth: Float,
    val cellSizeDivHeight: Float,
    val vertThick: Int,
    val horThick: Int,
    val thinLineWidth: Float,
    val thickLineWidth: Float,
    val paints: BoardPaints,
)

private fun DrawScope.drawSudokuBoardContent(context: BoardCanvasContext) {
    val (board, size, notes) = context.data
    val boardColors = context.style.boardColors
    val displayOptions = context.style.displayOptions
    val thickLineColor = boardColors.thickLineColor
    val thinLineColor = boardColors.thinLineColor
    val highlightColor = boardColors.highlightColor
    val maxWidth = context.maxWidth
    val cellSize = context.cellSize

    drawSelectionHighlights(
        context.interaction.selectedCell,
        highlightColor,
        cellSize,
        maxWidth,
        displayOptions.positionLines,
    )
    if (displayOptions.identicalNumbersHighlight) {
        drawIdenticalNumbersHighlight(board, size, context.interaction.selectedCell, highlightColor, cellSize)
    }
    drawCellsToHighlight(context.interaction.cellsToHighlight, highlightColor, cellSize)

    drawBoardFrame(
        thickLineColor = thickLineColor,
        thickLineWidth = context.thickLineWidth,
        maxWidth = maxWidth,
        cornerRadius = CornerRadius(BOARD_CORNER_RADIUS_PX, BOARD_CORNER_RADIUS_PX),
    )

    drawSudokuGridLines(
        geometry = GridGeometry(size, cellSize, maxWidth, context.horThick, context.vertThick),
        style = GridLineStyle(thickLineColor, thinLineColor, context.thickLineWidth, context.thinLineWidth),
        boundsCheckVerticalLines = true,
    )

    drawNumbers(
        size = size,
        board = board,
        paints = context.paints,
        options =
            DrawNumbersOptions(
                highlightErrors = displayOptions.errorsHighlight,
                questions = displayOptions.questions,
            ),
        cellSize = cellSize,
    )

    if (!notes.isNullOrEmpty() && !displayOptions.questions && displayOptions.renderNotes) {
        drawNotes(
            size = size,
            paint = context.paints.notePaint,
            notes = notes,
            metrics = NoteCellMetrics(cellSize, context.cellSizeDivWidth, context.cellSizeDivHeight),
        )
    }

    // doesn't look good on 6x6
    if (displayOptions.crossHighlight && size != BOARD_SIZE_6X6) {
        drawCrossHighlight(size, highlightColor, cellSize)
    }
}

@Composable
fun Board(
    data: BoardData,
    interaction: BoardInteraction,
    style: BoardStyle,
    modifier: Modifier = Modifier,
) {
    val size = data.size
    val mainTextSize = style.textSizes?.mainTextSize ?: defaultMainTextSizeFor(size)
    val noteTextSize = style.textSizes?.noteTextSize ?: defaultNoteTextSizeFor(size)
    val boardColors = style.boardColors
    SudokuBoardContainer(modifier = modifier) {
        val maxWidth = constraints.maxWidth.toFloat()

        // single cell size
        val cellSize by remember(size) { mutableFloatStateOf(maxWidth / size.toFloat()) }
        // div for notes in one row in cell
        val cellSizeDivWidth by remember(size) { mutableFloatStateOf(cellSize / ceil(sqrt(size.toFloat()))) }
        // div for note in one column in cell
        val cellSizeDivHeight by remember(size) { mutableFloatStateOf(cellSize / floor(sqrt(size.toFloat()))) }

        val (vertThick, horThick) = rememberGridThickIntervals(size)

        val thinLineWidth = with(LocalDensity.current) { LINE_WIDTH_DP.dp.toPx() }
        val thickLineWidth = with(LocalDensity.current) { LINE_WIDTH_DP.dp.toPx() }

        val paints = rememberBoardPaints(mainTextSize, noteTextSize, boardColors)

        val boardInteractionModifier =
            rememberBoardInteractionModifier(
                board = data.board,
                cellSizeProvider = { cellSize },
                maxWidth = maxWidth,
                displayOptions = style.displayOptions,
                interaction = interaction,
            )

        val canvasContext =
            BoardCanvasContext(
                data = data,
                interaction = interaction,
                style = style,
                maxWidth = maxWidth,
                cellSize = cellSize,
                cellSizeDivWidth = cellSizeDivWidth,
                cellSizeDivHeight = cellSizeDivHeight,
                vertThick = vertThick,
                horThick = horThick,
                thinLineWidth = thinLineWidth,
                thickLineWidth = thickLineWidth,
                paints = paints,
            )

        Canvas(modifier = boardInteractionModifier) {
            drawSudokuBoardContent(canvasContext)
        }
    }
}

@LightDarkPreview
@Composable
private fun BoardPreviewLight() {
    SudokuTheme {
        Surface {
            val sudokuParser = SudokuParser()
            val board by remember {
                mutableStateOf(
                    sudokuParser
                        .parseBoard(
                            board = "....1........4.............7...........9........68...............5...............",
                            gameType = GameType.Default9x9,
                            emptySeparator = '.',
                        ).toList(),
                )
            }
            val notes = sudokuParser.parseNotes("2,3,1;2,3,5;")
            Board(
                data = BoardData(board = board, notes = notes),
                interaction = BoardInteraction(selectedCell = Cell(-1, -1), onClick = { }),
                style = BoardStyle(boardColors = previewSudokuBoardColors()),
            )
        }
    }
}
