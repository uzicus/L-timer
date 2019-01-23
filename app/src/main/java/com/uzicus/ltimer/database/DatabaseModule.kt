package com.uzicus.ltimer.database

import androidx.room.Room
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module.Module
import org.koin.dsl.module.module

object DatabaseModule: Module by module(definition = {

    single {
        Room.databaseBuilder(androidContext(), AppDatabase::class.java, "app.db")
            .build()
    }

    single { get<AppDatabase>().taskDao() }

    single { get<AppDatabase>().timeRecordDao() }

})