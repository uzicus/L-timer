package com.tkachenkod.ltimer.database.dao

import androidx.room.Dao
import androidx.room.Insert
import com.tkachenkod.ltimer.entity.TimeRecord
import io.reactivex.Single

@Dao
interface TimeRecordDao {

    @Insert
    fun insert(timeRecord: TimeRecord): Single<Long>

}