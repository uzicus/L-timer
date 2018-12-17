package com.tkachenkod.ltimer.ui.statistics

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.jakewharton.rxbinding3.view.clicks
import com.tkachenkod.ltimer.R
import com.tkachenkod.ltimer.extension.inflate
import com.tkachenkod.ltimer.ui.base.BaseScreen
import com.tkachenkod.ltimer.ui.base.adapter.BaseListAdapter
import com.tkachenkod.ltimer.ui.base.adapter.DiffItemsCallback
import com.tkachenkod.ltimer.ui.statistics.StatisticsScreenPm.Period
import com.tkachenkod.ltimer.utils.Formatter
import kotlinx.android.synthetic.main.fragment_statistics.*
import kotlinx.android.synthetic.main.item_statistics_task.*

class StatisticsScreen : BaseScreen<StatisticsScreenPm>() {

    override val screenLayout = R.layout.fragment_statistics

    override fun providePresentationModel() = StatisticsScreenPm()

    private val tasksAdapter = TasksAdapter()
    private val tasksDiffItemsCallback = TasksDiffItemsCallback()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        with(pieChart) {
            setDrawEntryLabels(false)
            setDrawSlicesUnderHole(false)
            setTouchEnabled(false)
            setDrawEntryLabels(false)
            setDrawCenterText(false)
            setDrawMarkers(false)
            legend.isEnabled = false
            description = null
            holeRadius = 2f
            transparentCircleRadius = 0f
        }

        with(statisticsRecyclerView) {
            layoutManager = LinearLayoutManager(context)
            adapter = tasksAdapter
        }
    }

    override fun onBindPresentationModel(pm: StatisticsScreenPm) {

        pm.periodTasks bindTo { tasks ->
            tasksAdapter.updateItems(tasks, tasksDiffItemsCallback)

            val pieEntities = tasks.map {
                PieEntry(it.durationInSecond.toFloat())
            }

            val pieDataSet = PieDataSet(pieEntities, "").apply {
                colors = tasks.map(StatisticsTask::color)
                sliceSpace = 4f
            }

            pieChart.data = PieData(pieDataSet).apply {
                setDrawValues(false)
            }

            pieChart.invalidate()
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

    class TasksDiffItemsCallback : DiffItemsCallback<StatisticsTask> {
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

    }

}