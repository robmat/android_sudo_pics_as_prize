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

    private fun extractBoardData(parser: XmlPullParser): String? {
        for (i in 0 until parser.attributeCount) {
            if (parser.getAttributeName(i) == "data") {
                return parser.getAttributeValue(i)
            }
        }
        return null
    }

    private fun isValidBoard(boardString: String): Boolean = boardString.length == STANDARD_BOARD_LENGTH && boardString.all { it.isDigit() }

    private fun processSudokuTag(
        parser: XmlPullParser,
        parsedBoards: MutableList<String>,
    ): Boolean {
        val boardString = extractBoardData(parser)
        val success = boardString == null || isValidBoard(boardString)
        if (success && boardString != null) {
            parsedBoards.add(boardString)
        } else if (!success) {
            Log.i(tag, "Unexpected line: $boardString")
        }
        return success
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
        var success = true
        try {
            while (success && eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG && parser.name == "sudoku") {
                    success = processSudokuTag(parser, parsedBoards)
                }
                if (success) {
                    eventType = parser.next()
                }
            }
        } catch (e: XmlPullParserException) {
            Log.e(tag, "Exception while parsing!", e)
            success = false
        } catch (e: IOException) {
            Log.e(tag, "Exception while parsing!", e)
            success = false
        }

        input.close()

        return Pair(success, parsedBoards)
    }
}
