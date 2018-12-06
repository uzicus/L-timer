package com.tkachenkod.ltimer.ui.timer

import com.tkachenkod.ltimer.entity.Task
import com.tkachenkod.ltimer.entity.TimeRecord
import com.tkachenkod.ltimer.extension.inject
import com.tkachenkod.ltimer.model.TimerModel
import com.tkachenkod.ltimer.ui.BackMessage
import com.tkachenkod.ltimer.ui.base.BaseScreenPm
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.rxkotlin.withLatestFrom
import me.dmdev.rxpm.widget.inputControl
import org.threeten.bp.Instant
import java.util.concurrent.TimeUnit

class TimerScreenPm : BaseScreenPm() {

    private val timerModel: TimerModel by inject()

    enum class TaskNameState {
        ENTERING,
        SHOWING,
        NOTHING
    }

    enum class ScreenState {
        DASHBOARD,
        ENTERING_TASK,
        RECORDING,
        SAVING
    }

    private val backAction = Action<Unit>()
    override val backActionConsumer = backAction.consumer

    val lastTasks = State(emptyList<Task>())
    val currentState = State<ScreenState>()
    val taskName = State<String>()
    val taskNameInput = inputControl()
    val timerSeconds = State<Long>(0)
    val taskNameState = State(TaskNameState.NOTHING)

    val shakeTaskName = Command<Unit>()

    val buttonClicks = Action<Unit>()
    val savingAnimationEnd = Action<Unit>()
    val taskNameClicks = Action<Unit>()
    val taskNameActionDone = Action<Unit>()
    val lastTasksItemClicks = Action<Task>()

    override fun onCreate() {
        super.onCreate()

        timerModel.currentTimeRecord()
            .firstOrError()
            .subscribe { currentTimeRecord ->
                if (currentTimeRecord.isEmpty.not()) {
                    taskNameState.consumer.accept(TaskNameState.SHOWING)
                    currentState.consumer.accept(ScreenState.RECORDING)
                } else {
                    taskNameState.consumer.accept(TaskNameState.NOTHING)
                    currentState.consumer.accept(ScreenState.DASHBOARD)
                }

                timerSeconds.consumer.accept(currentTimeRecord.valueOrNull.getTimerSeconds())
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

        Observable.interval(0, 1, TimeUnit.SECONDS)
            .withLatestFrom(timerModel.currentTimeRecord())
            .map { (_, currentTimeRecord) ->
                currentTimeRecord.valueOrNull.getTimerSeconds()
            }
            .subscribe(timerSeconds.consumer)
            .untilDestroy()

        buttonClicks.observable
            .withLatestFrom(
                taskNameInput.text.observable.map(String::trim),
                currentState.observable
            )
            .map { (_, taskName, state) ->
                when (state) {
                    ScreenState.DASHBOARD -> {
                        taskNameInput.text.consumer.accept("")
                        taskNameState.consumer.accept(TaskNameState.ENTERING)
                        currentState.consumer.accept(ScreenState.ENTERING_TASK)
                    }
                    ScreenState.ENTERING_TASK -> {
                        if (taskName.isNotBlank()) {
                            currentState.consumer.accept(ScreenState.RECORDING)
                            taskNameState.consumer.accept(TaskNameState.SHOWING)
                            startTimer(taskName.trim())
                        } else {
                            shakeTaskName.consumer.accept(Unit)
                        }
                    }
                    ScreenState.RECORDING -> {
                        taskNameState.consumer.accept(TaskNameState.NOTHING)
                        currentState.consumer.accept(ScreenState.SAVING)
                        stopTimer()
                    }
                    else -> {
                        currentState.consumer.accept(ScreenState.DASHBOARD)
                    }
                }
            }
            .subscribe()
            .untilDestroy()

        taskNameClicks.observable
            .withLatestFrom(taskNameState.observable, taskName.observable)
            .subscribe { (_, currentTaskNameState, taskName) ->
                if (currentTaskNameState == TaskNameState.SHOWING) {
                    taskNameInput.text.consumer.accept(taskName)
                    taskNameState.consumer.accept(TaskNameState.ENTERING)
                }
            }
            .untilDestroy()

        taskNameActionDone.observable
            .withLatestFrom(
                currentState.observable,
                timerModel.currentTimeRecord(),
                taskNameInput.text.observable.map(String::trim)
            ) { _, state, timeRecordOptional, taskName ->
                val timeRecord = timeRecordOptional.valueOrNull

                if (state == ScreenState.ENTERING_TASK) {
                    buttonClicks.consumer.accept(Unit)
                } else if (state == ScreenState.RECORDING && timeRecord != null) {
                    if (taskName.isNotBlank()) {
                        timerModel.edit(timeRecord.id, taskName)
                            .subscribe {
                                taskNameState.consumer.accept(TaskNameState.SHOWING)
                            }
                            .untilDestroy()
                    } else {
                        shakeTaskName.consumer.accept(Unit)
                    }
                }
            }
            .subscribe()
            .untilDestroy()

        savingAnimationEnd.observable
            .map { ScreenState.DASHBOARD }
            .subscribe(currentState.consumer)
            .untilDestroy()

        backAction.observable
            .withLatestFrom(currentState.observable, taskNameState.observable)
            .subscribe { (_, state, currentTaskNameState) ->
                when {
                    state == ScreenState.ENTERING_TASK -> {
                        currentState.consumer.accept(ScreenState.DASHBOARD)
                    }
                    currentTaskNameState == TaskNameState.ENTERING -> {
                        taskNameState.consumer.accept(TaskNameState.SHOWING)
                    }
                    else -> sendMessage(BackMessage())
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

    private fun TimeRecord?.getTimerSeconds(): Long {
        return if (this != null) {
            Instant.now().epochSecond - startTime.toEpochSecond()
        } else {
            0
        }
    }
}