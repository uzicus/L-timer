package com.tkachenkod.ltimer.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.tkachenkod.ltimer.database.dao.TaskDao
import com.tkachenkod.ltimer.database.dao.TimeRecordDao
import com.tkachenkod.ltimer.entity.Task
import com.tkachenkod.ltimer.entity.TimeRecord

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