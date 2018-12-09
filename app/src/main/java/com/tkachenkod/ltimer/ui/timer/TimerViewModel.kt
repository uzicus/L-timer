package com.tkachenkod.ltimer.ui.timer

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.shopify.livedataktx.SingleLiveData
import com.tkachenkod.ltimer.entity.Task
import com.tkachenkod.ltimer.model.TimerModel
import com.tkachenkod.ltimer.ui.base.BaseViewModel
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.rxkotlin.withLatestFrom
import java.util.concurrent.TimeUnit

class TimerViewModel(
    private val timerModel: TimerModel
): BaseViewModel() {

    private val _lastTasks = MutableLiveData<List<Task>>()
    private val _taskName = MutableLiveData<String>()
    private val _taskNameInput = MutableLiveData<String>()
    private val _taskNameState = MutableLiveData<TaskNameState>()
    private val _screenState = MutableLiveData<ScreenState>()
    private val _timerSeconds = MutableLiveData<Long>()
    private val _shakeTaskName = SingleLiveData<Unit>()

    enum class TaskNameState {
        ENTERING,
        SHOWING,
        NOTHING
    }

    enum class ScreenState {
        DASHBOARD,
        ENTERING_TASK,
        RECORDING
    }

    val lastTasks: LiveData<List<Task>> = _lastTasks
    val taskName: LiveData<String> = _taskName
    val taskNameInput: LiveData<String> = _taskNameInput
    val taskNameState: LiveData<TaskNameState> = _taskNameState
    val screenState: LiveData<ScreenState> = _screenState
    val timerSeconds: LiveData<Long> = _timerSeconds
    val shakeTaskName: LiveData<Unit> = _shakeTaskName

    init {

        timerModel.currentTimeRecord()
            .firstOrError()
            .retry()
            .subscribe { currentTimeRecord ->
                if (currentTimeRecord.isEmpty.not()) {
                    _taskNameState.postValue(TaskNameState.SHOWING)
                    _screenState.postValue(ScreenState.RECORDING)
                } else {
                    _taskNameState.postValue(TaskNameState.NOTHING)
                    _screenState.postValue(ScreenState.DASHBOARD)
                }

                _timerSeconds.postValue(currentTimeRecord.valueOrNull?.duration ?: 0)
            }
            .untilDestroy()

        timerModel.lastTasks()
            .retry()
            .subscribe(_lastTasks::postValue)
            .untilDestroy()

        timerModel.currentTask()
            .map { currentTask ->
                currentTask.valueOrNull?.name.orEmpty()
            }
            .subscribe(_taskName::postValue)
            .untilDestroy()

        Observable.interval(0, 1, TimeUnit.SECONDS)
            .withLatestFrom(timerModel.currentTimeRecord())
            .map { (_, currentTimeRecord) ->
                currentTimeRecord.valueOrNull?.duration ?: 0
            }
            .subscribe(_timerSeconds::postValue)
            .untilDestroy()
    }

    fun taskNameInputChange(taskName: String) {
        _taskNameInput.value = taskName
    }

    fun buttonClicks() {
        when (_screenState.value) {
            ScreenState.DASHBOARD -> {
                _taskNameInput.postValue("")
                _taskNameState.postValue(TaskNameState.ENTERING)
                _screenState.postValue(ScreenState.ENTERING_TASK)
            }
            ScreenState.ENTERING_TASK -> {
                if (_taskNameInput.value.orEmpty().isNotBlank()) {
                    _screenState.postValue(ScreenState.RECORDING)
                    _taskNameState.postValue(TaskNameState.SHOWING)
                    startTimer(_taskNameInput.value.orEmpty().trim())
                } else {
                    _shakeTaskName.postValue(Unit)
                }
            }
            ScreenState.RECORDING -> {
                _screenState.postValue(ScreenState.DASHBOARD)
                _taskNameState.postValue(TaskNameState.NOTHING)
                stopTimer()
            }
        }
    }

    fun taskNameClicks() {
        if (taskNameState.value == TaskNameState.SHOWING) {
            _taskNameInput.postValue(_taskName.value)
            _taskNameState.postValue(TaskNameState.ENTERING)
        }
    }

    fun taskNameActionDone() {
        timerModel.currentTimeRecord()
            .firstOrError()
            .subscribe { timeRecordOptional ->
                val timeRecord = timeRecordOptional.valueOrNull

                if (_screenState.value == ScreenState.ENTERING_TASK) {
                    buttonClicks()
                } else if (_screenState.value == ScreenState.RECORDING && timeRecord != null) {
                    if (_taskNameInput.value.orEmpty().isNotBlank()) {
                        timerModel.edit(timeRecord.id, _taskNameInput.value.orEmpty().trim())
                            .subscribe {
                                _taskNameState.postValue(TaskNameState.SHOWING)
                            }
                            .untilDestroy()
                    } else {
                        _shakeTaskName.postValue(Unit)
                    }
                }
            }
            .untilDestroy()
    }

    fun lastTasksItemClicks(task: Task) {

    }

    fun onBackPressed(): Boolean {
        when {
            _screenState.value == ScreenState.ENTERING_TASK -> {
                _taskNameState.postValue(TaskNameState.NOTHING)
                _screenState.postValue(ScreenState.DASHBOARD)
            }
            _taskNameState.value == TaskNameState.ENTERING -> {
                _taskNameState.postValue(TaskNameState.SHOWING)
            }
            else -> return true
        }
        return false
    }

    private fun startTimer(taskName: String) {
        timerModel.start(taskName)
            .subscribe()
            .untilDestroy()
    }

    private fun stopTimer() {
        timerModel.currentTimeRecord()
            .firstOrError()
            .flatMapMaybe { currentTimeRecord ->
                currentTimeRecord.valueOrNull?.let { Maybe.just(it) } ?: Maybe.empty()
            }
            .flatMapCompletable { currentTimeRecord ->
                timerModel.stop(currentTimeRecord.id)
            }
            .subscribe()
            .untilDestroy()
    }

}