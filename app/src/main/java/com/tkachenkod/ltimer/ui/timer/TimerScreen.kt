package com.tkachenkod.ltimer.ui.timer

import android.os.Bundle
import android.text.InputType
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.view.inputmethod.EditorInfo
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.transition.TransitionManager
import com.tkachenkod.ltimer.ui.base.BaseScreen
import com.jakewharton.rxbinding3.view.clicks
import com.jakewharton.rxbinding3.widget.editorActions
import com.tkachenkod.ltimer.R
import com.tkachenkod.ltimer.entity.Task
import com.tkachenkod.ltimer.extension.hideKeyboard
import com.tkachenkod.ltimer.extension.inflate
import com.tkachenkod.ltimer.extension.inject
import com.tkachenkod.ltimer.extension.showKeyboard
import com.tkachenkod.ltimer.ui.base.adapter.BaseListAdapter
import com.tkachenkod.ltimer.ui.base.adapter.DiffItemsCallback
import com.tkachenkod.ltimer.ui.timer.TimerScreenPm.ScreenState
import com.tkachenkod.ltimer.ui.timer.TimerScreenPm.TaskNameState
import kotlinx.android.synthetic.main.item_last_task.*
import kotlinx.android.synthetic.main.screen_timer.*

class TimerScreen : BaseScreen<TimerScreenPm>() {

    override val screenLayout = R.layout.screen_timer
    override val pm: TimerScreenPm by inject()

    private val lastTasksAdapter = LastTasksAdapter()
    private val lastTasksDiffItemsCallback = LastTasksDiffItemsCallback()
    private val transitionHelper = TransitionHelper()

    override fun onInitView(view: View, savedViewState: Bundle?) {
        super.onInitView(view, savedViewState)

        rootLayout.setTransitionListener(transitionHelper)

        with(lastTasksRecyclerView) {
            layoutManager = LinearLayoutManager(context)
            adapter = lastTasksAdapter
        }

        taskEdit.imeOptions = EditorInfo.IME_ACTION_DONE
        taskEdit.setRawInputType(InputType.TYPE_CLASS_TEXT)
    }

    override fun onBindPresentationModel(view: View, pm: TimerScreenPm) {

        pm.taskName bindTo taskText::setText
        pm.taskNameInput bindTo taskInput

        pm.timerSeconds bindTo { seconds ->
            timerText.text = String.format(
                "%02d:%02d",
                seconds % 3600 / 60,
                seconds % 60
            )
        }

        pm.taskNameState bindTo { taskNameState ->
            TransitionManager.beginDelayedTransition(taskNameLayout)

            taskInput.isVisible = taskNameState == TaskNameState.ENTERING
            taskText.isVisible = taskNameState == TaskNameState.SHOWING

            if (taskNameState == TaskNameState.ENTERING) {
                taskEdit.showKeyboard()
            } else {
                taskEdit.hideKeyboard()
            }
        }

        pm.currentState bindTo { screenState ->

            when (screenState) {
                ScreenState.DASHBOARD -> {
                    if (rootLayout.currentState == R.id.timerSavingState) {
                        rootLayout.transitionToState(R.id.timerEmptyState)
                    } else {
                        rootLayout.transitionToState(R.id.timerReadyState)
                    }
                }
                ScreenState.ENTERING_TASK -> {
                    rootLayout.transitionToState(R.id.timerEnteringState)
                }
                ScreenState.RECORDING -> {
                    if (transitionHelper.pulseAnimation.not()) {
                        rootLayout.transitionToState(R.id.timerRecordingState)
                    }
                    transitionHelper.pulseAnimation = true
                }
                ScreenState.SAVING -> {
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

        pm.lastTasks bindTo {
            lastTasksTitleText.isVisible = it.isNotEmpty()
            lastTasksRecyclerView.isVisible = it.isNotEmpty()

            lastTasksAdapter.updateItems(it, lastTasksDiffItemsCallback)
        }

        button.clicks() bindTo pm.buttonClicks
        taskEdit.editorActions { it == EditorInfo.IME_ACTION_DONE }.map { Unit } bindTo pm.taskNameActionDone
        taskNameLayout.clicks() bindTo pm.taskNameClicks
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

    inner class LastTasksAdapter: BaseListAdapter<Task, LastTasksAdapter.LastTaskViewHolder>() {

        override fun newViewHolder(parent: ViewGroup, viewType: Int): LastTaskViewHolder {
            return LastTaskViewHolder(parent.inflate(R.layout.item_last_task))
        }

        inner class LastTaskViewHolder(itemView: View) : BaseViewHolder<Task>(itemView) {

            init {
                itemView.setOnClickListener { item passTo presentationModel.lastTasksItemClicks }
            }

            override fun bind(item: Task) {
                lastTaskNameText.text = item.name
            }

        }
    }

    class LastTasksDiffItemsCallback: DiffItemsCallback<Task> {

        override fun areItemsTheSame(oldItem: Task, newItem: Task): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Task, newItem: Task): Boolean {
            return oldItem.name == newItem.name
        }

    }
}