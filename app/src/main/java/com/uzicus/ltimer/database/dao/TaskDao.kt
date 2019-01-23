package com.uzicus.ltimer.database.dao

import androidx.room.*
import com.uzicus.ltimer.entity.Task
import com.uzicus.ltimer.entity.TaskWithTimeRecords
import io.reactivex.Flowable
import io.reactivex.Maybe
import io.reactivex.Single

@Dao
interface TaskDao {

    @Query("SELECT * FROM tasks")
    fun tasks(): Flowable<List<Task>>

    @Query("SELECT * FROM tasks WHERE id = :taskId")
    fun getById(taskId: Long): Single<Task>

    @Query("SELECT * FROM tasks WHERE name = :taskName")
    fun findByName(taskName: String): Maybe<Task>

    @Transaction
    @Query("SELECT id, name, color from tasks")
    fun taskWithTimeRecords(): Flowable<List<TaskWithTimeRecords>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(task: Task): Single<Long>

    @Query("UPDATE tasks SET color = :color WHERE id = :taskId")
    fun updateColorTask(taskId: Long, color: Int?)

}