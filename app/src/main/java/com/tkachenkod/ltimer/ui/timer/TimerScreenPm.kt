package com.tkachenkod.ltimer.ui.timer

import com.tkachenkod.ltimer.entity.Task
import com.tkachenkod.ltimer.entity.TimeRecord
import com.tkachenkod.ltimer.extension.inject
import com.tkachenkod.ltimer.model.TimerModel
import com.tkachenkod.ltimer.ui.BackMessage
import com.tkachenkod.ltimer.ui.base.BaseScreenPm
import com.tkachenkod.ltimer.utils.toMaybeValue
import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.rxkotlin.Maybes.zip
import io.reactivex.rxkotlin.withLatestFrom
import me.dmdev.rxpm.widget.inputControl

class TimerScreenPm : BaseScreenPm() {

    companion object {
        private const val LAST_TASKS_COUNT = 10
    }

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

    val timerChronometerBase = State<Long>()
    val screenState = State<ScreenState>()
    val taskNameState = State<TaskNameState>()
    val taskName = State<String>()
    val lastTasks = State<List<Task>>(emptyList())

    val taskNameInputControl = inputControl()

    val buttonClicks = Action<Unit>()
    val taskNameClicks = Action<Unit>()
    val taskNameActionDone = Action<Unit>()
    val lastTasksItemClicks = Action<Task>()
    val cancelSave = Action<Task>()
    val taskSavedMsgDismissed = Action<Task>()
    val timerTick = Action<Unit>()

    val shakeTaskName = Command<Unit>()
    val pulseButton = Command<Unit>()
    val showTaskSavedMsg = Command<Task>()

    override val backAction = Action<Unit>()

    override fun onCreate() {
        super.onCreate()

        taskName.observable
            .subscribe(taskNameInputControl.text.consumer)
            .untilDestroy()

        timerTick.observable
            .withLatestFrom(screenState.observable)
            .filter { (_, currentScreenState) -> currentScreenState == ScreenState.RECORDING }
            .map { Unit }
            .subscribe(pulseButton.consumer)
            .untilDestroy()

        timerModel.currentTimeRecord()
            .subscribe { optionalCurrentTimeRecord ->
                val currentTimeRecord = optionalCurrentTimeRecord.valueOrNull
                val currentScreenState = screenState.valueOrNull

                if (currentTimeRecord != null && currentScreenState != ScreenState.ENTERING_TASK) {
                    taskNameState.consumer.accept(TaskNameState.SHOWING)
                    screenState.consumer.accept(ScreenState.RECORDING)
                } else if (currentScreenState != ScreenState.DASHBOARD && currentScreenState != ScreenState.ENTERING_TASK) {
                    taskNameState.consumer.accept(TaskNameState.NOTHING)
                    screenState.consumer.accept(ScreenState.DASHBOARD)
                }

                if (currentTimeRecord != null && currentScreenState == ScreenState.RECORDING) {
                    timerChronometerBase.consumer.accept(currentTimeRecord.elapsedRealtime)
                }
            }
            .untilDestroy()

        timerModel.lastTasks()
            .retry()
            .map { it.take(LAST_TASKS_COUNT) }
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
                    else -> stopTimer()
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
        class TaskNameIsEmptyException : Exception()

        val currentTimeRecord = timerModel.currentTimeRecord()
            .firstOrError()
            .toMaybeValue()

        val currentTask = timerModel.currentTask()
            .firstOrError()
            .toMaybeValue()

        zip(currentTimeRecord, currentTask)
            .toObservable()
            .withLatestFrom(taskNameInputControl.text.observable)
            .flatMapMaybe { (currentTimeRecordAndTask, taskNameInput) ->
                val timeRecord = currentTimeRecordAndTask.first
                val task = currentTimeRecordAndTask.second

                when {
                    taskNameInput.isBlank() -> {
                        Maybe.error(TaskNameIsEmptyException())
                    }
                    task.name != taskNameInput -> {
                        timerModel.edit(timeRecord.id, taskNameInput)
                            .andThen(zip(currentTimeRecord, currentTask))
                    }
                    else -> {
                        Maybe.just(currentTimeRecordAndTask)
                    }
                }
            }
            .flatMapSingle { (currentTimeRecord, currentTask) ->
                timerModel.stop(currentTimeRecord.id)
                    .toSingleDefault(currentTimeRecord to currentTask)
            }
            .subscribe(
                { (savedTimeRecord, savedTask) ->
                    cancelableSavedTimeRecords.add(savedTimeRecord)
                    showTaskSavedMsg.consumer.accept(savedTask)
                }, { throwable ->
                    if (throwable is TaskNameIsEmptyException) {
                        shakeTaskName.consumer.accept(Unit)
                    }
                }
            )
            .untilDestroy()
    }
}