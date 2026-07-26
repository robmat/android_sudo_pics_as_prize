package com.batodev.sudoku.core.qqwing

import com.batodev.sudoku.R

enum class GameType(
    val size: Int,
    val sectionHeight: Int,
    val sectionWidth: Int,
    val resName: Int
) {
    Unspecified(size = 1, sectionHeight = 1, sectionWidth = 1, resName = R.string.type_unspecified),
    Default9x9(size = 9, sectionHeight = 3, sectionWidth = 3, resName = R.string.type_default_9x9),
    Default12x12(size = 12, sectionHeight = 3, sectionWidth = 4, resName = R.string.type_default_12x12),
    Default6x6(size = 6, sectionHeight = 2, sectionWidth = 3, resName = R.string.type_default_6x6),
}
