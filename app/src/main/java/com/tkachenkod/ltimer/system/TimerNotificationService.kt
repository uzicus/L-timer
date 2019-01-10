package com.tkachenkod.ltimer.system

import android.app.*
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.content.getSystemService
import com.tkachenkod.ltimer.R
import com.tkachenkod.ltimer.entity.TimeRecord
import com.tkachenkod.ltimer.extension.color
import com.tkachenkod.ltimer.extension.textBitmap
import com.tkachenkod.ltimer.model.TimerModel
import com.tkachenkod.ltimer.ui.MainActivity
import com.tkachenkod.ltimer.utils.RxBroadcastReceiver
import com.tkachenkod.ltimer.utils.toMaybeValue
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import org.koin.android.ext.android.inject

class TimerNotificationService : Service() {

    private val timerModel: TimerModel by inject()

    companion object {
        private const val ACTION_STOP_TIMER = "stop_timer"
        private const val TIMER_NOTIFICATION_ID = 100
        private const val TIMER_NOTIFICATION_CHANNEL_ID = "timer"
    }

    private val compositeDisposable = CompositeDisposable()

    private val notificationView: RemoteViews by lazy {
        RemoteViews(packageName, R.layout.layout_timer_notification).apply {
            val onStopClicksPendingIntent = PendingIntent.getBroadcast(
                this@TimerNotificationService,
                0,
                Intent(ACTION_STOP_TIMER),
                PendingIntent.FLAG_UPDATE_CURRENT
            )
            setOnClickPendingIntent(R.id.stopImageText, onStopClicksPendingIntent)
        }
    }

    private val notificationBuilder: NotificationCompat.Builder by lazy {
        val openActivityIntent =
            PendingIntent.getActivity(this, 0, Intent(this, MainActivity::class.java), 0)

        NotificationCompat.Builder(this, TIMER_NOTIFICATION_CHANNEL_ID).apply {
            setSmallIcon(R.drawable.ic_notification)
            setColorized(true)
            setCustomContentView(notificationView)
            setOnlyAlertOnce(true)
            setChannelId(TIMER_NOTIFICATION_CHANNEL_ID)
            setContentIntent(openActivityIntent)
            setUsesChronometer(true)
            color = color(R.color.colorPrimary)
        }
    }

    override fun onBind(intent: Intent?) = null

    override fun onCreate() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                TIMER_NOTIFICATION_CHANNEL_ID,
                resources.getString(R.string.notification_channel_timer_name),
                NotificationManager.IMPORTANCE_DEFAULT
            )

            notificationChannel.apply {
                setShowBadge(true)
                setSound(null, null)
                enableVibration(false)
            }

            getSystemService<NotificationManager>()?.createNotificationChannel(notificationChannel)
        }

        timerModel.currentTimeRecord()
            .doOnDispose {
                stopForeground(true)
            }
            .subscribe { optionalTimeRecord ->
                val timeRecord = optionalTimeRecord.valueOrNull

                if (timeRecord != null) {
                    startForeground(TIMER_NOTIFICATION_ID, createTimerNotification(timeRecord))
                } else {
                    stopForeground(true)
                }
            }
            .untilDestroy()

        RxBroadcastReceiver(this, IntentFilter(ACTION_STOP_TIMER))
            .flatMapMaybe {
                timerModel.currentTimeRecord()
                    .firstOrError()
                    .toMaybeValue()
            }
            .flatMapCompletable {
                sendBroadcast(Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS))
                timerModel.stop(it.id)
            }
            .subscribe()
            .untilDestroy()
    }

    override fun onDestroy() {
        compositeDisposable.dispose()
    }

    private fun createTimerNotification(timeRecord: TimeRecord): Notification {
        val stopText = resources.getString(R.string.timer_stop_title)
        val stopTextBitmap = textBitmap(
            stopText,
            android.R.color.white,
            R.dimen.timer_notification_stop_text_size,
            R.font.montserrat_bold
        )

        notificationView.setChronometer(R.id.timerChronometer, timeRecord.elapsedRealtime, null, true)
        notificationView.setImageViewBitmap(R.id.stopImageText, stopTextBitmap)

        return notificationBuilder.build()
    }

    private fun Disposable.untilDestroy() {
        compositeDisposable.add(this)
    }
}