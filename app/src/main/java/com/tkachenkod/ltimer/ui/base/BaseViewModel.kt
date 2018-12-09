package com.tkachenkod.ltimer.ui.base

import androidx.lifecycle.ViewModel
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

abstract class BaseViewModel: ViewModel() {

    protected val disposable = CompositeDisposable()

    protected fun Disposable.untilDestroy() {
        disposable.add(this)
    }

    override fun onCleared() {
        super.onCleared()
        disposable.dispose()
    }
}