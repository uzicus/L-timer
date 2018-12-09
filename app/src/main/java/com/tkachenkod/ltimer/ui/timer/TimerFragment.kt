package com.tkachenkod.ltimer.ui.timer

import android.os.Bundle
import android.text.InputType
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.view.inputmethod.EditorInfo
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.core.view.postDelayed
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.transition.TransitionManager
import com.tkachenkod.ltimer.R
import com.tkachenkod.ltimer.entity.Task
import com.tkachenkod.ltimer.extension.hideKeyboard
import com.tkachenkod.ltimer.extension.inflate
import com.tkachenkod.ltimer.extension.showKeyboard
import com.tkachenkod.ltimer.extension.textChangedListener
import com.tkachenkod.ltimer.ui.base.BackClickHandler
import com.tkachenkod.ltimer.ui.base.BaseFragment
import com.tkachenkod.ltimer.ui.base.adapter.BaseListAdapter
import com.tkachenkod.ltimer.ui.base.adapter.DiffItemsCallback
import com.tkachenkod.ltimer.ui.timer.TimerViewModel.ScreenState
import com.tkachenkod.ltimer.ui.timer.TimerViewModel.TaskNameState
import kotlinx.android.synthetic.main.fragment_timer.*
import kotlinx.android.synthetic.main.item_last_task.*
import org.koin.android.viewmodel.ext.android.viewModel

class TimerFragment : BaseFragment(), BackClickHandler {

    override val screenLayout: Int = R.layout.fragment_timer

    private val viewModel: TimerViewModel by viewModel()

    private val lastTasksAdapter = LastTasksAdapter()
    private val lastTasksDiffItemsCallback = LastTasksDiffItemsCallback()
    private val transitionHelper = TransitionHelper()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rootLayout.setTransitionListener(transitionHelper)

        with(lastTasksRecyclerView) {
            layoutManager = LinearLayoutManager(context)
            adapter = lastTasksAdapter
        }

        taskEdit.imeOptions = EditorInfo.IME_ACTION_DONE
        taskEdit.setRawInputType(InputType.TYPE_CLASS_TEXT)

        viewModel.taskName observe taskText::setText
        viewModel.taskNameInput observe {
            if (taskEdit.text.toString() != it) {
                taskEdit.setText(it)
                taskEdit.setSelection(it.length)
            }
        }

        viewModel.timerSeconds observe { seconds ->
            timerText.text = String.format(
                "%02d:%02d",
                seconds % 3600 / 60,
                seconds % 60
            )
        }

        viewModel.taskNameState observe { taskNameState ->
            TransitionManager.beginDelayedTransition(taskNameLayout)

            taskInput.isInvisible = taskNameState != TaskNameState.ENTERING
            taskText.isInvisible = taskNameState != TaskNameState.SHOWING

            if (taskNameState == TaskNameState.ENTERING) {
                taskEdit.showKeyboard()
            } else {
                taskEdit.hideKeyboard()
            }
        }

        viewModel.screenState observe { screenState ->
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

        viewModel.lastTasks observe {
            lastTasksTitleText.isVisible = it.isNotEmpty()
            lastTasksRecyclerView.isVisible = it.isNotEmpty()

            lastTasksAdapter.updateItems(it, lastTasksDiffItemsCallback)
        }

        viewModel.shakeTaskName observe {
            val animation = AnimationUtils.loadAnimation(taskInput.context, R.anim.shake)
            taskInput.startAnimation(animation)
        }

        button.setOnClickListener { viewModel.buttonClicks() }
        taskNameLayout.setOnClickListener { viewModel.taskNameClicks() }
        taskEdit.textChangedListener { viewModel.taskNameInputChange(it) }
        taskEdit.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                viewModel.taskNameActionDone()
            }
            true
        }
    }

    override fun onBackPressed(): Boolean {
        return viewModel.onBackPressed()
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
                itemView.setOnClickListener { viewModel.lastTasksItemClicks(item) }
            }

            override fun bind(item: Task) {
                lastTaskNameText.text = item.name
            }

        }
    }

    class LastTasksDiffItemsCallback : DiffItemsCallback<Task> {

        override fun areItemsTheSame(oldItem: Task, newItem: Task): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Task, newItem: Task): Boolean {
            return oldItem.name == newItem.name
        }

    }

}