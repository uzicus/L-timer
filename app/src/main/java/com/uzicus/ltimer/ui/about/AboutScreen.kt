package com.uzicus.ltimer.ui.about

import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.View
import com.uzicus.ltimer.R
import com.uzicus.ltimer.ui.base.BaseScreen
import com.uzicus.ltimer.ui.base.BaseScreenPm
import kotlinx.android.synthetic.main.fragment_about.*

class AboutScreen: BaseScreen<BaseScreenPm>() {

    override val screenLayout = R.layout.fragment_about

    override fun providePresentationModel() = BaseScreenPm()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        aboutLinkText.movementMethod = LinkMovementMethod.getInstance()
    }
}