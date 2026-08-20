package com.batodev.sudoku.core.parser

import android.util.Log
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import org.xmlpull.v1.XmlPullParserFactory
import java.io.IOException

// File type from OpenSudoku app (https://gitlab.com/opensudoku/opensudoku)
// https://gitlab.com/opensudoku/opensudoku/-/blob/develop/app/src/main/java/org/moire/opensudoku/gui/importing/OpenSudokuImportTask.java
/**
 * .opensudoku - format from the OpenSudoku app. Uses XML schema
 */
class OpenSudokuParser : FileImportParser {
    private val tag = "OpenSudokuParser"

    companion object {
        private const val STANDARD_BOARD_LENGTH = 81
    }

    private fun parseVersionedImport(parser: XmlPullParser, rootTag: String): Pair<Boolean, List<String>>? {
        if (rootTag != "opensudoku") return null
        val version = parser.getAttributeValue(null, "version")
        return when (version) {
            // no version provided, assume that it's version 1
            null -> importV1(parser)
            "2" -> importV2(parser)
            else -> null
        }
    }

    private fun processTag(
        parser: XmlPullParser,
        eventType: Int,
        result: Pair<Boolean, List<String>>
    ): Pair<Boolean, List<String>> {
        if (eventType != XmlPullParser.START_TAG) return result
        return parseVersionedImport(parser, parser.name) ?: result
    }

    private fun readOpenSudokuXml(content: String): Pair<Boolean, List<String>> {
        var result: Pair<Boolean, List<String>> = Pair(false, emptyList())
        try {
            val factory = XmlPullParserFactory.newInstance()
            factory.isNamespaceAware = false
            val parser = factory.newPullParser()
            parser.setInput(content.reader())
            var eventType = parser.eventType
            while (eventType != XmlPullParser.END_DOCUMENT) {
                result = processTag(parser, eventType, result)
                eventType = parser.next()
            }
        } catch (e: XmlPullParserException) {
            Log.e(tag, "Exception while parsing!", e)
            result = result.copy(first = false)
        } catch (e: IOException) {
            Log.e(tag, "Exception while parsing!", e)
            result = result.copy(first = false)
        }
        return result
    }

    /**
     * @param content .opensudoku file content
     * @return Pair with: First - parsing success. Second - strings of parsed boards
     */
    override fun toBoards(content: String): Pair<Boolean, List<String>> = readOpenSudokuXml(content)

    private fun processGameTagV1(parser: XmlPullParser, boards: MutableList<String>) {
        val boardString = parser.getAttributeValue(null, "data")
        if (boardString.length == STANDARD_BOARD_LENGTH && boardString.all { char -> char.isDigit() }) {
            boards.add(boardString)
        } else {
            Log.i("$tag/ImportV1", "This line was skipped $boardString")
        }
    }

    private fun importV1(parser: XmlPullParser): Pair<Boolean, List<String>> {
        val boards = mutableListOf<String>()
        var eventType = parser.eventType
        while (eventType != XmlPullParser.END_DOCUMENT) {
            if (eventType == XmlPullParser.START_TAG && parser.name == "game") {
                processGameTagV1(parser, boards)
            }
            eventType = parser.next()
        }
        return Pair(true, boards)
    }

    private fun processGameTagV2(parser: XmlPullParser, boards: MutableList<String>) {
        // Not used now, but maybe will be used in future
        // val created = parseLong(parser.getAttributeValue(null, "created"), System.currentTimeMillis());
        // val lastPlayed = parseLong(parser.getAttributeValue(null, "last_played"), 0);
        // val note = parser.getAttributeValue(null, "note");
        // val state = parseLong(parser.getAttributeValue(null, "state"))
        // val timer = parseLong(parser.getAttributeValue(null, "time"), 0)
        val boardString = parser.getAttributeValue(null, "data")
        if (boardString.length == STANDARD_BOARD_LENGTH) {
            boards.add(boardString)
        } else {
            Log.i("$tag/ImportV2", "This line was skipped $boardString")
        }
    }

    private fun importV2(parser: XmlPullParser): Pair<Boolean, List<String>> {
        var eventType = parser.eventType
        // Folder tags are present in the v2 schema but not used by this parser.
        val boards = mutableListOf<String>()
        while (eventType != XmlPullParser.END_DOCUMENT) {
            if (eventType == XmlPullParser.START_TAG && parser.name == "game") {
                processGameTagV2(parser, boards)
            }
            eventType = parser.next()
        }
        return Pair(true, boards)
    }
}
