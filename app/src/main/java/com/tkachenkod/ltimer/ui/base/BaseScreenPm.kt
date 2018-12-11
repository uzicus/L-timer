package com.tkachenkod.ltimer.ui.base

import com.tkachenkod.ltimer.ui.BackMessage
import me.dmdev.rxpm.PresentationModel
import me.dmdev.rxpm.navigation.NavigationMessage
import me.dmdev.rxpm.widget.dialogControl

open class BaseScreenPm: PresentationModel() {

    val errorDialog = dialogControl<String, Unit>()

    private val backActionDefault = Action<Unit>()

    open val backAction: Action<Unit> = backActionDefault

    override fun onCreate() {
        super.onCreate()

        backActionDefault.observable
            .subscribe { sendMessage(BackMessage()) }
            .untilDestroy()
    }

    protected fun sendMessage(message: NavigationMessage) {
        navigationMessages.consumer.accept(message)
    }

    protected fun showError(errorMessage: String?) {
        errorDialog.show(errorMessage ?: "Unknown error")
    }
}