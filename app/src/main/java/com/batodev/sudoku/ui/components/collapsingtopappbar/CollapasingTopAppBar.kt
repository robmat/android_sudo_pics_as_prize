package com.batodev.sudoku.ui.components.collapsingtopappbar

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.MeasureResult
import androidx.compose.ui.layout.MeasureScope
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.lang.Float.max
import kotlin.math.roundToInt

/** The slot content shown inside a [CollapsingTopAppBar]. */
data class CollapsingTopAppBarContent(
    val navigationIcon: (@Composable () -> Unit)? = null,
    val actions: (@Composable RowScope.() -> Unit)? = null,
    val centralContent: (@Composable () -> Unit)? = null,
    val additionalContent: (@Composable () -> Unit)? = null,
    val collapsingTitle: CollapsingTitle? = null,
)

/** The scroll-driven behavior/appearance options for a [CollapsingTopAppBar]. [windowInsets]
 * defaults to [TopAppBarDefaults.windowInsets] when left `null` (resolved inside the composable,
 * since that default itself reads composition-local state). */
data class CollapsingTopAppBarConfig(
    val scrollBehavior: CollapsingTopAppBarScrollBehavior? = null,
    val collapsedElevation: Dp = DefaultCollapsedElevation,
    val windowInsets: WindowInsets? = null,
)

private fun computeCollapsedFraction(
    scrollBehavior: CollapsingTopAppBarScrollBehavior?,
    hasCentralContent: Boolean,
): Float =
    when {
        scrollBehavior != null && !hasCentralContent -> scrollBehavior.state.collapsedFraction
        scrollBehavior != null && hasCentralContent -> 0f
        else -> 1f
    }

private fun computeShowElevation(
    scrollBehavior: CollapsingTopAppBarScrollBehavior?,
    collapsedFraction: Float,
    hasCentralContent: Boolean,
): Boolean =
    when {
        scrollBehavior == null -> false
        scrollBehavior.state.contentOffset <= 0 && collapsedFraction == 1f -> true
        scrollBehavior.state.contentOffset < -1f && hasCentralContent -> true
        else -> false
    }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun rememberContainerColor(
    scrollBehavior: CollapsingTopAppBarScrollBehavior?,
    collapsedFraction: Float,
    hasCentralContent: Boolean,
    collapsedElevation: Dp,
): State<Color> {
    val showElevation = computeShowElevation(scrollBehavior, collapsedFraction, hasCentralContent)
    return animateColorAsState(
        if (showElevation) {
            MaterialTheme.colorScheme.surfaceColorAtElevation(collapsedElevation)
        } else {
            MaterialTheme.colorScheme.surface
        },
    )
}

