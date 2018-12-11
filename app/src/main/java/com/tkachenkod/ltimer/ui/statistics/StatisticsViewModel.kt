package com.tkachenkod.ltimer.ui.statistics

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.tkachenkod.ltimer.entity.TaskWithTimeRecords
import com.tkachenkod.ltimer.model.TimerModel
import com.tkachenkod.ltimer.ui.base.BaseViewModel

class StatisticsViewModel(
    timerModel: TimerModel
): BaseViewModel() {

    enum class Period {
        DAY,
        WEEK,
        MONTH,
        YEAR
    }

    private val _period = MutableLiveData<Period>()
    private val _tasks = MutableLiveData<List<TaskWithTimeRecords>>()

    val period: LiveData<Period> = _period
    val tasks: LiveData<List<TaskWithTimeRecords>> = _tasks

    init {
        _period.value = Period.DAY

        _tasks

        timerModel.tasksWithTimeRecords()
            .subscribe { _tasks.postValue(it) }
            .untilDestroy()
    }

    fun dayLabelClicks() {
        _period.postValue(Period.DAY)
    }

    fun weekLabelClicks() {
        _period.postValue(Period.WEEK)
    }

    fun monthLabelClicks() {
        _period.postValue(Period.MONTH)
    }

    fun yearLabelClicks() {
        _period.postValue(Period.YEAR)
    }

}