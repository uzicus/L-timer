package com.tkachenkod.ltimer.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import org.threeten.bp.OffsetDateTime

@Entity(tableName = "time_records")
data class TimeRecord(

    @ColumnInfo(name = "id")
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "start_time")
    val startTime: OffsetDateTime,

    @ColumnInfo(name = "end_time")
    val endTime: OffsetDateTime? = null,

    @ColumnInfo(name = "task_id")
    val taskId: Long? = null

)