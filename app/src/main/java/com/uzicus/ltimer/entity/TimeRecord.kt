package com.uzicus.ltimer.entity

import android.os.SystemClock
import androidx.room.*
import org.threeten.bp.Instant
import org.threeten.bp.OffsetDateTime

@Entity(
    tableName = "time_records",
    foreignKeys = [
        ForeignKey(
            entity = Task::class,
            parentColumns = ["task_id"],
            childColumns = ["relation_task_id"]
        )
    ]
)
data class TimeRecord(

    @ColumnInfo(name = "time_records_id")
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "start_time")
    val startTime: OffsetDateTime,

    @ColumnInfo(name = "end_time")
    val endTime: OffsetDateTime? = null,

    @ColumnInfo(name = "relation_task_id")
    val taskId: Long,

    @Embedded
    val task: Task = Task(name = "")

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