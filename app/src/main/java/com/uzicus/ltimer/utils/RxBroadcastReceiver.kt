package com.uzicus.ltimer.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.disposables.Disposable
import java.util.concurrent.atomic.AtomicBoolean

class RxBroadcastReceiver(
    private val context: Context,
    private val intentFilter: IntentFilter
) : Observable<Intent>(), Disposable {

    private var receiver: BroadcastReceiver? = null
    private val disposed = AtomicBoolean(false)

    override fun subscribeActual(observer: Observer<in Intent>) {
        receiver = ReceiverDisposable(onReceive = { intent ->
            if (isDisposed.not()) {
                observer.onNext(intent)
            }
        })

        context.registerReceiver(receiver, intentFilter)

        observer.onSubscribe(this)
    }

    override fun isDisposed() = disposed.get()

    override fun dispose() {
        if (disposed.compareAndSet(false, true) && receiver != null) {
            context.unregisterReceiver(receiver)
        }
    }

    inner class ReceiverDisposable(val onReceive: (Intent) -> Unit): BroadcastReceiver() {

        override fun onReceive(context: Context?, intent: Intent) {
            onReceive.invoke(intent)
        }

    }

}