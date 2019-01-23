package com.uzicus.ltimer.ui.main

import android.os.Bundle
import android.view.MenuItem
import androidx.fragment.app.Fragment
import android.view.View
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.uzicus.ltimer.Debug
import com.uzicus.ltimer.R
import com.uzicus.ltimer.ui.about.AboutScreen
import com.uzicus.ltimer.ui.base.BackHandler
import com.uzicus.ltimer.ui.base.BaseScreenPm
import com.uzicus.ltimer.ui.base.BaseScreen
import com.uzicus.ltimer.ui.statistics.StatisticsScreen
import com.uzicus.ltimer.ui.timer.TimerScreen
import kotlinx.android.synthetic.main.fragment_main.*
import org.koin.android.ext.android.inject

class MainScreen : BaseScreen<BaseScreenPm>(), BackHandler {

    private val debug: Debug by inject()

    override fun providePresentationModel() = BaseScreenPm()

    override val screenLayout = R.layout.fragment_main

    private var prevMenuItem: MenuItem? = null

    private enum class Page {
        TIMER,
        STATISTICS,
        ABOUT;

        fun position() = Page.values().indexOf(this)
    }

    private val pagerAdapter: FragmentPagerAdapter by lazy {
        object : FragmentPagerAdapter(childFragmentManager) {

            override fun getItem(position: Int): Fragment {
                return when(Page.values()[position]) {
                    Page.TIMER -> TimerScreen()
                    Page.STATISTICS -> StatisticsScreen()
                    Page.ABOUT -> AboutScreen()
                }
            }

            override fun getCount() = Page.values().size


        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewPager.offscreenPageLimit = Page.values().size - 1
        viewPager.adapter = pagerAdapter

        bottomNavigation.setOnNavigationItemSelectedListener { item ->
            debug.onNavigationItemSelected(activity!!, item)
            when (item.itemId) {
                R.id.timer -> viewPager.currentItem = Page.TIMER.position()
                R.id.statistics -> viewPager.currentItem = Page.STATISTICS.position()
                R.id.about -> viewPager.currentItem = Page.ABOUT.position()
            }

            true
        }

        viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {}

            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {}

            override fun onPageSelected(position: Int) {
                prevMenuItem?.isChecked = false

                prevMenuItem = bottomNavigation.menu.getItem(position).apply {
                    isChecked = true
                }
            }

        })
    }

    override fun handleBack(): Boolean {
        val fragmentTag = "android:switcher:${viewPager.id}:${viewPager.currentItem}"
        val fragment = childFragmentManager.findFragmentByTag(fragmentTag)

        if (fragment != null && fragment is BackHandler) {
            return fragment.handleBack()
        }

        return true
    }

    override fun onBindPresentationModel(pm: BaseScreenPm) {

    }
}
