package com.batodev.sudoku.core.utils

import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.batodev.sudoku.core.qqwing.GameType

private fun fontSizeUnspecified(factor: Int): TextUnit = when (factor) {
    1 -> 26.sp
    2 -> 34.sp
    else -> 22.sp
}

private fun fontSize9x9(factor: Int): TextUnit = when (factor) {
    1 -> 28.sp
    2 -> 36.sp
    else -> 22.sp
}

private fun fontSize12x12(factor: Int): TextUnit = when (factor) {
    1 -> 24.sp
    2 -> 32.sp
    else -> 18.sp
}

private fun fontSize6x6(factor: Int): TextUnit = when (factor) {
    1 -> 34.sp
    2 -> 40.sp
    else -> 24.sp
}

// factor: 0 - small, 1 medium (default), 2 - big
fun SudokuUtils.getFontSize(type: GameType, factor: Int): TextUnit {
    return when (type) {
        GameType.Unspecified -> fontSizeUnspecified(factor)
        GameType.Default9x9 -> fontSize9x9(factor)
        GameType.Default12x12 -> fontSize12x12(factor)
        GameType.Default6x6 -> fontSize6x6(factor)
    }
}
