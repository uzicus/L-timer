package com.tkachenkod.ltimer.ui.statistics

import com.tkachenkod.ltimer.entity.TaskWithTimeRecords
import com.tkachenkod.ltimer.entity.TimeRecord
import com.tkachenkod.ltimer.extension.inject
import com.tkachenkod.ltimer.extension.sumByLong
import com.tkachenkod.ltimer.model.TimerModel
import com.tkachenkod.ltimer.ui.base.BaseScreenPm
import io.reactivex.rxkotlin.Observables.combineLatest

class StatisticsScreenPm : BaseScreenPm() {

    private val timerModel: TimerModel by inject()

    enum class Period {
        DAY,
        WEEK,
        MONTH,
        YEAR
    }

    val period = State(Period.DAY)
    val chartTasks = State<List<StatisticsTask>>()
    val listTasks = State<List<StatisticsTask>>()

    val labelClicks = Action<Period>()

    override fun onCreate() {
        super.onCreate()

        timerModel.tasksWithTimeRecords()
            .firstOrError()
            .subscribe()
            .untilDestroy()

        combineLatest(
            timerModel.tasksWithTimeRecords(),
            period.observable
        )
            .map { (tasks, period) ->
                val tasksFilterAndSortOfDay = tasks.filterAndSortByPeriod(Period.DAY)
                    .mapIndexed { index, statisticsTask ->
                        statisticsTask.name to index
                    }
                    .toMap()

                val tasksList = tasks.filterAndSortByPeriod(period)
                val tasksChart = tasksList.sortedBy { tasksFilterAndSortOfDay[it.name] }

                tasksList to tasksChart
            }
            .distinctUntilChanged()
            .subscribe { (tasksList, tasksChart) ->
                listTasks.consumer.accept(tasksList)
                chartTasks.consumer.accept(tasksChart)
            }
            .untilDestroy()

        labelClicks.observable
            .subscribe(period.consumer)
            .untilDestroy()
    }

    private fun List<TaskWithTimeRecords>.filterAndSortByPeriod(period: Period): List<StatisticsTask> {
        val filteredTasks = this
            .filter { taskWithTimeRecords ->
                taskWithTimeRecords.timeRecordsDuration != 0L
            }
            .map {
                val lastTime = it.timeRecords
                    .map(TimeRecord::startTime)
                    .sorted()
                    .lastOrNull()

                val filteredTimeRecords = it.timeRecords.filter { timeRecord ->
                    when (period) {
                        Period.DAY -> {
                            lastTime?.minusDays(1)?.isBefore(timeRecord.startTime) ?: false
                        }
                        Period.WEEK -> {
                            lastTime?.minusWeeks(1)?.isBefore(timeRecord.startTime) ?: false
                        }
                        Period.MONTH -> {
                            lastTime?.minusMonths(1)?.isBefore(timeRecord.startTime) ?: false
                        }
                        else -> {
                            lastTime?.minusYears(1)?.isBefore(timeRecord.startTime) ?: false
                        }
                    }
                }

                it.copy(timeRecords = filteredTimeRecords)
            }

        val sumDuration = filteredTasks.sumByLong {
            it.timeRecords.sumByLong(TimeRecord::duration)
        }

        return filteredTasks.map { taskWithTimeRecords ->
            StatisticsTask(
                name = taskWithTimeRecords.task.name,
                color = taskWithTimeRecords.task.color,
                durationInSecond = taskWithTimeRecords.timeRecordsDuration,
                percent = (taskWithTimeRecords.timeRecordsDuration.toFloat() / sumDuration.toFloat())
            )
        }.sortedByDescending {
            it.durationInSecond
        }
    }
}