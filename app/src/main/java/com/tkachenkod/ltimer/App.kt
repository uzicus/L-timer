package com.tkachenkod.ltimer

import android.app.Application
import com.jakewharton.threetenabp.AndroidThreeTen
import com.tkachenkod.ltimer.database.DatabaseModule
import com.tkachenkod.ltimer.model.ModelModule
import org.koin.android.ext.android.startKoin
import timber.log.Timber

class App : Application() {

    override fun onCreate() {
        super.onCreate()

        AndroidThreeTen.init(this)

        startKoin(applicationContext, listOf(
            DebugModule,
            DatabaseModule,
            ModelModule
        ))

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }
}