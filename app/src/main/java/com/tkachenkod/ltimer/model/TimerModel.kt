package com.tkachenkod.ltimer.model

import android.annotation.SuppressLint
import com.jakewharton.rxrelay2.PublishRelay
import com.tkachenkod.ltimer.database.dao.TaskDao
import com.tkachenkod.ltimer.database.dao.TimeRecordDao
import com.tkachenkod.ltimer.entity.Task
import com.tkachenkod.ltimer.entity.TaskWithTimeRecords
import com.tkachenkod.ltimer.entity.TimeRecord
import com.tkachenkod.ltimer.extension.sumByLong
import com.tkachenkod.ltimer.utils.ColorHelper
import com.tkachenkod.ltimer.utils.Optional
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.rxkotlin.Observables.combineLatest
import io.reactivex.rxkotlin.toObservable
import io.reactivex.rxkotlin.withLatestFrom
import io.reactivex.rxkotlin.zipWith
import io.reactivex.schedulers.Schedulers
import org.threeten.bp.OffsetDateTime
import org.threeten.bp.ZoneOffset
import java.util.concurrent.TimeUnit
import kotlin.random.Random

@SuppressLint("CheckResult")
class TimerModel(
    private val taskDao: TaskDao,
    private val timeRecordDao: TimeRecordDao,
    private val colorHelper: ColorHelper
) {

    private var isColorInitialized: Boolean = false

    private val currentTimer = PublishRelay.create<Optional<TimeRecord>>()

    init {

        combineLatest(
            Observable.interval(0, 1, TimeUnit.SECONDS),
            currentTimeRecord()
        )
            .map { (_, optionalTimeRecord) -> optionalTimeRecord }
            .subscribe(currentTimer)

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
/*
        listOf(
            "work",
            "games",
            "skateboard",
            "eating",
            "sleeping",
            "chatting"
        )
            .toObservable()
            .map { Task(name = it) }
            .flatMapSingle(taskDao::insert)
            .flatMapSingle(taskDao::getById)
            .flatMap { task ->
                (0 until Random.nextInt(50, 1000)).map {
                    val start = OffsetDateTime.of(
                        2018,
                        Random.nextInt(1, 12),
                        Random.nextInt(1, 25),
                        Random.nextInt(0, 23),
                        Random.nextInt(0, 59),
                        0, 0, ZoneOffset.UTC
                    )
                    val end = start.plusMinutes(Random.nextLong(1, 150))
                    TimeRecord(
                        startTime = start,
                        endTime = end,
                        taskId = task.id
                    )
                }
                    .toObservable()
                    .flatMapSingle(timeRecordDao::insert)
            }
            .subscribeOn(Schedulers.io())
            .subscribe()
*/
    }

    fun currentTimerIntervalObservable(): Observable<Optional<TimeRecord>> {
        return currentTimer.hide()
    }

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