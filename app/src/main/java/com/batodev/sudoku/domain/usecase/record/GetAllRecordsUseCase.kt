package com.batodev.sudoku.domain.usecase.record

import com.batodev.sudoku.core.qqwing.GameDifficulty
import com.batodev.sudoku.core.qqwing.GameType
import com.batodev.sudoku.domain.repository.RecordRepository
import javax.inject.Inject

class GetAllRecordsUseCase @Inject constructor(
    private val recordRepository: RecordRepository
) {
    operator fun invoke(difficulty: GameDifficulty, type: GameType) = recordRepository.getAll(difficulty, type)
}