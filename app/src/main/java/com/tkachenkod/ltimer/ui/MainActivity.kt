package com.tkachenkod.ltimer.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bluelinelabs.conductor.Conductor
import com.bluelinelabs.conductor.Router
import com.tkachenkod.ltimer.R
import com.tkachenkod.ltimer.extension.back
import com.tkachenkod.ltimer.extension.setRoot
import com.tkachenkod.ltimer.ui.main.MainScreen
import kotlinx.android.synthetic.main.layout_container.*
import me.dmdev.rxpm.navigation.NavigationMessage
import me.dmdev.rxpm.navigation.NavigationMessageHandler

class MainActivity : AppCompatActivity(), NavigationMessageHandler {

    private lateinit var router: Router

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_container)

        router = Conductor.attachRouter(this, container, savedInstanceState)

        if (router.hasRootController().not()) {
            handleNavigationMessage(StartUpMessage())
        }
    }

    override fun handleNavigationMessage(message: NavigationMessage): Boolean {
        when (message) {
            is BackMessage -> {
                if (router.back())
                else super.onBackPressed()
            }

            is StartUpMessage -> {
                router.setRoot(MainScreen())
            }
        }

        return true
    }
}