package com.batodev.sudoku.core.parser

import android.util.Log
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import org.xmlpull.v1.XmlPullParserFactory
import java.io.IOException

/**
 * .1gsudoku - file type from "Sudoku 10'000" app.
 */
class GsudokuParser : FileImportParser {
    private val tag = "GsudokuParser"

    companion object {
        private const val STANDARD_BOARD_LENGTH = 81
    }

    /**
     * @param content .1gsudoku file content
     * @return Pair with: First - parsing success. Second - strings of parsed boards
     */
    override fun toBoards(content: String): Pair<Boolean, List<String>> {
        val parsedBoards = mutableListOf<String>()

        val factory = XmlPullParserFactory.newInstance()
        val parser = factory.newPullParser()

        val input = content.reader()
        parser.setInput(input)

        var eventType = parser.eventType
        try {
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG && parser.name == "sudoku") {
                    for (i in 0 until parser.attributeCount) {
                        if (parser.getAttributeName(i) == "data") {
                            val boardString = parser.getAttributeValue(i)
                            val isValidBoard = boardString.length == STANDARD_BOARD_LENGTH &&
                                boardString.all { char -> char.isDigit() }
                            if (isValidBoard) {
                                parsedBoards.add(boardString)
                            } else {
                                Log.i(tag, "Unexpected line: $boardString")
                                return Pair(false, parsedBoards)
                            }
                        }
                    }
                }
                eventType = parser.next()
            }
        } catch (e: XmlPullParserException) {
            Log.e(tag, "Exception while parsing!", e)
            return Pair(false, parsedBoards)
        } catch (e: IOException) {
            Log.e(tag, "Exception while parsing!", e)
            return Pair(false, parsedBoards)
        }

        input.close()

        return Pair(true, parsedBoards)
    }
}