// Both Text()s below are separate layoutId'd children measured independently by the enclosing
// custom Layout (expanded vs. collapsed title, crossfaded by alpha) - they cannot be merged into
// a single emitter without breaking that measurement. compose-rules' multiple-emitters-check
// flags this; it does not apply cleanly to raw Layout-based custom layouts like this one.
@Composable
private fun CollapsingTitleTexts(
    collapsingTitle: CollapsingTitle,
    collapsingTitleScale: Float,
) {
    Text(
        modifier =
            Modifier
                .layoutId(EXPANDED_TITLE_ID)
                .wrapContentHeight(align = Alignment.Top)
                .graphicsLayer(
                    scaleX = collapsingTitleScale,
                    scaleY = collapsingTitleScale,
                    transformOrigin = TransformOrigin(0f, 0f),
                ),
        text = collapsingTitle.titleText,
        style = collapsingTitle.expandedTextStyle,
        color = collapsingTitle.color,
    )
    Text(
        modifier =
            Modifier
                .layoutId(COLLAPSED_TITLE_ID)
                .wrapContentHeight(align = Alignment.Top)
                .graphicsLayer(
                    scaleX = collapsingTitleScale,
                    scaleY = collapsingTitleScale,
                    transformOrigin = TransformOrigin(0f, 0f),
                ),
        text = collapsingTitle.titleText,
        style = collapsingTitle.expandedTextStyle,
        color = collapsingTitle.color,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
}

// Each child below carries its own layoutId and is measured/placed independently by the
// enclosing custom Layout - they are the slots of a multi-slot app bar, not incidental sibling
// emissions, so they cannot be wrapped into a single emitter. Same structural exception as
// CollapsingTitleTexts above.
@Composable
private fun TopAppBarChildren(
    content: CollapsingTopAppBarContent,
    collapsingTitleScale: Float,
) {
    if (content.collapsingTitle != null) {
        CollapsingTitleTexts(content.collapsingTitle, collapsingTitleScale)
    }

    if (content.navigationIcon != null) {
        Box(
            modifier =
                Modifier
                    .wrapContentSize()
                    .layoutId(NAVIGATION_ICON_ID),
        ) {
            content.navigationIcon.invoke()
        }
    }

    if (content.actions != null) {
        Row(
            modifier =
                Modifier
                    .wrapContentSize()
                    .layoutId(ACTIONS_ID),
        ) {
            content.actions.invoke(this)
        }
    }

    if (content.centralContent != null) {
        Box(
            modifier =
                Modifier
                    .wrapContentSize()
                    .layoutId(CENTRAL_CONTENT_ID),
        ) {
            content.centralContent.invoke()
        }
    }

    if (content.additionalContent != null) {
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .layoutId(ADDITIONAL_CONTENT_ID),
        ) {
            content.additionalContent.invoke()
        }
    }
}

/** The child [Placeable]s measured out of a [CollapsingTopAppBar]'s [Layout] content. */
private class TopAppBarPlaceables(
    val navigationIcon: Placeable?,
    val actions: Placeable?,
    val expandedTitle: Placeable?,
    val collapsedTitle: Placeable?,
    val centralContent: Placeable?,
    val additionalContent: Placeable?,
)

/** The horizontal space reserved on each side of the app bar for the navigation icon/actions. */
private class TopAppBarOffsets(
    val navigationIconOffset: Float,
    val actionsOffset: Float,
)

/** The non-geometric state threaded through the whole measure/place pipeline of a
 * [CollapsingTopAppBar]: its [config], whether it [hasCentralContent], the scale a fully
 * collapsed title is shown at, and how collapsed it currently is. */
private class TopAppBarMeasureContext(
    val config: CollapsingTopAppBarConfig,
    val hasCentralContent: Boolean,
    val fullyCollapsedTitleScale: Float,
    val collapsedFraction: Float,
)

