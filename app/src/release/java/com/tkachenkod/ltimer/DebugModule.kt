package com.uzicus.ltimer

import org.koin.dsl.module.Module
import org.koin.dsl.module.module

object DebugModule: Module by module(definition = {

    single { Debug() }

})