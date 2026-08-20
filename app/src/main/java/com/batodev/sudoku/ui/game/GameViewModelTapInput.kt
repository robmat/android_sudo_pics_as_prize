package com.batodev.sudoku.ui.game

import com.batodev.sudoku.core.Cell
import com.batodev.sudoku.core.utils.DigitFirstCallbacks
import com.batodev.sudoku.core.utils.GameState
import com.batodev.sudoku.core.utils.handleDigitFirstBranches

fun GameViewModel.processInput(
    cell: Cell,
    remainingUse: Boolean,
    longTap: Boolean = false,
): Boolean {
    if (!gamePlaying) return false
    currCell = nextSelectedCell(cell)
    val canInput = currCell.row >= 0 && currCell.col >= 0 && !gameBoard[currCell.row][currCell.col].locked
    if (canInput) {
        applyCellInput(remainingUse, longTap)
        remainingUsesList = countRemainingUses(gameBoard)
    }
    return canInput
}

private fun GameViewModel.nextSelectedCell(cell: Cell): Cell =
    if (currCell.row == cell.row && currCell.col == cell.col && digitFirstNumber == 0) {
        Cell(-1, -1)
    } else {
        cell
    }

private fun GameViewModel.applyCellInput(
    remainingUse: Boolean,
    longTap: Boolean,
) {
    if ((inputMethod.value == 1 || overrideInputMethodDF) && digitFirstNumber > 0) {
        applyDigitFirstInput(remainingUse, longTap)
    } else if (eraseButtonToggled) {
        applyEraseInput()
    }
}

private fun GameViewModel.applyDigitFirstInput(
    remainingUse: Boolean,
    longTap: Boolean,
) {
    if (!longTap) {
        val hasRemainingUses =
            remainingUsesList.size >= digitFirstNumber &&
                remainingUsesList[digitFirstNumber - 1] > 0
        if (hasRemainingUses || !remainingUse) {
            processNumberInput(digitFirstNumber)
            undoRedoManager.addState(GameState(gameBoard, notes))
            if (notesToggled) {
                currCell = Cell(currCell.row, currCell.col, digitFirstNumber)
            }
        }
    } else if (!currCell.locked) {
        gameBoard = setValueCell(0)
        setNote(digitFirstNumber)
        undoRedoManager.addState(GameState(gameBoard, notes))
    }
}

private fun GameViewModel.applyEraseInput() {
    val oldCell = currCell
    processNumberInput(0)
    if (oldCell.value != 0 && !oldCell.locked) {
        undoRedoManager.addState(GameState(gameBoard, notes))
    }
}

fun GameViewModel.processInputKeyboard(
    number: Int,
    longTap: Boolean = false,
) {
    if (!gamePlaying) return
    val canDirectInput =
        !longTap && inputMethod.value == 0 && !currCell.locked &&
            currCell.col >= 0 && currCell.row >= 0
    if (canDirectInput) {
        overrideInputMethodDF = false
        digitFirstNumber = 0
        processNumberInput(number)
        undoRedoManager.addState(GameState(gameBoard, notes))
    } else {
        handleDigitFirstBranches(
            longTap,
            inputMethod.value,
            digitFirstNumber,
            number,
            DigitFirstCallbacks(
                setOverrideInputMethodDF = { overrideInputMethodDF = true },
                setDigitFirstNumber = { digitFirstNumber = it },
                setCurrCell = { currCell = it },
            ),
        )
    }
    eraseButtonToggled = false
}
