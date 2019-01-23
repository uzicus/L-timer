package com.uzicus.ltimer.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.uzicus.ltimer.database.dao.TaskDao
import com.uzicus.ltimer.database.dao.TimeRecordDao
import com.uzicus.ltimer.entity.Task
import com.uzicus.ltimer.entity.TimeRecord

@Database(
    entities = [
        Task::class,
        TimeRecord::class
    ],
    version = 1,
    exportSchema = false)
@TypeConverters(DateTimeTypeConverters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun taskDao(): TaskDao

    abstract fun timeRecordDao(): TimeRecordDao

}