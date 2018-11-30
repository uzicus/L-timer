package com.tkachenkod.ltimer.extension

import org.koin.core.parameter.ParameterDefinition
import org.koin.core.parameter.emptyParameterDefinition
import org.koin.standalone.StandAloneContext

inline fun <reified T : Any> inject(
    name: String = "",
    noinline parameters: ParameterDefinition = emptyParameterDefinition()
): Lazy<T> = lazy {
    StandAloneContext.getKoin().koinContext.get<T>(name = name, parameters = parameters)
}