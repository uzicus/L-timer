package com.uzicus.ltimer.utils

import java.text.NumberFormat

object Formatter {

    private val percentFormat = NumberFormat.getPercentInstance()

    fun percentFormat(percent: Float): String {
        return percentFormat.format(percent)
    }

    fun durationFormat(durationInSecond: Long): String {
        return when {
            durationInSecond < 60 -> String.format(
                "%ds",
                durationInSecond
            )
            durationInSecond < 3600 -> String.format(
                "%dm",
               durationInSecond % 3600 / 60
            )
            durationInSecond < 86400 -> String.format(
                "%dh %dm",
                durationInSecond / 3600,
                durationInSecond % 3600 / 60
            )
            else -> String.format(
                "%dd %dh %dm",
                durationInSecond / 86400,
                durationInSecond % 86400 / 3600,
                durationInSecond % 3600 / 60
            )
        }
    }
}