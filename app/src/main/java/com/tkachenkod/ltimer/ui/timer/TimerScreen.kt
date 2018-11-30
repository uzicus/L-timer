package com.tkachenkod.ltimer.ui.timer

import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import androidx.constraintlayout.motion.widget.MotionLayout
import com.tkachenkod.ltimer.ui.base.BaseScreen
import com.jakewharton.rxbinding3.view.clicks
import com.tkachenkod.ltimer.R
import com.tkachenkod.ltimer.extension.hideKeyboard
import com.tkachenkod.ltimer.extension.inject
import com.tkachenkod.ltimer.extension.showKeyboard
import kotlinx.android.synthetic.main.screen_timer.*

class TimerScreen : BaseScreen<TimerScreenPm>() {

    override val screenLayout = R.layout.screen_timer
    override val pm: TimerScreenPm by inject()

    private val transitionHelper = TransitionHelper()

    override fun onInitView(view: View, savedViewState: Bundle?) {
        super.onInitView(view, savedViewState)

        rootLayout.setTransitionListener(transitionHelper)
    }

    override fun onBindPresentationModel(view: View, pm: TimerScreenPm) {

        pm.taskName bindTo taskText::setText
        pm.taskNameInput bindTo taskInput
        pm.timer bindTo timerText::setText

        pm.currentState bindTo { screenState ->
            when (screenState) {
                TimerScreenPm.ScreenState.DASHBOARD -> {
                    if (rootLayout.currentState == R.id.timerSavingState) {
                        rootLayout.transitionToState(R.id.timerEmptyState)
                    } else {
                        rootLayout.transitionToState(R.id.timerReadyState)
                    }
                }
                TimerScreenPm.ScreenState.ENTERING_TASK -> {
                    rootLayout.transitionToState(R.id.timerEnteringState)
                    taskEdit.showKeyboard()
                }
                TimerScreenPm.ScreenState.RECORDING -> {
                    if (transitionHelper.pulseAnimation.not()) {
                        rootLayout.transitionToState(R.id.timerRecordingState)
                        taskEdit.hideKeyboard()
                    }
                    transitionHelper.pulseAnimation = true
                }
                TimerScreenPm.ScreenState.SAVING -> {
                    transitionHelper.pulseAnimation = false
                    rootLayout.setTransition(R.id.timerRecordingState, R.id.timerSavingState)
                    rootLayout.transitionToEnd()
                }
            }
        }

        pm.shakeTaskName bindTo {
            AnimationUtils.loadAnimation(taskInput.context, R.anim.shake).also { animation ->
                taskInput.startAnimation(animation)
            }
        }

        button.clicks() bindTo pm.buttonClicks
    }

    inner class TransitionHelper : MotionLayout.TransitionListener {

        var pulseAnimation = false

        override fun onTransitionChange(p0: MotionLayout?, p1: Int, p2: Int, p3: Float) {}

        override fun onTransitionCompleted(motionLayout: MotionLayout, stateId: Int) {
            if (pulseAnimation) {
                when (stateId) {
                    R.id.timerRecordingPulseState -> {
                        rootLayout.transitionToState(R.id.timerRecordingState)
                    }
                    else -> {
                        rootLayout.transitionToState(R.id.timerRecordingPulseState)
                    }
                }
            } else {
                when (stateId) {
                    R.id.timerSavingState -> {
                        Unit passTo presentationModel.savingAnimationEnd
                    }
                    R.id.timerEmptyState -> {
                        Unit passTo presentationModel.savingAnimationEnd
                    }
                }
            }
        }

    }
}