package com.batodev.sudoku.ui.components.board

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableFloatState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import com.batodev.sudoku.core.Cell
import kotlin.math.floor

private const val MIN_ZOOM = 1f
private const val MAX_ZOOM = 3f

private data class ZoomPanState(
    val zoom: MutableFloatState,
    val offset: MutableState<Offset>,
)

/**
 * Builds the [Modifier] that gives [Board] its tap/long-press-to-select and (if
 * [BoardDisplayOptions.zoomable]) pinch-to-zoom/pan behavior.
 *
 * [cellSizeProvider] is a lambda (rather than a plain `Float`) so that the gesture callbacks -
 * which are only re-created when [BoardDisplayOptions.enabled] or [board] change - always read
 * the board's *current* cell size rather than the value captured when the gesture detector was
 * last (re)installed.
 */
@Composable
internal fun rememberBoardInteractionModifier(
    board: List<List<Cell>>,
    cellSizeProvider: () -> Float,
    maxWidth: Float,
    displayOptions: BoardDisplayOptions,
    interaction: BoardInteraction,
): Modifier {
    val enabled = displayOptions.enabled
    val zoomPan =
        ZoomPanState(
            zoom = remember(enabled) { mutableFloatStateOf(1f) },
            offset = remember(enabled) { mutableStateOf(Offset.Zero) },
        )

    val tapModifier = rememberTapModifier(board, cellSizeProvider, enabled, interaction, zoomPan)
    val zoomModifier = rememberZoomModifier(maxWidth, enabled, zoomPan)

    return if (displayOptions.zoomable) tapModifier.then(zoomModifier) else tapModifier
}

private fun rememberTapModifier(
    board: List<List<Cell>>,
    cellSizeProvider: () -> Float,
    enabled: Boolean,
    interaction: BoardInteraction,
    zoomPan: ZoomPanState,
): Modifier {
    val onClick = interaction.onClick
    val onLongClick = interaction.onLongClick
    return Modifier
        .fillMaxSize()
        .pointerInput(key1 = enabled, key2 = board) {
            detectTapGestures(
                onTap = {
                    if (enabled) {
                        val cellSize = cellSizeProvider()
                        val totalOffset = it / zoomPan.zoom.floatValue + zoomPan.offset.value
                        val row =
                            floor((totalOffset.y) / cellSize)
                                .toInt()
                                .coerceIn(board.indices)
                        val column =
                            floor((totalOffset.x) / cellSize)
                                .toInt()
                                .coerceIn(board.indices)
                        onClick(board[row][column])
                    }
                },
                onLongPress = {
                    if (enabled) {
                        val cellSize = cellSizeProvider()
                        val totalOffset = it / zoomPan.zoom.floatValue + zoomPan.offset.value
                        val row = floor((totalOffset.y) / cellSize).toInt()
                        val column = floor((totalOffset.x) / cellSize).toInt()
                        onLongClick(board[row][column])
                    }
                },
            )
        }
}

private fun rememberZoomModifier(
    maxWidth: Float,
    enabled: Boolean,
    zoomPan: ZoomPanState,
): Modifier =
    Modifier
        .pointerInput(enabled) {
            detectTransformGestures(
                onGesture = { gestureCentroid, gesturePan, gestureZoom, _ ->
                    if (enabled) {
                        applyZoomGesture(zoomPan, maxWidth, gestureCentroid, gesturePan, gestureZoom)
                    }
                },
            )
        }.graphicsLayer {
            translationX = -zoomPan.offset.value.x * zoomPan.zoom.floatValue
            translationY = -zoomPan.offset.value.y * zoomPan.zoom.floatValue
            scaleX = zoomPan.zoom.floatValue
            scaleY = zoomPan.zoom.floatValue
            TransformOrigin(0f, 0f).also { transformOrigin = it }
        }

private fun applyZoomGesture(
    zoomPan: ZoomPanState,
    maxWidth: Float,
    gestureCentroid: Offset,
    gesturePan: Offset,
    gestureZoom: Float,
) {
    val oldScale = zoomPan.zoom.floatValue
    val newScale = (oldScale * gestureZoom).coerceIn(MIN_ZOOM..MAX_ZOOM)
    var offset =
        (zoomPan.offset.value + gestureCentroid / oldScale) -
            (gestureCentroid / newScale + gesturePan / oldScale)
    zoomPan.zoom.floatValue = newScale
    if (offset.x < 0) {
        offset = Offset(0f, offset.y)
    } else if (maxWidth - offset.x < maxWidth / newScale) {
        offset = offset.copy(x = maxWidth - maxWidth / newScale)
    }
    if (offset.y < 0) {
        offset = Offset(offset.x, 0f)
    } else if (maxWidth - offset.y < maxWidth / newScale) {
        offset = offset.copy(y = maxWidth - maxWidth / newScale)
    }
    zoomPan.offset.value = offset
}
