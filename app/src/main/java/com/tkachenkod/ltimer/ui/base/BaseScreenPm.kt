package com.tkachenkod.ltimer.ui.base

import com.tkachenkod.ltimer.ui.BackMessage
import io.reactivex.functions.Consumer
import me.dmdev.rxpm.PresentationModel
import me.dmdev.rxpm.navigation.NavigationMessage

abstract class BaseScreenPm : PresentationModel() {

    open val backActionConsumer = Consumer<Unit> { sendMessage(BackMessage()) }

    protected fun sendMessage(message: NavigationMessage) {
        navigationMessages.consumer.accept(message)
    }
}