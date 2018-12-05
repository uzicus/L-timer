package com.tkachenkod.ltimer.ui.about

import com.tkachenkod.ltimer.ui.base.BaseScreen
import com.tkachenkod.ltimer.R
import com.tkachenkod.ltimer.extension.inject

class AboutScreen : BaseScreen<AboutScreenPm>() {


    override val screenLayout = R.layout.screen_about
    override val pm: AboutScreenPm by inject()
}