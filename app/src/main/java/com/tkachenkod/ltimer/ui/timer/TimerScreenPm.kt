package com.tkachenkod.ltimer.ui.timer

import com.tkachenkod.ltimer.extension.inject
import com.tkachenkod.ltimer.model.TimerModel
import com.tkachenkod.ltimer.ui.base.BaseScreenPm
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.withLatestFrom
import me.dmdev.rxpm.widget.inputControl
import org.threeten.bp.Instant
import java.util.concurrent.TimeUnit

class TimerScreenPm : BaseScreenPm() {

    private val timerModel: TimerModel by inject()

    private var timerDisposable: Disposable? = null

    enum class ScreenState {
        DASHBOARD,
        ENTERING_TASK,
        RECORDING,
        SAVING
    }

    val currentState = State(ScreenState.DASHBOARD)
    val taskName = State<String>()
    val taskNameInput = inputControl()
    val timer = State("00:00")

    val shakeTaskName = Command<Unit>()

    val buttonClicks = Action<Unit>()
    val savingAnimationEnd = Action<Unit>()

    override fun onCreate() {
        super.onCreate()

        taskNameInput.text.observable
            .subscribe(taskName.consumer)
            .untilDestroy()

        buttonClicks.observable
            .withLatestFrom(taskNameInput.text.observable, currentState.observable)
            .map { (_, taskName, state) ->
                when (state) {
                    ScreenState.DASHBOARD -> {
                        taskNameInput.text.consumer.accept("")
                        currentState.consumer.accept(ScreenState.ENTERING_TASK)
                    }
                    ScreenState.ENTERING_TASK -> {
                        if (taskName.isNotBlank()) {
                            currentState.consumer.accept(ScreenState.RECORDING)
                            startTimer()
                        } else {
                            shakeTaskName.consumer.accept(Unit)
                        }
                    }
                    ScreenState.RECORDING -> {
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

        savingAnimationEnd.observable
            .map { ScreenState.DASHBOARD }
            .subscribe(currentState.consumer)
            .untilDestroy()
    }

    private fun startTimer() {
        timerModel.start("123")
        timerDisposable = Observable.interval(0, 1, TimeUnit.SECONDS)
            .map {
                val value = timerModel.currentTimer?.let {
                    Instant.now().epochSecond - it.startTime.epochSecond
                } ?: 0

                String.format(
                    "%02d:%02d",
                    value % 3600 / 60,
                    value % 60
                )
            }
            .subscribe(timer.consumer)

        timerDisposable?.untilDestroy()
    }

    private fun stopTimer() {
        timerModel.stop()
        timerDisposable?.dispose()
    }
}