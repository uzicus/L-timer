package com.tkachenkod.ltimer.ui.timer

import android.os.Bundle
import android.text.InputType
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.view.inputmethod.EditorInfo
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.core.view.isInvisible
import androidx.core.view.postDelayed
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.transition.TransitionManager
import com.google.android.material.snackbar.Snackbar
import com.jakewharton.rxbinding3.view.clicks
import com.jakewharton.rxbinding3.widget.editorActions
import com.tkachenkod.ltimer.R
import com.tkachenkod.ltimer.entity.Task
import com.tkachenkod.ltimer.extension.*
import com.tkachenkod.ltimer.ui.base.BackHandler
import com.tkachenkod.ltimer.ui.base.BaseScreen
import com.tkachenkod.ltimer.ui.base.adapter.BaseListAdapter
import com.tkachenkod.ltimer.ui.base.adapter.DiffItemsCallback
import com.tkachenkod.ltimer.ui.timer.TimerScreenPm.ScreenState
import com.tkachenkod.ltimer.ui.timer.TimerScreenPm.TaskNameState
import kotlinx.android.synthetic.main.fragment_timer.*
import kotlinx.android.synthetic.main.item_last_task.*

class TimerScreen : BaseScreen<TimerScreenPm>(), BackHandler {

    override fun providePresentationModel() = TimerScreenPm()

    override val screenLayout: Int = R.layout.fragment_timer

    private val lastTasksAdapter = LastTasksAdapter()
    private val lastTasksDiffItemsCallback = LastTasksDiffItemsCallback()
    private val transitionHelper = TransitionHelper()

    override fun onBindPresentationModel(pm: TimerScreenPm) {
        pm.taskName bindTo taskText::setText
        pm.taskNameInputControl bindTo taskInput

        pm.timerSeconds bindTo { seconds ->
            timerText.text = String.format(
                "%02d:%02d",
                seconds % 3600 / 60,
                seconds % 60
            )
        }

        pm.taskNameState bindTo  { taskNameState ->
            TransitionManager.beginDelayedTransition(taskNameLayout)

            taskInput.isInvisible = taskNameState != TaskNameState.ENTERING
            taskText.isInvisible = taskNameState != TaskNameState.SHOWING

            if (taskNameState == TaskNameState.ENTERING) {
                taskEdit.showKeyboard()
            } else {
                taskEdit.hideKeyboard()
            }
        }

        pm.screenState bindTo { screenState ->
            when (screenState) {
                ScreenState.DASHBOARD -> {
                    if (transitionHelper.pulseAnimation) {
                        transitionHelper.pulseAnimation = false
                        rootLayout.setTransition(R.id.timerRecordingState, R.id.timerSavingState)
                        rootLayout.transitionToEnd()
                    } else if (rootLayout.currentState == R.id.timerSavingState) {
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
            }
        }

        pm.lastTasks bindTo { lastTasks ->
            lastTasksTitleText.isInvisible = lastTasks.isEmpty()
            lastTasksRecyclerView.isInvisible = lastTasks.isEmpty()

            lastTasksAdapter.updateItems(lastTasks, lastTasksDiffItemsCallback)
        }

        pm.shakeTaskName bindTo {
            val animation = AnimationUtils.loadAnimation(taskInput.context, R.anim.shake)
            taskInput.startAnimation(animation)
        }

        pm.showTaskSavedMsg bindTo { savedTask ->
            val title = resources.getString(R.string.timer_task_saved_msg_title)
                .spannable()
                .applyColor(resources.color(R.color.white_translucent_50))

            val taskName = savedTask.name
                .spannable()
                .applyColor(resources.color(android.R.color.white))

            val msg = resources.getString(R.string.timer_task_saved_msg_format)
                .format(title, taskName)

            Snackbar.make(rootLayout, msg, Snackbar.LENGTH_LONG)
                .setAction(R.string.timer_don_t_save_title) {
                    savedTask passTo pm.cancelSave
                }
                .onDismissed {
                    savedTask passTo pm.taskSavedMsgDismissed
                }
                .show()
        }

        button.clicks() bindTo pm.buttonClicks
        taskNameLayout.clicks() bindTo pm.taskNameClicks
        taskEdit.editorActions()
            .filter { it == EditorInfo.IME_ACTION_DONE }
            .map { Unit }
            .bindTo(pm.taskNameActionDone)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rootLayout.setTransitionListener(transitionHelper)

        with(lastTasksRecyclerView) {
            layoutManager = LinearLayoutManager(context)
            adapter = lastTasksAdapter
        }

        taskEdit.imeOptions = EditorInfo.IME_ACTION_DONE
        taskEdit.setRawInputType(InputType.TYPE_CLASS_TEXT)
    }

    inner class TransitionHelper : MotionLayout.TransitionListener {

        var pulseAnimation = false

        override fun onTransitionChange(
            motionLayout: MotionLayout,
            startId: Int,
            endId: Int,
            progress: Float
        ) {
        }

        override fun onTransitionCompleted(motionLayout: MotionLayout, stateId: Int) {
            // D>- onTransitionCompleted may not be called after the pulse animation
            rootLayout.postDelayed(50) {
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
                            rootLayout.transitionToState(R.id.timerEmptyState)
                        }
                        R.id.timerEmptyState -> {
                            rootLayout.transitionToState(R.id.timerReadyState)
                        }
                    }
                }
            }
        }

    }

    inner class LastTasksAdapter : BaseListAdapter<Task, LastTasksAdapter.LastTaskViewHolder>() {

        override fun newViewHolder(parent: ViewGroup, viewType: Int): LastTaskViewHolder {
            return LastTaskViewHolder(parent.inflate(R.layout.item_last_task))
        }

        inner class LastTaskViewHolder(itemView: View) : BaseViewHolder<Task>(itemView) {

            init {
                itemView.setOnClickListener { item passTo presentationModel.lastTasksItemClicks }
            }

            override fun bind(item: Task) {
                lastTaskNameText.text = item.name

                if (item.color != null) {
                    lastTaskColorView.background.mutate().setTint(item.color)
                    lastTaskNameText.setTextColor(item.color)
                }
            }

        }
    }

    class LastTasksDiffItemsCallback : DiffItemsCallback<Task> {

        override fun areItemsTheSame(oldItem: Task, newItem: Task): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Task, newItem: Task): Boolean {
            return oldItem.name == newItem.name && oldItem.color == newItem.color
        }

    }

}