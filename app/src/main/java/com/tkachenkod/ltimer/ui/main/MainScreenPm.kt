package com.tkachenkod.ltimer.ui.main

import com.tkachenkod.ltimer.ui.base.BaseScreenPm

class MainScreenPm : BaseScreenPm() {

    val currentPage = State<Int>()

    val pageSelected = Action<Int>()
}