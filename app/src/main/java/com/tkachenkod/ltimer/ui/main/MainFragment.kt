package com.tkachenkod.ltimer.ui.main

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import androidx.fragment.app.FragmentPagerAdapter
import com.tkachenkod.ltimer.R
import com.tkachenkod.ltimer.ui.about.AboutFragment
import com.tkachenkod.ltimer.ui.base.BackClickHandler
import com.tkachenkod.ltimer.ui.base.BaseFragment
import com.tkachenkod.ltimer.ui.statistics.StatisticsFragment
import com.tkachenkod.ltimer.ui.timer.TimerFragment
import kotlinx.android.synthetic.main.fragment_main.*

class MainFragment : BaseFragment(), BackClickHandler {

    override val screenLayout = R.layout.fragment_main

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
                    Page.TIMER -> TimerFragment()
                    Page.STATISTICS -> StatisticsFragment()
                    Page.ABOUT -> AboutFragment()
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
            when (item.itemId) {
                R.id.timer -> viewPager.currentItem = Page.TIMER.position()
                R.id.statistics -> viewPager.currentItem = Page.STATISTICS.position()
                R.id.about -> viewPager.currentItem = Page.ABOUT.position()
            }

            true
        }
    }

    override fun onBackPressed(): Boolean {
        val fragmentTag = "android:switcher:${viewPager.id}:${viewPager.currentItem}"
        val fragment = childFragmentManager.findFragmentByTag(fragmentTag)

        if (fragment != null && fragment is BackClickHandler) {
            return fragment.onBackPressed()
        }

        return true
    }
}
