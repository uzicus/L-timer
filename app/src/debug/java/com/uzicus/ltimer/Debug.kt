package com.uzicus.ltimer

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.os.SystemClock
import android.view.MenuItem
import android.widget.Toast
import com.uzicus.ltimer.database.dao.TaskDao
import com.uzicus.ltimer.database.dao.TimeRecordDao
import com.uzicus.ltimer.entity.Task
import com.uzicus.ltimer.entity.TimeRecord
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.toObservable
import io.reactivex.schedulers.Schedulers
import org.threeten.bp.OffsetDateTime
import org.threeten.bp.ZoneOffset
import timber.log.Timber
import kotlin.random.Random

@SuppressLint("CheckResult")
class Debug(
    private val taskDao: TaskDao,
    private val timeRecordDao: TimeRecordDao
) {

    companion object {
        private val simpleTasksName = listOf(
            "work",
            "games",
            "skateboard",
            "eating",
            "sleeping",
            "chatting"
        )
    }

    private var lastNavigationItemSelectedTime: Long = 0
    private var lastNavigationItemSelectedId: Int = 0
    private var countNavigationItemSelected: Int = 0

    fun onNavigationItemSelected(context: Context, item: MenuItem) {
        if (lastNavigationItemSelectedId == item.itemId
            && SystemClock.elapsedRealtime() - lastNavigationItemSelectedTime < 1000) {
            countNavigationItemSelected += 1

            if (countNavigationItemSelected == 3) {
                countNavigationItemSelected = 0
                openDebugView(context)
            }
        } else {
            countNavigationItemSelected = 0
        }

        lastNavigationItemSelectedTime = SystemClock.elapsedRealtime()
        lastNavigationItemSelectedId = item.itemId
    }

    private fun openDebugView(context: Context) {

        AlertDialog.Builder(context)
            .setItems(arrayOf("generate random tasks")) { dialog, _ ->
                generateRandomTask(context)
                dialog.dismiss()
            }
            .setNegativeButton("cancel", null)
            .create()
            .show()
    }

    private fun generateRandomTask(context: Context) {
        val taskName = simpleTasksName.random()

        taskDao.findByName(taskName)
            .map(Task::id)
            .switchIfEmpty(taskDao.insert(Task(name = taskName)))
            .flatMapObservable { taskId ->
                (0 until Random.nextInt(50, 1000)).map {
                    val start = OffsetDateTime.of(
                        2018,
                        Random.nextInt(1, 12),
                        Random.nextInt(1, 25),
                        Random.nextInt(0, 23),
                        Random.nextInt(0, 59),
                        0, 0, ZoneOffset.UTC
                    )
                    val end = start.plusMinutes(Random.nextLong(1, 150))
                    TimeRecord(
                        startTime = start,
                        endTime = end,
                        taskId = taskId
                    )
                }
                    .toObservable()
                    .flatMapSingle(timeRecordDao::insert)
            }
            .toList()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                {
                    Toast.makeText(context, "\"$taskName\" time records generate success", Toast.LENGTH_SHORT).show()
                },
                {
                    Toast.makeText(context, "\"$taskName\" time records generate fail", Toast.LENGTH_SHORT).show()
                    Timber.e(it)
                }
            )
    }
}