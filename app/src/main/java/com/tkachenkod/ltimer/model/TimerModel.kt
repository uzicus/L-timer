package com.tkachenkod.ltimer.model

import com.tkachenkod.ltimer.entity.TimeRecord
import org.threeten.bp.Instant
import java.util.*

class TimerModel {

    var currentTimer: TimeRecord? = null

    fun start(taskName: String) {
        if (currentTimer == null) {
            currentTimer = TimeRecord(UUID.randomUUID().toString(), taskName, Instant.now())
        }
    }

    fun stop() {
        currentTimer = null
    }
}