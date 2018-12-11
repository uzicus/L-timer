package com.tkachenkod.ltimer.ui.statistics

import com.tkachenkod.ltimer.R
import com.tkachenkod.ltimer.ui.base.BaseScreen
import com.tkachenkod.ltimer.ui.base.BaseScreenPm

class StatisticsFragment: BaseScreen<BaseScreenPm>() {

    override val screenLayout = R.layout.fragment_statistics

    override fun providePresentationModel() = BaseScreenPm()

}