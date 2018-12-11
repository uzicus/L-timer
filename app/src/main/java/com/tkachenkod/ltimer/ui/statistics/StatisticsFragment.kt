package com.tkachenkod.ltimer.ui.statistics

import android.os.Bundle
import android.view.View
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.tkachenkod.ltimer.R
import com.tkachenkod.ltimer.ui.base.BaseFragment
import com.tkachenkod.ltimer.ui.statistics.StatisticsViewModel.Period
import kotlinx.android.synthetic.main.fragment_statistics.*
import org.koin.android.viewmodel.ext.android.viewModel

class StatisticsFragment : BaseFragment() {

    override val screenLayout = R.layout.fragment_statistics

    private val viewModel: StatisticsViewModel by viewModel()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        viewModel.tasks observe { tasks ->
            val data = PieData()

            pieChart.data = data
        }

        viewModel.period observe { period ->
            dayLabel.isSelected = period == Period.DAY
            weekLabel.isSelected = period == Period.WEEK
            monthLabel.isSelected = period == Period.MONTH
            yearLabel.isSelected = period == Period.YEAR
        }

        dayLabel.setOnClickListener {
            viewModel.dayLabelClicks()
        }

        weekLabel.setOnClickListener {
            viewModel.weekLabelClicks()
        }

        monthLabel.setOnClickListener {
            viewModel.monthLabelClicks()
        }

        yearLabel.setOnClickListener {
            viewModel.yearLabelClicks()
        }
    }

}