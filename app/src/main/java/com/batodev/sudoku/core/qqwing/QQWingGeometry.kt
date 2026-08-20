package com.batodev.sudoku.core.qqwing

/**
 * Given the index of a cell (0-80) calculate the column (0-8) in which that
 * cell resides.
 */
internal fun cellToColumnInternal(cell: Int): Int {
    return cell % QQWing.ROW_COL_SEC_SIZE
}

/**
 * Given the index of a cell (0-80) calculate the row (0-8) in which it
 * resides.
 */
internal fun cellToRowInternal(cell: Int): Int {
    return cell / QQWing.ROW_COL_SEC_SIZE
}

/**
 * Given the index of a cell (0-80) calculate the section (0-8) in which it
 * resides.
 */
internal fun cellToSectionInternal(cell: Int): Int {
    return cell / QQWing.SEC_GROUP_SIZE * QQWing.GRID_SIZE_ROW + cellToColumnInternal(cell) / QQWing.GRID_SIZE_COL
}

/**
 * Given the index of a cell (0-80) calculate the cell (0-80) that is the
 * upper left start cell of that section.
 */
internal fun cellToSectionStartCellInternal(cell: Int): Int {
    return cell / QQWing.SEC_GROUP_SIZE * QQWing.SEC_GROUP_SIZE +
        cellToColumnInternal(cell) / QQWing.GRID_SIZE_COL * QQWing.GRID_SIZE_COL
}

/**
 * Given a row (0-8) calculate the first cell (0-80) of that row.
 */
internal fun rowToFirstCellInternal(row: Int): Int {
    return QQWing.ROW_COL_SEC_SIZE * row
}

/**
 * Given a column (0-8) calculate the first cell (0-80) of that column.
 */
internal fun columnToFirstCellInternal(column: Int): Int {
    return column
}

/**
 * Given a section (0-8) calculate the first cell (0-80) of that section.
 */
internal fun sectionToFirstCellInternal(section: Int): Int {
    return section % QQWing.GRID_SIZE_ROW * QQWing.GRID_SIZE_COL +
        section / QQWing.GRID_SIZE_ROW * QQWing.SEC_GROUP_SIZE
}

/**
 * Given a value for a cell (0-8) and a cell number (0-80) calculate the
 * offset into the possibility array (0-728).
 */
internal fun getPossibilityIndexInternal(valueIndex: Int, cell: Int): Int {
    return valueIndex + QQWing.ROW_COL_SEC_SIZE * cell
}

/**
 * Given a row (0-8) and a column (0-8) calculate the cell (0-80).
 */
internal fun rowColumnToCellInternal(row: Int, column: Int): Int {
    return row * QQWing.ROW_COL_SEC_SIZE + column
}

/**
 * Given a section (0-8) and an offset into that section (0-8) calculate the
 * cell (0-80)
 */
internal fun sectionToCellInternal(section: Int, offset: Int): Int {
    return sectionToFirstCellInternal(section) +
        offset / QQWing.GRID_SIZE_COL * QQWing.ROW_COL_SEC_SIZE + offset % QQWing.GRID_SIZE_COL
}
