package com.uzicus.ltimer

import androidx.room.Room
import com.uzicus.ltimer.database.AppDatabase
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module.Module
import org.koin.dsl.module.module

object DatabaseTestModule: Module by module(override = true, definition = {

    single {
        Room.inMemoryDatabaseBuilder(androidContext(), AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

})