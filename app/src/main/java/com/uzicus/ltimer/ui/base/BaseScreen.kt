package com.uzicus.ltimer.ui.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import me.dmdev.rxpm.base.PmSupportFragment

abstract class BaseScreen<PM : BaseScreenPm> :
    PmSupportFragment<PM>(),
    BackHandler {

    abstract val screenLayout: Int

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(screenLayout, container, false)
    }

    override fun handleBack(): Boolean {
        presentationModel.backAction.consumer.accept(Unit)
        return true
    }

    override fun onBindPresentationModel(pm: PM) {}
}