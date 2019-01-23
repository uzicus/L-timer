package com.uzicus.ltimer.utils

import android.content.Context
import com.github.lzyzsd.randomcolor.RandomColor
import com.uzicus.ltimer.R

class ColorHelper(
    context: Context
) {

    private val materialColors = context.resources.getIntArray(R.array.material_colors).toList()
    private val randomColor = RandomColor()

    fun getColorByIndex(index: Int): Int {
        return materialColors.getOrElse(index) {
            randomColor.randomColor()
        }
    }

}