private fun MeasureScope.measureTopAppBarPlaceables(
    measurables: List<Measurable>,
    constraints: Constraints,
    horizontalPaddingPx: Float,
    fullyCollapsedTitleScale: Float,
): TopAppBarPlaceables {
    val navigationIconPlaceable =
        measurables
            .firstOrNull { it.layoutId == NAVIGATION_ICON_ID }
            ?.measure(constraints.copy(minWidth = 0))

    val actionsPlaceable =
        measurables
            .firstOrNull { it.layoutId == ACTIONS_ID }
            ?.measure(constraints.copy(minWidth = 0))

    val expandedTitlePlaceable =
        measurables
            .firstOrNull { it.layoutId == EXPANDED_TITLE_ID }
            ?.measure(
                constraints.copy(
                    maxWidth = (constraints.maxWidth - 2 * horizontalPaddingPx).roundToInt(),
                    minWidth = 0,
                    minHeight = 0,
                ),
            )

    val additionalContentPlaceable =
        measurables
            .firstOrNull { it.layoutId == ADDITIONAL_CONTENT_ID }
            ?.measure(constraints)

    val navigationIconOffset =
        when (navigationIconPlaceable) {
            null -> horizontalPaddingPx
            else -> navigationIconPlaceable.width + horizontalPaddingPx * 2
        }

    val actionsOffset =
        when (actionsPlaceable) {
            null -> horizontalPaddingPx
            else -> actionsPlaceable.width + horizontalPaddingPx * 2
        }

    val collapsedTitleMaxWidthPx =
        (constraints.maxWidth - navigationIconOffset - actionsOffset) / fullyCollapsedTitleScale

    val collapsedTitlePlaceable =
        measurables
            .firstOrNull { it.layoutId == COLLAPSED_TITLE_ID }
            ?.measure(
                constraints.copy(
                    maxWidth = collapsedTitleMaxWidthPx.roundToInt(),
                    minWidth = 0,
                    minHeight = 0,
                ),
            )

    val centralContentPlaceable =
        measurables
            .firstOrNull { it.layoutId == CENTRAL_CONTENT_ID }
            ?.measure(
                constraints.copy(
                    minWidth = 0,
                    maxWidth = (constraints.maxWidth - navigationIconOffset - actionsOffset).roundToInt(),
                ),
            )

    return TopAppBarPlaceables(
        navigationIcon = navigationIconPlaceable,
        actions = actionsPlaceable,
        expandedTitle = expandedTitlePlaceable,
        collapsedTitle = collapsedTitlePlaceable,
        centralContent = centralContentPlaceable,
        additionalContent = additionalContentPlaceable,
    )
}

/** Pixel geometry of a [CollapsingTopAppBar]'s title, and the app bar's current height. */
private class TopAppBarTitleGeometry(
    val collapsingTitleX: Int,
    val collapsingTitleY: Int,
    val layoutHeightPx: Float,
)

private fun Density.computeTitleGeometry(
    placeables: TopAppBarPlaceables,
    context: TopAppBarMeasureContext,
    navigationIconOffset: Float,
    collapsedHeightPx: Float,
): TopAppBarTitleGeometry {
    val expandedTitlePlaceable = placeables.expandedTitle
    val collapsedTitlePlaceable = placeables.collapsedTitle
    if (expandedTitlePlaceable == null || collapsedTitlePlaceable == null) {
        context.config.scrollBehavior
            ?.state
            ?.heightOffsetLimit = -1f
        return TopAppBarTitleGeometry(0, 0, collapsedHeightPx)
    }

    val expandedTitleTopPadding = ExpandedTitleTopPadding.toPx()
    val expandedTitleBottomPaddingPx = ExpandedTitleBottomPadding.toPx()
    val expandedTitleStartPaddingPx = ExpandedTitleStartPadding.toPx()

    // Measuring TopAppBar collapsing distance
    val heightOffsetLimitPx =
        expandedTitleTopPadding + expandedTitlePlaceable.height + expandedTitleBottomPaddingPx
    context.config.scrollBehavior
        ?.state
        ?.heightOffsetLimit =
        when {
            context.hasCentralContent -> -1f
            else -> -heightOffsetLimitPx
        }

    // TopAppBar height at fully expanded state
    val fullyExpandedHeightPx = MinCollapsedHeight.toPx() + heightOffsetLimitPx

    // Coordinates of fully expanded title
    val fullyExpandedTitleX = expandedTitleStartPaddingPx
    val fullyExpandedTitleY =
        fullyExpandedHeightPx - expandedTitlePlaceable.height - expandedTitleBottomPaddingPx

    // Coordinates of fully collapsed title
    val fullyCollapsedTitleX = navigationIconOffset
    val fullyCollapsedTitleY =
        collapsedHeightPx / 2 - CollapsedTitleLineHeight.toPx().roundToInt() / 2

    val collapsedFraction = context.collapsedFraction

    // Current height of TopAppBar
    val layoutHeightPx = lerp(fullyExpandedHeightPx, collapsedHeightPx, collapsedFraction)

    // Current coordinates of collapsing title
    val collapsingTitleX =
        lerp(fullyExpandedTitleX, fullyCollapsedTitleX, collapsedFraction).roundToInt()
    val collapsingTitleY =
        lerp(fullyExpandedTitleY, fullyCollapsedTitleY, collapsedFraction).roundToInt()

    return TopAppBarTitleGeometry(collapsingTitleX, collapsingTitleY, layoutHeightPx)
}

