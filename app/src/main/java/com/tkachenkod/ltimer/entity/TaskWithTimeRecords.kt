package com.tkachenkod.ltimer.entity

import androidx.room.Embedded
import androidx.room.Relation

data class TaskWithTimeRecords(

    @Embedded
    val task: Task,

    @Relation(parentColumn = "id", entityColumn = "task_id")
    val timeRecords: List<TimeRecord>

)