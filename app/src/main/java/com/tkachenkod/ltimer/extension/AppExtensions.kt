package com.tkachenkod.ltimer.extension

import android.app.ActivityManager
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import androidx.core.content.getSystemService
import org.koin.core.parameter.ParameterDefinition
import org.koin.core.parameter.emptyParameterDefinition
import org.koin.standalone.StandAloneContext

inline fun <reified T : Any> inject(
    name: String = "",
    noinline parameters: ParameterDefinition = emptyParameterDefinition()
): Lazy<T> = lazy {
    StandAloneContext.getKoin().koinContext.get<T>(name = name, parameters = parameters)
}

inline val Context.notificationManager: NotificationManager
    get() = getSystemService() ?: throw IllegalArgumentException(/* TODO */)

inline val Context.activityManager: ActivityManager
    get() = getSystemService() ?: throw IllegalArgumentException(/* TODO */)

inline fun <reified T> Context.serviceIsRunning(): Boolean {
    return activityManager.getRunningServices(Int.MAX_VALUE)
        .any { it.service.className == T::class.java.name }
}

inline fun <T> Iterable<T>.sumByLong(selector: (T) -> Long): Long {
    var sum = 0L
    for (element in this) {
        sum += selector(element)
    }
    return sum
}
