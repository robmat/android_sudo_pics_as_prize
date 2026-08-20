package com.batodev.sudoku.ui.components.collapsingtopappbar

internal fun lerp(
    a: Float,
    b: Float,
    fraction: Float,
): Float = a + fraction * (b - a)
