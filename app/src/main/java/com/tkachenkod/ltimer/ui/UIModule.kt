package com.tkachenkod.ltimer.ui

import com.tkachenkod.ltimer.ui.timer.TimerViewModel
import org.koin.android.viewmodel.ext.koin.viewModel
import org.koin.dsl.module.Module
import org.koin.dsl.module.module

object UIModule: Module by module(definition = {

    viewModel { TimerViewModel(get()) }

})