package com.tkachenkod.ltimer.model

import android.annotation.SuppressLint
import com.tkachenkod.ltimer.database.dao.TaskDao
import com.tkachenkod.ltimer.database.dao.TimeRecordDao
import com.tkachenkod.ltimer.entity.Task
import com.tkachenkod.ltimer.entity.TaskWithTimeRecords
import com.tkachenkod.ltimer.entity.TimeRecord
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.threeten.bp.OffsetDateTime
import timber.log.Timber

class TimerModel(
    private val taskDao: TaskDao,
    private val timeRecordDao: TimeRecordDao
) {

    var currentTimer: OffsetDateTime? = null

    fun lastTasks(): Observable<List<Task>> {
        return taskDao.taskWithTimeRecords()
            .map { tasksWithTimeRecords ->
                tasksWithTimeRecords
                    .sortedByDescending { it ->
                        it.timeRecords
                            .map(TimeRecord::startTime)
                            .sorted()
                            .lastOrNull()
                    }
                    .map(TaskWithTimeRecords::task)
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .toObservable()
    }

    @SuppressLint("CheckResult")
    fun start(taskName: String) {
        if (currentTimer == null) {
            val now = OffsetDateTime.now()

            taskDao.findByName(taskName)
                .map(Task::id)
                .switchIfEmpty(taskDao.insert(Task(name = taskName)))
                .subscribeOn(Schedulers.io())
                .flatMap { createdTaskId ->
                    val timeRecord = TimeRecord(
                        startTime = now,
                        taskId = createdTaskId
                    )
                    timeRecordDao.insert(timeRecord)
                }
                .subscribe({ currentTimer = now }, Timber::e)
        }
    }

    fun stop() {
        currentTimer = null
    }
}