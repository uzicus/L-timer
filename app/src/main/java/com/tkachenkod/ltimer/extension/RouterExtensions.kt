@file:Suppress("NOTHING_TO_INLINE")

package com.tkachenkod.ltimer.extension

import com.bluelinelabs.conductor.Controller
import com.bluelinelabs.conductor.Router
import com.bluelinelabs.conductor.RouterTransaction

inline fun Router.back(): Boolean {
    return if (backstackSize > 1) {
        popCurrentController()
        true
    } else {
        false
    }
}

inline fun Router.setRoot(screen: Controller) {
    setRoot(
            RouterTransaction.with(screen)
                    .tag(screen::class.java.name)
    )
}

inline fun Router.goTo(screen: Controller) {
    pushController(
            RouterTransaction.with(screen)
                    .tag(screen::class.java.name)
    )
}

fun Router.currentScreen(): Controller? {
    return backstack[backstack.size - 1].controller()
}