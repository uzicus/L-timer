package com.tkachenkod.ltimer.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import com.tkachenkod.ltimer.R
import com.tkachenkod.ltimer.ui.base.BackClickHandler
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onSupportNavigateUp(): Boolean {
        return findNavController(R.id.navHostFragment).navigateUp()
    }

    override fun onBackPressed() {
        val currentFragment = navHostFragment.childFragmentManager.primaryNavigationFragment

        if (currentFragment is BackClickHandler
            && currentFragment.onBackPressed()) {
            super.onBackPressed()
        }
    }
}