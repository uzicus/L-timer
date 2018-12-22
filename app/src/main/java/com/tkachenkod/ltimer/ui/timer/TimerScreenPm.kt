package com.tkachenkod.ltimer.ui.timer

import com.tkachenkod.ltimer.entity.Task
import com.tkachenkod.ltimer.entity.TimeRecord
import com.tkachenkod.ltimer.extension.inject
import com.tkachenkod.ltimer.model.TimerModel
import com.tkachenkod.ltimer.ui.BackMessage
import com.tkachenkod.ltimer.ui.ShowTimerNotificationMessage
import com.tkachenkod.ltimer.ui.base.BaseScreenPm
import com.tkachenkod.ltimer.utils.toMaybeValue
import io.reactivex.Completable
import io.reactivex.rxkotlin.Maybes.zip
import io.reactivex.rxkotlin.withLatestFrom
import me.dmdev.rxpm.widget.inputControl

class TimerScreenPm : BaseScreenPm() {

    private val timerModel: TimerModel by inject()

    private val cancelableSavedTimeRecords = mutableListOf<TimeRecord>()

    enum class TaskNameState {
        ENTERING,
        SHOWING,
        NOTHING
    }

    enum class ScreenState {
        DASHBOARD,
        ENTERING_TASK,
        RECORDING,
    }

    val screenState = State<ScreenState>()
    val taskNameState = State<TaskNameState>()
    val taskName = State<String>()
    val timerSeconds = State<Long>()
    val lastTasks = State<List<Task>>(emptyList())

    val taskNameInputControl = inputControl()

    val buttonClicks = Action<Unit>()
    val taskNameClicks = Action<Unit>()
    val taskNameActionDone = Action<Unit>()
    val lastTasksItemClicks = Action<Task>()
    val cancelSave = Action<Task>()
    val taskSavedMsgDismissed = Action<Task>()

    val shakeTaskName = Command<Unit>()
    val showTaskSavedMsg = Command<Task>()

    override val backAction = Action<Unit>()

