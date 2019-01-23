package com.uzicus.ltimer

import android.app.Application
import com.jakewharton.threetenabp.AndroidThreeTen
import com.uzicus.ltimer.database.DatabaseModule
import com.uzicus.ltimer.model.ModelModule
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