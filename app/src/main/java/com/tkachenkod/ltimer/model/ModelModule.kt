package com.tkachenkod.ltimer.model

import org.koin.dsl.module.Module
import org.koin.dsl.module.module

object ModelModule : Module by module(definition = {

    single { TimerModel() }

})