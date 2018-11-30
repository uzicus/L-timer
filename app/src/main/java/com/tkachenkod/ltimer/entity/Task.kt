package com.tkachenkod.ltimer.entity

import androidx.room.Entity

@Entity(tableName = "tasks")
data class Task(
    val id: String,
    val name: String
)