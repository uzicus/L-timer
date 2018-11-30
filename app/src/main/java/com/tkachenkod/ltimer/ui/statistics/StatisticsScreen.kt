package com.tkachenkod.ltimer.ui.statistics

import com.tkachenkod.ltimer.ui.base.BaseScreen
import com.tkachenkod.ltimer.R
import com.tkachenkod.ltimer.extension.inject

class StatisticsScreen : BaseScreen<StatisticsScreenPm>() {

    override val screenLayout: Int = R.layout.screen_statistics
    override val pm: StatisticsScreenPm by inject()

}