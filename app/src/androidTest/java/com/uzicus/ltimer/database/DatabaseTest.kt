package com.uzicus.ltimer.database

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.runner.AndroidJUnit4
import com.uzicus.ltimer.DatabaseTestModule
import com.uzicus.ltimer.database.dao.TaskDao
import com.uzicus.ltimer.database.dao.TimeRecordDao
import com.uzicus.ltimer.entity.Task
import com.uzicus.ltimer.entity.TimeRecord
import com.uzicus.ltimer.extension.inject
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.standalone.StandAloneContext.loadKoinModules
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