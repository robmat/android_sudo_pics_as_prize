package com.batodev.sudoku.ui.components.collapsingtopappbar

internal fun lerp(a: Float, b: Float, fraction: Float): Float {
    return a + fraction * (b - a)
}
