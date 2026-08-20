package com.batodev.sudoku.core.utils

import com.batodev.sudoku.core.Cell

/**
 * Toggles the "digit-first" input selection (used by both the create/edit-sudoku and the play
 * screens): if [number] is already the selected digit, the selection is cleared; otherwise
 * [number] becomes selected. Returns the new `digitFirstNumber` together with the placeholder
 * [Cell] used to render it as the "current cell" while no board cell is actually selected.
 */
fun toggleDigitFirstNumber(currentDigitFirstNumber: Int, number: Int): Pair<Int, Cell> {
    val next = if (currentDigitFirstNumber == number) 0 else number
    return next to Cell(-1, -1, next)
}

/**
 * The three callbacks [handleDigitFirstBranches] uses to push its result back into the caller's
 * state: overriding the input method, updating the selected digit-first number, and updating the
 * placeholder "current cell".
 */
data class DigitFirstCallbacks(
    val setOverrideInputMethodDF: () -> Unit,
    val setDigitFirstNumber: (Int) -> Unit,
    val setCurrCell: (Cell) -> Unit
)

/**
 * Handles the "digit-first" input branches shared, statement-for-statement, by
 * `CreateSudokuViewModel` and `GameViewModel`'s `processInputKeyboard`: the paths taken when the
 * tap does *not* directly write a value into the board. On a short tap while already in
 * digit-first mode (`inputMethod == 1`), or a long tap while in normal mode (`inputMethod == 0`,
 * which also calls `callbacks.setOverrideInputMethodDF`), toggles the selected digit via
 * [toggleDigitFirstNumber] and pushes the result out via `callbacks`.
 *
 * Callers should invoke this from the `else` of whatever branch handles directly writing a value
 * (which differs slightly between the two view-models), passing through the same [longTap] and
 * `inputMethod` they already checked there.
 */
fun handleDigitFirstBranches(
    longTap: Boolean,
    inputMethod: Int,
    digitFirstNumber: Int,
    number: Int,
    callbacks: DigitFirstCallbacks
) {
    if (!longTap) {
        if (inputMethod == 1) {
            val (newDigitFirstNumber, newCurrCell) = toggleDigitFirstNumber(digitFirstNumber, number)
            callbacks.setDigitFirstNumber(newDigitFirstNumber)
            callbacks.setCurrCell(newCurrCell)
        }
    } else {
        if (inputMethod == 0) {
            callbacks.setOverrideInputMethodDF()
            val (newDigitFirstNumber, newCurrCell) = toggleDigitFirstNumber(digitFirstNumber, number)
            callbacks.setDigitFirstNumber(newDigitFirstNumber)
            callbacks.setCurrCell(newCurrCell)
        }
    }
}

/**
 * The bookkeeping shared by both view-models' `setValueCell` right after a cell's value is
 * written into the board copy: keeps [currCell] in sync when it refers to the just-changed
 * [changedCell], and (when [value] is 0, i.e. the cell was cleared) clears the error flag on both.
 *
 * Returns the possibly-updated `currCell` together with a flag telling the caller whether it
 * should return the board as-is immediately (true for a clear, since there's nothing left to
 * validate).
 */
fun applyClearedCellBookkeeping(currCell: Cell, changedCell: Cell, value: Int): Pair<Cell, Boolean> {
    val updatedCurrCell = if (currCell.row == changedCell.row && currCell.col == changedCell.col) {
        currCell.copy(value = changedCell.value)
    } else {
        currCell
    }
    if (value == 0) {
        changedCell.error = false
        updatedCurrCell.error = false
        return updatedCurrCell to true
    }
    return updatedCurrCell to false
}
