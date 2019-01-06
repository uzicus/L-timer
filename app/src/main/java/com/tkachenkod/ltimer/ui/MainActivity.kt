package com.tkachenkod.ltimer.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import com.tkachenkod.ltimer.R
import com.tkachenkod.ltimer.extension.serviceIsRunning
import com.tkachenkod.ltimer.system.TimerNotificationService
import com.tkachenkod.ltimer.ui.base.BackHandler
import kotlinx.android.synthetic.main.activity_main.*
import me.dmdev.rxpm.navigation.NavigationMessage
import me.dmdev.rxpm.navigation.NavigationMessageHandler

class MainActivity : AppCompatActivity(), NavigationMessageHandler {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onSupportNavigateUp(): Boolean {
        return findNavController(R.id.navHostFragment).navigateUp()
    }

    override fun onBackPressed() {
        val currentFragment = navHostFragment.childFragmentManager.primaryNavigationFragment

        if (currentFragment is BackHandler
            && currentFragment.handleBack().not()) {
            super.onBackPressed()
        }
    }

    override fun handleNavigationMessage(message: NavigationMessage): Boolean {

        when (message) {
            is BackMessage -> super.onBackPressed()

            is ShowTimerNotificationMessage -> {
                if (serviceIsRunning<TimerNotificationService>().not()) {
                    TimerNotificationService.showTimerNotification(this, message.timer)
                }
            }
        }

        return true
    }
}