    override fun onCreate() {
        super.onCreate()

        timerModel.currentTimerIntervalObservable()
            .subscribe { optionalCurrentTimeRecord ->
                val currentTimeRecord = optionalCurrentTimeRecord.valueOrNull
                val currentScreenState = screenState.valueOrNull

                if (currentTimeRecord != null && currentScreenState != ScreenState.ENTERING_TASK) {
                    taskNameState.consumer.accept(TaskNameState.SHOWING)
                    screenState.consumer.accept(ScreenState.RECORDING)
                    sendMessage(ShowTimerNotificationMessage(currentTimeRecord.duration))
                } else if (currentScreenState != ScreenState.DASHBOARD && currentScreenState != ScreenState.ENTERING_TASK) {
                    taskNameState.consumer.accept(TaskNameState.NOTHING)
                    screenState.consumer.accept(ScreenState.DASHBOARD)
                }

                timerSeconds.consumer.accept(currentTimeRecord?.duration ?: 0)
            }
            .untilDestroy()

        timerModel.lastTasks()
            .retry()
            .subscribe(lastTasks.consumer)
            .untilDestroy()

        timerModel.currentTask()
            .map { currentTask ->
                currentTask.valueOrNull?.name.orEmpty()
            }
            .subscribe(taskName.consumer)
            .untilDestroy()

        buttonClicks.observable
            .withLatestFrom(screenState.observable, taskNameInputControl.text.observable)
            .subscribe { (_, currentScreenState, taskNameInput) ->
                when (currentScreenState) {
                    ScreenState.DASHBOARD -> {
                        taskNameInputControl.text.consumer.accept("")
                        taskNameState.consumer.accept(TaskNameState.ENTERING)
                        screenState.consumer.accept(ScreenState.ENTERING_TASK)
                    }
                    ScreenState.ENTERING_TASK -> {
                        if (taskNameInput.isNotBlank()) {
                            startTimer(taskNameInput.trim())
                            screenState.consumer.accept(ScreenState.RECORDING)
                            taskNameState.consumer.accept(TaskNameState.SHOWING)
                        } else {
                            shakeTaskName.consumer.accept(Unit)
                        }
                    }
                    else -> { stopTimer() }
                }
            }
            .untilDestroy()

        taskNameClicks.observable
            .withLatestFrom(taskName.observable, taskNameState.observable)
            .subscribe { (_, taskName, currentTaskNameState) ->
                if (currentTaskNameState == TaskNameState.SHOWING) {
                    taskNameInputControl.text.consumer.accept(taskName)
                    taskNameState.consumer.accept(TaskNameState.ENTERING)
                }
            }
            .untilDestroy()

        taskNameActionDone.observable
            .withLatestFrom(
                timerModel.currentTimeRecord(),
                screenState.observable,
                taskNameInputControl.text.observable
            ) { _, timeRecordOptional, currentScreenState, taskNameInput ->
                Triple(timeRecordOptional, currentScreenState, taskNameInput)
            }
            .subscribe { (timeRecordOptional, currentScreenState, taskNameInput) ->
                val timeRecord = timeRecordOptional.valueOrNull

                if (currentScreenState == ScreenState.ENTERING_TASK) {
                    buttonClicks.consumer.accept(Unit)
                } else if (currentScreenState == ScreenState.RECORDING && timeRecord != null) {
                    if (taskNameInput.isNotBlank()) {
                        timerModel.edit(timeRecord.id, taskNameInput.trim())
                            .subscribe {
                                taskNameState.consumer.accept(TaskNameState.SHOWING)
                            }
                            .untilDestroy()
                    } else {
                        shakeTaskName.consumer.accept(Unit)
                    }
                }
            }
            .untilDestroy()

        lastTasksItemClicks.observable
            .withLatestFrom(screenState.observable)
            .subscribe { (task, currentScreenState) ->
                if (currentScreenState != ScreenState.RECORDING) {
                    startTimer(task.name)
                    taskNameInputControl.text.consumer.accept(task.name)
                    screenState.consumer.accept(ScreenState.RECORDING)
                    taskNameState.consumer.accept(TaskNameState.SHOWING)
                }
            }
            .untilDestroy()

        cancelSave.observable
            .flatMapCompletable { task ->
                val doCancelTimeRecord = cancelableSavedTimeRecords.find { it.taskId == task.id }

                if (doCancelTimeRecord != null) {
                    timerModel.delete(doCancelTimeRecord.id)
                } else {
                    Completable.never()
                }
            }
            .subscribe()
            .untilDestroy()

        taskSavedMsgDismissed.observable
            .subscribe { task ->
                cancelableSavedTimeRecords.removeAll {
                    it.taskId == task.id
                }
            }
            .untilDestroy()

        backAction.observable
            .withLatestFrom(screenState.observable, taskNameState.observable)
            .subscribe { (_, currentScreenState, currentTaskNameState) ->
                when {
                    currentScreenState == ScreenState.ENTERING_TASK -> {
                        taskNameState.consumer.accept(TaskNameState.NOTHING)
                        screenState.consumer.accept(ScreenState.DASHBOARD)
                    }
                    currentTaskNameState == TaskNameState.ENTERING -> {
                        taskNameState.consumer.accept(TaskNameState.SHOWING)
                    }
                    else -> {
                        sendMessage(BackMessage())
                    }
                }
            }
            .untilDestroy()
    }

    private fun startTimer(taskName: String) {
        timerModel.start(taskName)
            .subscribe()
            .untilDestroy()
    }

    private fun stopTimer() {
        val currentTimeRecord = timerModel.currentTimeRecord()
            .firstOrError()
            .toMaybeValue()

        val currentTask = timerModel.currentTask()
            .firstOrError()
            .toMaybeValue()

        zip(currentTimeRecord, currentTask)
            .flatMapSingle { (currentTimeRecord, currentTask) ->
                timerModel.stop(currentTimeRecord.id)
                    .toSingleDefault(currentTimeRecord to currentTask)
            }
            .subscribe { (savedTimeRecord, savedTask) ->
                cancelableSavedTimeRecords.add(savedTimeRecord)
                showTaskSavedMsg.consumer.accept(savedTask)
            }
            .untilDestroy()
    }
}