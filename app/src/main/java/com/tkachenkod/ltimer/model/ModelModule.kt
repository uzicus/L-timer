package com.tkachenkod.ltimer.model

import com.tkachenkod.ltimer.utils.ColorHelper
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module.Module
import org.koin.dsl.module.module

object ModelModule : Module by module(definition = {

    single { TimerModel(get(), get(), ColorHelper(androidContext())) }

})