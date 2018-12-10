package com.tkachenkod.ltimer.model

import com.tkachenkod.ltimer.database.dao.TaskDao
import com.tkachenkod.ltimer.database.dao.TimeRecordDao
import com.tkachenkod.ltimer.entity.Task
import com.tkachenkod.ltimer.entity.TaskWithTimeRecords
import com.tkachenkod.ltimer.entity.TimeRecord
import com.tkachenkod.ltimer.utils.Optional
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.rxkotlin.zipWith
import io.reactivex.schedulers.Schedulers
import org.threeten.bp.OffsetDateTime

class TimerModel(
    private val taskDao: TaskDao,
    private val timeRecordDao: TimeRecordDao
) {

    fun lastTasks(): Observable<List<Task>> {
        return taskDao.taskWithTimeRecords()
            .map { tasksWithTimeRecords ->
                tasksWithTimeRecords
                    .filter { it.timeRecords.isNotEmpty() }
                    .sortedByDescending { it ->
                        it.timeRecords
                            .map(TimeRecord::startTime)
                            .sorted()
                            .lastOrNull()
                    }
                    .map(TaskWithTimeRecords::task)
            }
            .subscribeOn(Schedulers.io())
            .toObservable()
    }

    fun currentTimeRecord(): Observable<Optional<TimeRecord>> {
        return timeRecordDao.timeRecords()
            .map { allTimeRecords ->
                allTimeRecords.firstOrNull { it.endTime == null }
                    .let { Optional.ofNullable(it) }
            }
            .subscribeOn(Schedulers.io())
            .toObservable()
    }

    fun currentTask(): Observable<Optional<Task>> {
        return currentTimeRecord()
            .flatMapSingle { optionalCurrentTimeRecord ->
                when (optionalCurrentTimeRecord) {
                    is Optional.Some -> {
                        taskDao.getById(optionalCurrentTimeRecord.value.taskId)
                            .map { Optional.Some(it) }
                    }
                    else -> Single.just(Optional.EMPTY)
                }
            }
            .subscribeOn(Schedulers.io())
    }

    fun start(taskName: String): Single<TimeRecord> {
        return taskDao.findByName(taskName)
            .map(Task::id)
            .switchIfEmpty(taskDao.insert(Task(name = taskName)))
            .map { taskId ->
                TimeRecord(
                    startTime = OffsetDateTime.now(),
                    taskId = taskId
                )
            }
            .flatMap(timeRecordDao::insert)
            .flatMap(timeRecordDao::getById)
            .subscribeOn(Schedulers.io())
    }

    fun edit(timeRecordId: Long, taskName: String): Completable {
        return taskDao.findByName(taskName)
            .map(Task::id)
            .switchIfEmpty(taskDao.insert(Task(name = taskName)))
            .zipWith(timeRecordDao.getById(timeRecordId))
            .map { (newTaskId, timeRecord) ->
                timeRecord.copy(
                    taskId = newTaskId
                )
            }
            .flatMap(timeRecordDao::insert)
            .subscribeOn(Schedulers.io())
            .ignoreElement()
    }

    fun stop(timeRecordId: Long): Completable {
        return timeRecordDao.findById(timeRecordId)
            .map { timeRecord ->
                timeRecord.copy(
                    endTime = OffsetDateTime.now()
                )
            }
            .flatMapSingle(timeRecordDao::insert)
            .subscribeOn(Schedulers.io())
            .ignoreElement()
    }

    fun delete(timeRecordId: Long): Completable {
        return timeRecordDao.findById(timeRecordId)
            .flatMapCompletable(timeRecordDao::delete)
            .subscribeOn(Schedulers.io())
    }
}