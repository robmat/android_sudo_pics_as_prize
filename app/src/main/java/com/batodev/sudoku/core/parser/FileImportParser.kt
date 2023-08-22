package com.batodev.sudoku.core.parser

interface FileImportParser {
    fun toBoards(content: String): Pair<Boolean, List<String>>
}