/** The pixel measurements [placeTopAppBarChildren] needs beyond the placeables/offsets/title
 * geometry it's already given. */
private class TopAppBarPlacementMetrics(
    val constraintsMaxWidth: Int,
    val collapsedHeightPx: Float,
    val horizontalPaddingPx: Float,
    val collapsedFraction: Float,
)

private fun Placeable.PlacementScope.placeTopAppBarChildren(
    placeables: TopAppBarPlaceables,
    offsets: TopAppBarOffsets,
    titleGeometry: TopAppBarTitleGeometry,
    metrics: TopAppBarPlacementMetrics,
) {
    val collapsedHeightPx = metrics.collapsedHeightPx
    val horizontalPaddingPx = metrics.horizontalPaddingPx

    val navigationIconX = horizontalPaddingPx.roundToInt()
    val navigationIconY =
        ((collapsedHeightPx - (placeables.navigationIcon?.height ?: 0)) / 2).roundToInt()
    placeables.navigationIcon?.placeRelative(x = navigationIconX, y = navigationIconY)

    val actionsX =
        (
            metrics.constraintsMaxWidth - (placeables.actions?.width ?: 0) - horizontalPaddingPx
        ).roundToInt()
    val actionsY = ((collapsedHeightPx - (placeables.actions?.height ?: 0)) / 2).roundToInt()
    placeables.actions?.placeRelative(x = actionsX, y = actionsY)

    placeables.centralContent?.placeRelative(
        x = offsets.navigationIconOffset.roundToInt(),
        y = ((collapsedHeightPx - placeables.centralContent.height) / 2).roundToInt(),
    )

    if (placeables.expandedTitle?.width == placeables.collapsedTitle?.width) {
        placeables.expandedTitle?.placeRelative(
            x = titleGeometry.collapsingTitleX,
            y = titleGeometry.collapsingTitleY,
        )
    } else {
        placeables.expandedTitle?.placeRelativeWithLayer(
            x = titleGeometry.collapsingTitleX,
            y = titleGeometry.collapsingTitleY,
            layerBlock = { alpha = 1 - metrics.collapsedFraction },
        )
        placeables.collapsedTitle?.placeRelativeWithLayer(
            x = titleGeometry.collapsingTitleX,
            y = titleGeometry.collapsingTitleY,
            layerBlock = { alpha = metrics.collapsedFraction },
        )
    }
    placeables.additionalContent?.placeRelative(
        x = 0,
        y = titleGeometry.layoutHeightPx.roundToInt(),
    )
}

