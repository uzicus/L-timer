package com.uzicus.ltimer.entity

import android.os.SystemClock
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import org.threeten.bp.Instant
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
    val taskId: Long

) {

    val duration: Long
        get() {
            val endEpochSecond = endTime?.toEpochSecond()
                ?: Instant.now().epochSecond

            return endEpochSecond - startTime.toEpochSecond()
        }

    val elapsedRealtime: Long
        get() = SystemClock.elapsedRealtime() - (duration * 1000)
}