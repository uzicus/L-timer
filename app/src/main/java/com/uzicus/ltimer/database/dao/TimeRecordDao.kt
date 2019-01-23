package com.uzicus.ltimer.database.dao

import androidx.room.*
import com.uzicus.ltimer.entity.TimeRecord
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Maybe
import io.reactivex.Single

@Dao
interface TimeRecordDao {

    @Query("SELECT * FROM time_records")
    fun timeRecords(): Flowable<List<TimeRecord>>

    @Query("SELECT * FROM time_records WHERE id = :timeRecordId")
    fun getById(timeRecordId: Long): Single<TimeRecord>

    @Query("SELECT * FROM time_records WHERE id = :timeRecordId")
    fun findById(timeRecordId: Long): Maybe<TimeRecord>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(timeRecord: TimeRecord): Single<Long>

    @Delete
    fun delete(timeRecord: TimeRecord): Completable
}