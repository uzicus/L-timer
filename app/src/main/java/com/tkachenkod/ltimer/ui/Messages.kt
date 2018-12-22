package com.tkachenkod.ltimer.ui

import me.dmdev.rxpm.navigation.NavigationMessage

class BackMessage: NavigationMessage

class ShowTimerNotificationMessage(val timer: Long): NavigationMessage