package com.tkachenkod.ltimer

import com.tkachenkod.ltimer.ui.about.AboutScreenPm
import com.tkachenkod.ltimer.ui.main.MainScreenPm
import com.tkachenkod.ltimer.ui.statistics.StatisticsScreenPm
import com.tkachenkod.ltimer.ui.timer.TimerScreenPm
import org.koin.dsl.module.Module
import org.koin.dsl.module.module

object AppModule : Module by module(definition = {

    factory { MainScreenPm() }

    factory { TimerScreenPm() }

    factory { StatisticsScreenPm() }

    factory { AboutScreenPm() }

})