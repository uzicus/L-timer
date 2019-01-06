package com.tkachenkod.ltimer.system

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import com.tkachenkod.ltimer.R
import com.tkachenkod.ltimer.extension.color
import com.tkachenkod.ltimer.extension.notificationManager
import com.tkachenkod.ltimer.extension.textBitmap
import com.tkachenkod.ltimer.model.TimerModel
import com.tkachenkod.ltimer.ui.MainActivity
import com.tkachenkod.ltimer.utils.Formatter
import com.tkachenkod.ltimer.utils.RxBroadcastReceiver
import com.tkachenkod.ltimer.utils.toMaybeValue
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.Observables.combineLatest
import org.koin.android.ext.android.inject
import timber.log.Timber

class TimerNotificationService : Service() {

    private val timerModel: TimerModel by inject()

    companion object {

        private const val EXTRA_TIMER = "timer"
        private const val ACTION_STOP_TIMER = "stop_timer"
        private const val TIMER_NOTIFICATION_ID = 100
        private const val TIMER_NOTIFICATION_CHANNEL_ID = "timer"

        fun showTimerNotification(context: Context, timer: Long) {
            val intent = Intent(context, TimerNotificationService::class.java).apply {
                putExtra(EXTRA_TIMER, timer)
            }

            context.startService(intent)
        }
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
            setTimeoutAfter(1000)
            color = color(R.color.colorPrimary)
        }
    }

    override fun onBind(intent: Intent?) = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
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

            notificationManager.createNotificationChannel(notificationChannel)
        }

        val screenEnabledObservable = RxBroadcastReceiver(this, IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_ON)
            addAction(Intent.ACTION_SCREEN_OFF)
        })
            .map { receiverIntent ->
                receiverIntent.action == Intent.ACTION_SCREEN_ON
            }
            .startWith(true)

        combineLatest(
            timerModel.currentTimerIntervalObservable(),
            screenEnabledObservable
        )
            .subscribe { (optionalTimeRecord, isScreenEnabled) ->
                val timeRecord = optionalTimeRecord.valueOrNull

                if (timeRecord != null) {
                    if (isScreenEnabled) {
                        val notification = createTimerNotification(timeRecord.duration)
                        startForeground(TIMER_NOTIFICATION_ID, notification)
                        notificationManager.notify(TIMER_NOTIFICATION_ID, notification)
                        Timber.d("notify timer: ${timeRecord.duration}")
                    }
                } else {
                    stopForeground(true)
                    notificationManager.cancel(TIMER_NOTIFICATION_ID)
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

        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        compositeDisposable.dispose()
        notificationManager.cancel(TIMER_NOTIFICATION_ID)
        stopForeground(true)
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

        return notificationBuilder.apply {
            setWhen(System.currentTimeMillis())
        }.build()
    }

    private fun Disposable.untilDestroy() {
        compositeDisposable.add(this)
    }
}