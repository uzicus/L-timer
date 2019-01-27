package com.uzicus.ltimer.database.dao

import androidx.room.*
import com.uzicus.ltimer.entity.TimeRecord
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Maybe
import io.reactivex.Single

@Dao
interface TimeRecordDao {

    @Transaction
    @Query("SELECT * FROM time_records JOIN tasks ON relation_task_id = tasks.task_id")
    fun timeRecords(): Flowable<List<TimeRecord>>

    @Transaction
    @Query("SELECT * FROM time_records JOIN tasks ON relation_task_id = tasks.task_id WHERE time_records_id = :timeRecordId")
    fun getById(timeRecordId: Long): Single<TimeRecord>

    @Transaction
    @Query("SELECT * FROM time_records JOIN tasks ON relation_task_id = tasks.task_id WHERE time_records_id = :timeRecordId")
    fun findById(timeRecordId: Long): Maybe<TimeRecord>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(timeRecord: TimeRecord): Single<Long>

    @Delete
    fun delete(timeRecord: TimeRecord): Completable
}