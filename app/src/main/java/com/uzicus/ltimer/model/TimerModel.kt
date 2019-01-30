package com.uzicus.ltimer.model

import android.annotation.SuppressLint
import com.jakewharton.rxrelay2.PublishRelay
import com.uzicus.ltimer.database.dao.TaskDao
import com.uzicus.ltimer.database.dao.TimeRecordDao
import com.uzicus.ltimer.entity.Task
import com.uzicus.ltimer.entity.TaskWithTimeRecords
import com.uzicus.ltimer.entity.TimeRecord
import com.uzicus.ltimer.utils.ColorHelper
import com.uzicus.ltimer.utils.Optional
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.rxkotlin.zipWith
import io.reactivex.schedulers.Schedulers
import org.threeten.bp.OffsetDateTime

@SuppressLint("CheckResult")
class TimerModel(
    private val taskDao: TaskDao,
    private val timeRecordDao: TimeRecordDao,
    private val colorHelper: ColorHelper
) {

    private var isColorInitialized: Boolean = false

    private val savedTimeRecord = PublishRelay.create<TimeRecord>()

    init {

        taskDao.taskWithTimeRecords()
            .toObservable()
            .map { tasksWithTimeRecords ->
                val filteredTasks = tasksWithTimeRecords.map {
                    val lastTime = it.timeRecords
                        .map(TimeRecord::startTime)
                        .sorted()
                        .lastOrNull()

                    val filteredTimeRecords = it.timeRecords.filter { timeRecord ->
                        lastTime?.minusDays(1)?.isBefore(timeRecord.startTime) ?: false
                    }

                    it.copy(timeRecords = filteredTimeRecords)
                }

                filteredTasks
                    .sortedByDescending(TaskWithTimeRecords::timeRecordsDuration)
                    .mapIndexed { index, taskWithTimeRecords ->
                        taskWithTimeRecords.task to index
                    }
                    .filter { (task, _) ->
                        // if startup colorize
                        isColorInitialized.not() || task.color == null
                    }
                    .map { (task, index) ->
                        task.id to colorHelper.getColorByIndex(index)
                    }
            }
            .flatMap {
                isColorInitialized = true
                Observable.fromIterable(it)
            }
            .flatMapCompletable { (taskId, color) ->
                Completable.fromCallable {
                    taskDao.updateColorTask(taskId, color)
                }
            }
            .subscribeOn(Schedulers.io())
            .subscribe()
    }

    fun savedTimeRecord(): Observable<TimeRecord> = savedTimeRecord.hide()

    fun tasksWithTimeRecords(): Observable<List<TaskWithTimeRecords>> {
        return taskDao.taskWithTimeRecords()
            .filter { it.all { it.task.color != null } }
            .toObservable()
            .subscribeOn(Schedulers.io())
    }

    fun lastTasks(): Observable<List<Task>> {
        return tasksWithTimeRecords()
            .map { tasksWithTimeRecords ->
                tasksWithTimeRecords
                    .filter {
                        it.timeRecords.any { timeRecord ->
                            timeRecord.endTime != null
                        }
                    }
                    .sortedByDescending {
                        it.timeRecords
                            .filter { it.endTime != null }
                            .map(TimeRecord::startTime)
                            .sorted()
                            .lastOrNull()
                    }
                    .map(TaskWithTimeRecords::task)
            }
            .subscribeOn(Schedulers.io())
    }

    fun currentTimeRecord(): Observable<Optional<TimeRecord>> {
        return timeRecordDao.timeRecords()
            .distinctUntilChanged()
            .map { allTimeRecords ->
                allTimeRecords.firstOrNull { it.endTime == null }
                    .let { Optional.ofNullable(it) }
            }
            .subscribeOn(Schedulers.io())
            .toObservable()
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
            .flatMap(timeRecordDao::getById)
            .doOnSuccess(savedTimeRecord::accept)
            .subscribeOn(Schedulers.io())
            .ignoreElement()
    }

    fun delete(timeRecordId: Long): Completable {
        return timeRecordDao.findById(timeRecordId)
            .flatMapCompletable(timeRecordDao::delete)
            .subscribeOn(Schedulers.io())
    }
}