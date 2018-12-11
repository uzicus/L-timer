package com.tkachenkod.ltimer.ui.about

import com.tkachenkod.ltimer.R
import com.tkachenkod.ltimer.ui.base.BaseScreen
import com.tkachenkod.ltimer.ui.base.BaseScreenPm

class AboutScreen: BaseScreen<BaseScreenPm>() {

    override val screenLayout = R.layout.fragment_about

    override fun providePresentationModel() = BaseScreenPm()
}