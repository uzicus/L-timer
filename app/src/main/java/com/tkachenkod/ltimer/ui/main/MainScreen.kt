package com.tkachenkod.ltimer.ui.main

import android.os.Bundle
import android.view.View
import com.bluelinelabs.conductor.Router
import com.bluelinelabs.conductor.support.RouterPagerAdapter
import com.tkachenkod.ltimer.ui.base.BaseScreen
import com.jakewharton.rxbinding3.material.itemSelections
import com.jakewharton.rxbinding3.viewpager.pageSelections
import com.tkachenkod.ltimer.R
import com.tkachenkod.ltimer.extension.inject
import com.tkachenkod.ltimer.extension.setRoot
import com.tkachenkod.ltimer.ui.about.AboutScreen
import com.tkachenkod.ltimer.ui.statistics.StatisticsScreen
import com.tkachenkod.ltimer.ui.timer.TimerScreen
import kotlinx.android.synthetic.main.layout_main.*

class MainScreen : BaseScreen<MainScreenPm>() {

    override val screenLayout = R.layout.layout_main
    override val pm: MainScreenPm by inject()

    private enum class Page {
        TIMER,
        STATISTICS,
        ABOUT;

        fun position() = Page.values().indexOf(this)
    }

    private val pagerAdapter = object : RouterPagerAdapter(this) {
        override fun configureRouter(router: Router, position: Int) {
            if (!router.hasRootController()) {
                router.setRoot(
                    when (Page.values()[position]) {
                        Page.TIMER -> TimerScreen()
                        Page.STATISTICS -> StatisticsScreen()
                        Page.ABOUT -> AboutScreen()
                    }
                )
            }
        }

        override fun getCount() = Page.values().size
    }

    override fun onInitView(view: View, savedViewState: Bundle?) {
        super.onInitView(view, savedViewState)

        viewPager.offscreenPageLimit = Page.values().size - 1
        viewPager.adapter = pagerAdapter

        bottomNavigation.itemSelections()
            .map {
                when (it.itemId) {
                    R.id.timer -> Page.TIMER.position()
                    R.id.statistics -> Page.STATISTICS.position()
                    R.id.about -> Page.ABOUT.position()
                    else -> throw IllegalArgumentException()
                }
            } bindTo viewPager::setCurrentItem

        viewPager.pageSelections()
            .map {
                when (Page.values()[it]) {
                    Page.TIMER -> R.id.timer
                    Page.STATISTICS -> R.id.statistics
                    Page.ABOUT -> R.id.about
                }
            } bindTo bottomNavigation::setSelectedItemId
    }
}