private fun MeasureScope.measureTopAppBar(
    measurables: List<Measurable>,
    constraints: Constraints,
    context: TopAppBarMeasureContext,
): MeasureResult {
    val horizontalPaddingPx = HorizontalPadding.toPx()

    val placeables =
        measureTopAppBarPlaceables(
            measurables,
            constraints,
            horizontalPaddingPx,
            context.fullyCollapsedTitleScale,
        )

    val offsets =
        TopAppBarOffsets(
            navigationIconOffset =
                when (placeables.navigationIcon) {
                    null -> horizontalPaddingPx
                    else -> placeables.navigationIcon.width + horizontalPaddingPx * 2
                },
            actionsOffset =
                when (placeables.actions) {
                    null -> horizontalPaddingPx
                    else -> placeables.actions.width + horizontalPaddingPx * 2
                },
        )

    val collapsedHeightPx =
        when {
            placeables.centralContent != null -> {
                max(MinCollapsedHeight.toPx(), placeables.centralContent.height.toFloat())
            }

            else -> {
                MinCollapsedHeight.toPx()
            }
        }

    val titleGeometry =
        computeTitleGeometry(
            placeables,
            context,
            offsets.navigationIconOffset,
            collapsedHeightPx,
        )

    val topAppBarHeightPx =
        titleGeometry.layoutHeightPx.roundToInt() + (placeables.additionalContent?.height ?: 0)

    return layout(constraints.maxWidth, topAppBarHeightPx) {
        placeTopAppBarChildren(
            placeables,
            offsets,
            titleGeometry,
            TopAppBarPlacementMetrics(
                constraintsMaxWidth = constraints.maxWidth,
                collapsedHeightPx = collapsedHeightPx,
                horizontalPaddingPx = horizontalPaddingPx,
                collapsedFraction = context.collapsedFraction,
            ),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollapsingTopAppBar(
    modifier: Modifier = Modifier,
    content: CollapsingTopAppBarContent = CollapsingTopAppBarContent(),
    config: CollapsingTopAppBarConfig = CollapsingTopAppBarConfig(),
) {
    val hasCentralContent = content.centralContent != null
    val collapsedFraction = computeCollapsedFraction(config.scrollBehavior, hasCentralContent)

    val fullyCollapsedTitleScale =
        when {
            content.collapsingTitle != null -> {
                CollapsedTitleLineHeight.value / content.collapsingTitle.expandedTextStyle.lineHeight.value
            }

            else -> {
                1f
            }
        }

    val collapsingTitleScale = lerp(1f, fullyCollapsedTitleScale, collapsedFraction)

    val containerColor =
        rememberContainerColor(
            config.scrollBehavior,
            collapsedFraction,
            hasCentralContent,
            config.collapsedElevation,
        )

    val measureContext =
        TopAppBarMeasureContext(
            config = config,
            hasCentralContent = hasCentralContent,
            fullyCollapsedTitleScale = fullyCollapsedTitleScale,
            collapsedFraction = collapsedFraction,
        )

    Surface(
        modifier = modifier,
        color = containerColor.value,
    ) {
        Layout(
            content = { TopAppBarChildren(content, collapsingTitleScale) },
            modifier =
                Modifier
                    .windowInsetsPadding(config.windowInsets ?: TopAppBarDefaults.windowInsets)
                    .then(Modifier.heightIn(min = MinCollapsedHeight)),
        ) { measurables, constraints ->
            measureTopAppBar(measurables, constraints, measureContext)
        }
    }
}

data class CollapsingTitle(
    val titleText: String,
    val expandedTextStyle: TextStyle,
    val color: Color,
) {
    companion object {
        @Composable
        fun large(titleText: String) =
            CollapsingTitle(
                titleText,
                MaterialTheme.typography.headlineLarge,
                MaterialTheme.colorScheme.onSurface,
            )

        @Composable
        fun medium(titleText: String) =
            CollapsingTitle(
                titleText,
                MaterialTheme.typography.headlineMedium,
                MaterialTheme.colorScheme.onSurface,
            )
    }
}

private val MinCollapsedHeight = 64.dp
private val HorizontalPadding = 4.dp
private val ExpandedTitleBottomPadding = 26.dp
private val ExpandedTitleTopPadding = 12.dp
private val ExpandedTitleStartPadding = 16.dp
private val CollapsedTitleLineHeight = 28.sp
private val DefaultCollapsedElevation = 3.0.dp

private const val EXPANDED_TITLE_ID = "expandedTitle"
private const val COLLAPSED_TITLE_ID = "collapsedTitle"
private const val NAVIGATION_ICON_ID = "navigationIcon"
private const val ACTIONS_ID = "actions"
private const val CENTRAL_CONTENT_ID = "centralContent"
private const val ADDITIONAL_CONTENT_ID = "additionalContent"
