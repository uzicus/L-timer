package com.tkachenkod.ltimer.database

import android.support.test.runner.AndroidJUnit4
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.tkachenkod.ltimer.DatabaseTestModule
import com.tkachenkod.ltimer.database.dao.TaskDao
import com.tkachenkod.ltimer.database.dao.TimeRecordDao
import com.tkachenkod.ltimer.entity.Task
import com.tkachenkod.ltimer.entity.TimeRecord
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.standalone.StandAloneContext.loadKoinModules
import org.koin.standalone.inject
import org.koin.test.KoinTest
import org.threeten.bp.OffsetDateTime

@RunWith(AndroidJUnit4::class)
class DatabaseTest: KoinTest {

    @Rule
    @JvmField
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val database: AppDatabase by inject()
    private val taskDao: TaskDao by inject()
    private val timeRecordDao: TimeRecordDao by inject()

    @Before
    fun before() {
        loadKoinModules(listOf(DatabaseTestModule))
    }

    @Test
    fun saveTaskTest() {
        val task = Task(name = "first task")

        taskDao.insert(task)
            .flatMap { createdTaskId ->
                val timeRecord = TimeRecord(
                    startTime = OffsetDateTime.now(),
                    taskId = createdTaskId
                )
                timeRecordDao.insert(timeRecord)
            }
            .subscribe()

        taskDao.taskWithTimeRecords()
            .test()
            .assertValue { tasksWithTimeRecords ->
                tasksWithTimeRecords.any {
                    it.task.name == task.name && it.timeRecords.isNotEmpty()
                }
            }
    }

    @After
    @Throws(Exception::class)
    fun closeDb() {
        database.close()
    }
}