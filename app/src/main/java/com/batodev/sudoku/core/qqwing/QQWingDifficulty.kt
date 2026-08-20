package com.batodev.sudoku.core.qqwing

private const val HIDDEN_SINGLE_THRESHOLD_6X6 = 0
private const val HIDDEN_SINGLE_THRESHOLD_9X9 = 10
private const val HIDDEN_SINGLE_THRESHOLD_12X12 = 20
private const val HIDDEN_SINGLE_THRESHOLD_DEFAULT = 10

private const val SINGLE_COUNT_THRESHOLD_6X6 = 10
private const val SINGLE_COUNT_THRESHOLD_9X9 = 35
private const val SINGLE_COUNT_THRESHOLD_12X12 = 50
private const val SINGLE_COUNT_THRESHOLD_DEFAULT = 20

/**
 * Get the gameDifficulty rating.
 */
internal fun QQWing.getDifficulty(): GameDifficulty =
    when {
        historyRecorder.getGuessCount() > 0 -> GameDifficulty.Challenge
        historyRecorder.getBoxLineReductionCount() > 0 -> GameDifficulty.Hard
        historyRecorder.getPointingPairTripleCount() > 0 -> GameDifficulty.Hard
        historyRecorder.getHiddenPairCount() > 0 -> GameDifficulty.Moderate
        historyRecorder.getNakedPairCount() > 0 -> GameDifficulty.Moderate
        isHiddenSingleCountAboveModerateThreshold() -> GameDifficulty.Moderate
        isSingleCountAboveEasyThreshold() -> GameDifficulty.Easy
        else -> GameDifficulty.Unspecified
    }

/**
 * Get the gameDifficulty rating.
 */
internal fun QQWing.getDifficultyAsString(): String = getDifficulty().name

private fun QQWing.isHiddenSingleCountAboveModerateThreshold(): Boolean {
    val threshold =
        when (gameType) {
            GameType.Default6x6 -> HIDDEN_SINGLE_THRESHOLD_6X6
            GameType.Default9x9 -> HIDDEN_SINGLE_THRESHOLD_9X9
            GameType.Default12x12 -> HIDDEN_SINGLE_THRESHOLD_12X12
            else -> HIDDEN_SINGLE_THRESHOLD_DEFAULT
        }
    return historyRecorder.getHiddenSingleCount() > threshold
}

private fun QQWing.isSingleCountAboveEasyThreshold(): Boolean {
    val threshold =
        when (gameType) {
            GameType.Default6x6 -> SINGLE_COUNT_THRESHOLD_6X6
            GameType.Default9x9 -> SINGLE_COUNT_THRESHOLD_9X9
            GameType.Default12x12 -> SINGLE_COUNT_THRESHOLD_12X12
            else -> SINGLE_COUNT_THRESHOLD_DEFAULT
        }
    return historyRecorder.getSingleCount() > threshold
}
