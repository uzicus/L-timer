package com.tkachenkod.ltimer.ui.statistics

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isInvisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.mikephil.charting.data.PieDataSet
import com.jakewharton.rxbinding3.view.clicks
import com.tkachenkod.ltimer.R
import com.tkachenkod.ltimer.extension.inflate
import com.tkachenkod.ltimer.extension.updateData
import com.tkachenkod.ltimer.ui.base.BaseScreen
import com.tkachenkod.ltimer.ui.base.adapter.BaseListAdapter
import com.tkachenkod.ltimer.ui.statistics.StatisticsScreenPm.Period
import com.tkachenkod.ltimer.utils.Formatter
import kotlinx.android.synthetic.main.fragment_statistics.*
import kotlinx.android.synthetic.main.item_statistics_task.*

class StatisticsScreen : BaseScreen<StatisticsScreenPm>() {

    override val screenLayout = R.layout.fragment_statistics

    override fun providePresentationModel() = StatisticsScreenPm()

    private val tasksAdapter = TasksAdapter()

    private val animationDuration: Long by lazy {
        resources.getInteger(R.integer.statistics_chart_changes_animation_duration).toLong()
    }

    private val pieChartSliceSpace: Float by lazy {
        resources.getInteger(R.integer.statistics_chart_slice_space).toFloat()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        with(pieChart) {
            setDrawEntryLabels(false)
            setTouchEnabled(false)
            setDrawEntryLabels(false)
            setDrawCenterText(false)
            setDrawMarkers(false)
            legend.isEnabled = false
            description = null
            isDrawHoleEnabled = false
        }

        with(statisticsRecyclerView) {
            layoutManager = LinearLayoutManager(context)
            adapter = tasksAdapter
        }
    }

    override fun onBindPresentationModel(pm: StatisticsScreenPm) {

        pm.listTasks bindTo { tasks ->
            emptyLayout.isInvisible = tasks.isNotEmpty()
            contentLayout.isInvisible = tasks.isEmpty()

            tasksAdapter.updateItems(tasks)

            statisticsRecyclerView.post {
                statisticsRecyclerView?.smoothScrollToPosition(0)
            }
        }

        pm.chartTasks bindTo { tasks ->
            with (pieChart) {
                updateData(
                    newValues = tasks.map { it.durationInSecond.toFloat() },
                    animationDuration = animationDuration
                )

                data.dataSet.setDrawValues(false)
                (data.dataSet as PieDataSet).sliceSpace = pieChartSliceSpace
                (data.dataSet as PieDataSet).colors = tasks.map { it.color }

                invalidate()
            }
        }

        pm.period bindTo { period ->
            dayLabel.isSelected = period == Period.DAY
            weekLabel.isSelected = period == Period.WEEK
            monthLabel.isSelected = period == Period.MONTH
            yearLabel.isSelected = period == Period.YEAR
        }

        dayLabel.clicks().map { Period.DAY } bindTo pm.labelClicks
        weekLabel.clicks().map { Period.WEEK } bindTo pm.labelClicks
        monthLabel.clicks().map { Period.MONTH } bindTo pm.labelClicks
        yearLabel.clicks().map { Period.YEAR } bindTo pm.labelClicks
    }

    class TasksAdapter : BaseListAdapter<StatisticsTask, TasksAdapter.TaskViewHolder>() {

        override fun areItemsTheSame(
            oldItem: StatisticsTask,
            newItem: StatisticsTask
        ): Boolean {
            return oldItem.name == newItem.name
        }

        override fun areContentsTheSame(
            oldItem: StatisticsTask,
            newItem: StatisticsTask
        ): Boolean {
            return oldItem.durationInSecond == newItem.durationInSecond
                    && oldItem.percent == newItem.percent
                    && oldItem.color == newItem.color
        }

        override fun newViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
            return TaskViewHolder(parent.inflate(R.layout.item_statistics_task))
        }

        inner class TaskViewHolder(containerView: View) :
            BaseViewHolder<StatisticsTask>(containerView) {

            override fun bind(item: StatisticsTask) {

                statisticsTaskNameText.text = item.name
                statisticsTaskPercent.text = Formatter.percentFormat(item.percent)
                statisticsTaskDuration.text = Formatter.durationFormat(item.durationInSecond)

                if (item.color != null) {
                    statisticsTaskColorView.background.mutate().setTint(item.color)
                }
            }

        }
    }
}