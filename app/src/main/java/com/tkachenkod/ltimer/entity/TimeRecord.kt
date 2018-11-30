package com.tkachenkod.ltimer.entity

import androidx.room.Entity
import org.threeten.bp.Instant

@Entity(tableName = "time_records")
data class TimeRecord(
    val id: String,
    val name: String,
    val startTime: Instant,
    val endTime: Instant? = null
)