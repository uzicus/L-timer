package com.tkachenkod.ltimer.system

import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import com.tkachenkod.ltimer.R
import com.tkachenkod.ltimer.extension.notificationManager
import com.tkachenkod.ltimer.extension.textBitmap
import com.tkachenkod.ltimer.model.TimerModel
import com.tkachenkod.ltimer.ui.MainActivity
import com.tkachenkod.ltimer.utils.Formatter
import com.tkachenkod.ltimer.utils.toMaybeValue
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import org.koin.android.ext.android.inject

class TimerNotificationService: Service() {

    private val timerModel: TimerModel by inject()

    companion object {

        private const val EXTRA_TIMER = "timer"
        private const val ACTION_STOP_TIMER = "stop_timer"
        private const val TIMER_NOTIFICATION_ID = 100
        private const val TIMER_NOTIFICATION_CHANNEL_ID = "timer"

        private var isRunning = false

        fun isRunning() = isRunning

        fun showTimerNotification(context: Context, timer: Long) {
            val intent = Intent(context, TimerNotificationService::class.java).apply {
                putExtra(EXTRA_TIMER, timer)
            }

            context.startService(intent)
        }
    }

    private val compositeDisposable = CompositeDisposable()

    private val notificationView: RemoteViews by lazy {
        val onStopClicksPendingIntent = PendingIntent.getBroadcast(this, 0, Intent(ACTION_STOP_TIMER), PendingIntent.FLAG_UPDATE_CURRENT)

        RemoteViews(packageName, R.layout.layout_timer_notification).apply {
            setOnClickPendingIntent(R.id.stopImageText, onStopClicksPendingIntent)
        }
    }

    private val notificationBuilder: NotificationCompat.Builder by lazy {
        val openActivityIntent = PendingIntent.getActivity(this, 0, Intent(this, MainActivity::class.java), 0)

        NotificationCompat.Builder(this, TIMER_NOTIFICATION_CHANNEL_ID).apply {
            setSmallIcon(R.drawable.ic_notification)
            setColorized(true)
            setCustomContentView(notificationView)
            setOnlyAlertOnce(true)
            setChannelId(TIMER_NOTIFICATION_CHANNEL_ID)
            setContentIntent(openActivityIntent)
        }
    }

    private val onStopClicksBroadcastReceiver = object : BroadcastReceiver() {

        override fun onReceive(context: Context?, intent: Intent?) {
            // collapse notifications drawer
            sendBroadcast(Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS))

            timerModel.currentTimeRecord()
                .firstOrError()
                .toMaybeValue()
                .flatMapCompletable {
                    timerModel.stop(it.id)
                }
                .subscribe()
                .untilDestroy()
        }

    }

    override fun onBind(intent: Intent?) = null

    override fun onCreate() {
        isRunning = false
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        isRunning = true
        registerReceiver(onStopClicksBroadcastReceiver, IntentFilter(ACTION_STOP_TIMER))

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                TIMER_NOTIFICATION_CHANNEL_ID,
                resources.getString(R.string.notification_channel_timer_name),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                setSound(null, null)
                enableVibration(false)
            }
            notificationManager.createNotificationChannel(notificationChannel)
        }

        val startTimer = intent?.getLongExtra(EXTRA_TIMER, 0) ?: 0
        startForeground(TIMER_NOTIFICATION_ID, createTimerNotification(startTimer))

        timerModel.currentTimerIntervalObservable()
            .subscribe { optionalTimeRecord ->
                if (optionalTimeRecord.isEmpty) {
                    stopForeground(true)
                    notificationManager.cancel(TIMER_NOTIFICATION_ID)
                } else {
                    optionalTimeRecord.valueOrNull?.also {
                        notificationManager.notify(
                            TIMER_NOTIFICATION_ID,
                            createTimerNotification(it.duration)
                        )
                    }
                }
            }
            .untilDestroy()

        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        compositeDisposable.dispose()
        notificationManager.cancel(TIMER_NOTIFICATION_ID)
        stopForeground(true)
        unregisterReceiver(onStopClicksBroadcastReceiver)
        isRunning = false
    }

    private fun createTimerNotification(timer: Long): Notification {
        val stopText = resources.getString(R.string.timer_stop_title)
        val stopTextBitmap = textBitmap(
            stopText,
            android.R.color.white,
            R.dimen.timer_notification_stop_text_size,
            R.font.montserrat_bold
        )

        val timerText = Formatter.timerFormat(timer)
        val timerTextBitmap = textBitmap(
            timerText,
            android.R.color.white,
            R.dimen.timer_notification_text_size,
            R.font.montserrat_bold
        )

        notificationView.setImageViewBitmap(R.id.timerImageText, timerTextBitmap)
        notificationView.setImageViewBitmap(R.id.stopImageText, stopTextBitmap)

        return notificationBuilder.build()
    }

    private fun Disposable.untilDestroy() {
        compositeDisposable.add(this)
    }
}