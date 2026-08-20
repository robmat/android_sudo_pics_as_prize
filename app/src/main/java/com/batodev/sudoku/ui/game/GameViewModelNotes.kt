package com.batodev.sudoku.ui.game

import com.batodev.sudoku.core.Cell
import com.batodev.sudoku.core.Note
import com.batodev.sudoku.core.utils.GameState

internal fun GameViewModel.clearNotesAtCell(
    notes: List<Note>,
    row: Int = currCell.row,
    col: Int = currCell.col,
): List<Note> =
    notes.minus(
        notes
            .filter { note ->
                note.row == row &&
                    note.col == col
            }.toSet(),
    )

internal fun GameViewModel.emptyNotes(): List<Note> = emptyList()

fun GameViewModel.clearNotes() {
    notes = emptyNotes()
    undoRedoManager.addState(
        GameState(gameBoard, notes),
    )
}

internal fun GameViewModel.addNote(
    note: Int,
    row: Int,
    col: Int,
): List<Note> = notes.plus(Note(row, col, note))

internal fun GameViewModel.removeNote(
    note: Int,
    row: Int,
    col: Int,
): List<Note> = notes.minus(Note(row, col, note))

internal fun GameViewModel.setNote(number: Int) {
    val note = Note(currCell.row, currCell.col, number)
    notes =
        if (notes.contains(note)) {
            removeNote(note.value, note.row, note.col)
        } else {
            notesTaken++
            addNote(note.value, note.row, note.col)
        }
}

fun GameViewModel.computeNotes() {
    notes = sudokuUtils.computeNotes(gameBoard, boardEntity.type)
    undoRedoManager.addState(GameState(gameBoard, notes))
}

internal fun GameViewModel.autoEraseNotes(
    board: List<List<Cell>> = getBoardNoRef(),
    cell: Cell,
): List<Note> {
    if (currCell.row < 0 || currCell.col < 0) {
        return notes
    }
    return sudokuUtils.autoEraseNotes(board, notes, cell, boardEntity.type)
}
