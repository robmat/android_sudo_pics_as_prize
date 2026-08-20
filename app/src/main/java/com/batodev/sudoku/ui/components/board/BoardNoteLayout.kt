package com.batodev.sudoku.ui.components.board

import com.batodev.sudoku.core.qqwing.GameType

private const val NOTES_PER_GROUP_9X9 = 3
private const val NOTES_PER_GROUP_12X12 = 4

private fun notesPerGroupForSize(size: Int): Int =
    if (size == BOARD_SIZE_12X12) NOTES_PER_GROUP_12X12 else NOTES_PER_GROUP_9X9

private fun isSupportedNoteLayoutSize(size: Int): Boolean =
    size == BOARD_SIZE_6X6 || size == BOARD_SIZE_9X9 || size == BOARD_SIZE_12X12

/**
 * Which "column" (0-indexed) within a cell's mini note grid the given note [number] should be
 * drawn in, for a board of the given [size]. Notes are laid out left-to-right, top-to-bottom in
 * groups of [notesPerGroupForSize].
 */
internal fun getNoteColumnNumber(number: Int, size: Int): Int {
    if (!isSupportedNoteLayoutSize(size)) return 0
    return (number - 1) / notesPerGroupForSize(size)
}

/**
 * Which "row" (0-indexed) within a cell's mini note grid the given note [number] should be drawn
 * in, for a board of the given [size]. See [getNoteColumnNumber].
 */
internal fun getNoteRowNumber(number: Int, size: Int): Int {
    if (!isSupportedNoteLayoutSize(size)) return 0
    return (number - 1) % notesPerGroupForSize(size)
}

internal fun getSectionHeightForSize(size: Int): Int {
    return when (size) {
        BOARD_SIZE_6X6 -> GameType.Default6x6.sectionHeight
        BOARD_SIZE_9X9 -> GameType.Default9x9.sectionHeight
        BOARD_SIZE_12X12 -> GameType.Default12x12.sectionHeight
        else -> GameType.Default9x9.sectionHeight
    }
}

internal fun getSectionWidthForSize(size: Int): Int {
    return when (size) {
        BOARD_SIZE_6X6 -> GameType.Default6x6.sectionWidth
        BOARD_SIZE_9X9 -> GameType.Default9x9.sectionWidth
        BOARD_SIZE_12X12 -> GameType.Default12x12.sectionWidth
        else -> GameType.Default9x9.sectionWidth
    }
}
