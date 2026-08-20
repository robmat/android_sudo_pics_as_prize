package com.batodev.sudoku.core.qqwing

import java.util.Random

internal fun QQWing.setRandom(seed: Int) {
    QQWingRandom.random = Random(seed.toLong())
}

internal fun QQWing.setPrintStyle(ps: PrintStyle) {
    historyRecorder.printStyle = ps
}

internal fun QQWing.setRecordHistory(recHistory: Boolean) {
    historyRecorder.recordHistory = recHistory
}

internal fun QQWing.setLogHistory(logHist: Boolean) {
    historyRecorder.logHistory = logHist
}
