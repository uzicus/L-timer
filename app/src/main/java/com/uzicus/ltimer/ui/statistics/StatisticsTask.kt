package com.uzicus.ltimer.ui.statistics

data class StatisticsTask(
    val name: String,
    val color: Int?,
    val durationInSecond: Long,
    val percent: Float
)