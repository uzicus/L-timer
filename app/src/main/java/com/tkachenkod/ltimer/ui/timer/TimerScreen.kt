package com.tkachenkod.ltimer.ui.timer

import android.os.Bundle
import android.text.InputType
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.view.inputmethod.EditorInfo
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.core.view.isInvisible
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
import com.tkachenkod.ltimer.ui.timer.TimerScreenPm.ScreenState
import com.tkachenkod.ltimer.ui.timer.TimerScreenPm.TaskNameState
import kotlinx.android.synthetic.main.fragment_timer.*
import kotlinx.android.synthetic.main.item_last_task.*

class TimerScreen : BaseScreen<TimerScreenPm>(), BackHandler {

    override fun providePresentationModel() = TimerScreenPm()

    override val screenLayout: Int = R.layout.fragment_timer

    private val lastTasksAdapter = LastTasksAdapter()

    private val pulseAnimation by lazy {
        AnimationUtils.loadAnimation(context, R.anim.pulse)
    }

    private val shakeAnimation by lazy {
        AnimationUtils.loadAnimation(context, R.anim.shake)
    }

    private val defaultTimerTextSize by lazy {
        resources.getDimensionPixelSize(R.dimen.timer_default_text_size)
    }
    private val scaleTimerTextSize by lazy {
        resources.getDimensionPixelSize(R.dimen.timer_scale_text_size)
    }

    private val isChangeScreenStateRunning: Boolean
        get() = rootLayout.progress != 0F
                && rootLayout.progress != 1F

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(lastTasksRecyclerView) {
            layoutManager = LinearLayoutManager(context)
            adapter = lastTasksAdapter
        }

        taskEdit.imeOptions = EditorInfo.IME_ACTION_DONE
        taskEdit.setRawInputType(InputType.TYPE_CLASS_TEXT)

        rootLayout.setTransitionListener(object : MotionLayout.TransitionListener {
            override fun onTransitionTrigger(p0: MotionLayout?, p1: Int, p2: Boolean, p3: Float) {}
            override fun onTransitionStarted(p0: MotionLayout?, p1: Int, p2: Int) {}
            override fun onTransitionChange(p0: MotionLayout?, p1: Int, p2: Int, p3: Float) {}

            override fun onTransitionCompleted(motionLayout: MotionLayout, stateId: Int) {
                if (stateId == R.id.timerEmptyState) {
                    rootLayout.transitionToState(R.id.timerReadyState)
                }
            }
        })

        timerChronometer.setOnChronometerTickListener { chronometer ->
            button.startAnimation(pulseAnimation)

            val textLength = chronometer.text.length

            chronometer.setTextSize(
                TypedValue.COMPLEX_UNIT_PX,
                (defaultTimerTextSize - ((textLength - 5) * scaleTimerTextSize)).toFloat()
            )
        }
    }

    override fun onBindPresentationModel(pm: TimerScreenPm) {
        pm.taskName bindTo taskText::setText
        pm.taskNameInputControl bindTo taskInput

        pm.timerChronometerBase bindTo {
            timerChronometer.stop()
            timerChronometer.base = it
            timerChronometer.start()
        }

        pm.taskNameState bindTo { taskNameState ->
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
            if (isChangeScreenStateRunning.not()) {
                when (screenState) {
                    ScreenState.DASHBOARD -> {
                        timerChronometer.stop()

                        if (rootLayout.currentState == R.id.timerRecordingState) {
                            rootLayout.transitionToState(R.id.timerEmptyState)
                        } else {
                            rootLayout.transitionToState(R.id.timerReadyState)
                        }
                    }
                    ScreenState.ENTERING_TASK -> {
                        rootLayout.transitionToState(R.id.timerEnteringState)
                    }
                    ScreenState.RECORDING -> {
                        rootLayout.transitionToState(R.id.timerRecordingState)
                    }
                }
            }
        }

        pm.lastTasks bindTo { lastTasks ->
            lastTasksTitleText.isInvisible = lastTasks.isEmpty()
            lastTasksRecyclerView.isInvisible = lastTasks.isEmpty()

            lastTasksAdapter.updateItems(lastTasks)

            lastTasksRecyclerView.post {
                lastTasksRecyclerView?.smoothScrollToPosition(0)
            }
        }

        pm.shakeTaskName bindTo {
            taskInput.startAnimation(shakeAnimation)
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

    inner class LastTasksAdapter : BaseListAdapter<Task, LastTasksAdapter.LastTaskViewHolder>() {

        override fun areItemsTheSame(oldItem: Task, newItem: Task): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Task, newItem: Task): Boolean {
            return oldItem.name == newItem.name && oldItem.color == newItem.color
        }

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

}