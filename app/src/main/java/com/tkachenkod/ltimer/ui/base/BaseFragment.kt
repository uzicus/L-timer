package com.tkachenkod.ltimer.ui.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer

abstract class BaseFragment: Fragment() {

    abstract val screenLayout: Int

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(screenLayout, container, false)
    }

    infix fun <T> LiveData<T>.observe(observer: (T) -> Unit) {
        observe(this@BaseFragment, Observer(observer))
    }
}