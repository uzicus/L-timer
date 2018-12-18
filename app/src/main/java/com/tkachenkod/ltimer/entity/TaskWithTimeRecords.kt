package com.tkachenkod.ltimer.entity

import androidx.room.Embedded
import androidx.room.Relation
import com.tkachenkod.ltimer.extension.sumByLong

data class TaskWithTimeRecords(

    @Embedded
    val task: Task,

    @Relation(parentColumn = "id", entityColumn = "task_id")
    val timeRecords: List<TimeRecord>

) {

    val timeRecordsDuration: Long
        get() = timeRecords.sumByLong {
            it.endTime?.toEpochSecond()?.minus(it.startTime.toEpochSecond()) ?: 0
        }

}