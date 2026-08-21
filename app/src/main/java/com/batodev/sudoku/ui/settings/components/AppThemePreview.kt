package com.batodev.sudoku.ui.settings.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.batodev.sudoku.ui.theme.SudokuTheme
import com.batodev.sudoku.ui.util.PreviewLightDark

private const val UNSELECTED_BORDER_ALPHA = 0.75f
private const val TERTIARY_BAR_WIDTH_FRACTION = 0.6f
private const val SECONDARY_BAR_WIDTH_FRACTION = 0.4f
private const val ACCENT_BAR_WIDTH_FRACTION = 0.5f
private const val BOTTOM_BAR_ITEM_ALPHA = 0.6f
private const val PREVIEW_ASPECT_RATIO = 1f / 1.7f

/** Info needed to render an [AppThemePreviewItem]: the theme it represents and whether it's picked. */
data class AppThemePreviewInfo(
    val selected: Boolean,
    val colorScheme: ColorScheme,
    val shapes: Shapes,
)

@Composable
private fun RowScope.AppThemePreviewSelectionIndicator(info: AppThemePreviewInfo) {
    AnimatedVisibility(
        visible = info.selected,
        enter = fadeIn() + scaleIn(),
        exit = fadeOut() + scaleOut(),
    ) {
        Icon(
            imageVector = Icons.Filled.CheckCircle,
            contentDescription = null,
            tint = info.colorScheme.primary,
        )
    }
}

@Composable
private fun AppThemePreviewTextBlock(info: AppThemePreviewInfo) {
    Box(
        modifier =
            Modifier
                .padding(start = 8.dp, end = 8.dp)
                .background(
                    color = info.colorScheme.surfaceVariant,
                    shape = info.shapes.small,
                ).fillMaxWidth(1f),
    ) {
        Column(
            modifier =
                Modifier
                    .padding(4.dp)
                    .height(32.dp)
                    .fillMaxWidth(),
        ) {
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth(TERTIARY_BAR_WIDTH_FRACTION)
                        .weight(1f)
                        .background(
                            color = info.colorScheme.tertiary,
                            shape = RoundedCornerShape(5.dp),
                        ),
            )
            Spacer(modifier = Modifier.height(6.dp))
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth(SECONDARY_BAR_WIDTH_FRACTION)
                        .weight(1f)
                        .background(
                            color = info.colorScheme.secondary,
                            shape = RoundedCornerShape(5.dp),
                        ),
            )
        }
    }
}

@Composable
private fun AppThemePreviewAccentBar(info: AppThemePreviewInfo) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
                .height(16.dp),
        horizontalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(ACCENT_BAR_WIDTH_FRACTION)
                    .background(
                        color = info.colorScheme.primary,
                        shape = info.shapes.small,
                    ),
        )
    }
}

@Composable
private fun AppThemePreviewBottomBar(info: AppThemePreviewInfo) {
    Box(
        modifier =
            Modifier
                .fillMaxWidth(),
        contentAlignment = Alignment.BottomCenter,
    ) {
        Surface(
            tonalElevation = 3.dp,
        ) {
            Row(
                modifier =
                    Modifier
                        .height(32.dp)
                        .fillMaxWidth()
                        .background(info.colorScheme.surface)
                        .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier =
                        Modifier
                            .alpha(BOTTOM_BAR_ITEM_ALPHA)
                            .height(17.dp)
                            .weight(1f)
                            .background(
                                color = info.colorScheme.surfaceTint,
                                shape = info.shapes.small,
                            ),
                )
                Box(
                    modifier =
                        Modifier
                            .padding(start = 8.dp)
                            .size(17.dp)
                            .background(
                                color = info.colorScheme.primaryContainer,
                                shape = CircleShape,
                            ),
                )
            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun AppThemePreviewItem(
    info: AppThemePreviewInfo,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .aspectRatio(PREVIEW_ASPECT_RATIO)
                .border(
                    width = 4.dp,
                    color =
                        if (info.selected) {
                            info.colorScheme.primary
                        } else {
                            info.colorScheme.onSurface.copy(alpha = UNSELECTED_BORDER_ALPHA)
                        },
                    shape = RoundedCornerShape(15.dp),
                ).padding(4.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(info.colorScheme.background)
                .clickable(onClick = onClick),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(32.dp)
                    .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AppThemePreviewSelectionIndicator(info)
        }

        AppThemePreviewTextBlock(info)
        AppThemePreviewAccentBar(info)
        AppThemePreviewBottomBar(info)
    }
}

@PreviewLightDark
@Composable
private fun AppThemePreviewItem_Preview() {
    SudokuTheme {
        Surface {
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Column(
                    modifier = Modifier.width(100.dp),
                ) {
                    AppThemePreviewItem(
                        info =
                            AppThemePreviewInfo(
                                selected = true,
                                colorScheme = MaterialTheme.colorScheme,
                                shapes = MaterialTheme.shapes,
                            ),
                        onClick = { },
                    )
                }
                Column(
                    modifier = Modifier.width(100.dp),
                ) {
                    AppThemePreviewItem(
                        info =
                            AppThemePreviewInfo(
                                selected = false,
                                colorScheme = MaterialTheme.colorScheme,
                                shapes = MaterialTheme.shapes,
                            ),
                        onClick = { },
                    )
                }
            }
        }
    }
}
