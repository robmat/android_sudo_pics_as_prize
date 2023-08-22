package com.batodev.sudoku.data.database.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.Companion.CASCADE
import androidx.room.PrimaryKey
import com.batodev.sudoku.core.qqwing.GameDifficulty
import com.batodev.sudoku.core.qqwing.GameType
import java.time.Duration
import java.time.ZonedDateTime

@Entity(
    tableName = "record",
    foreignKeys = [ForeignKey(
        onDelete = CASCADE,
        entity = SudokuBoard::class,
        parentColumns = arrayOf("uid"),
        childColumns = arrayOf("board_uid")
    )]
)
data class Record(
    @PrimaryKey @ColumnInfo(name = "board_uid") val board_uid: Long,
    @ColumnInfo(name = "type") val type: GameType,
    @ColumnInfo(name = "difficulty") val difficulty: GameDifficulty,
    @ColumnInfo(name = "date") val date: ZonedDateTime,
    @ColumnInfo(name = "time") val time: Duration
)