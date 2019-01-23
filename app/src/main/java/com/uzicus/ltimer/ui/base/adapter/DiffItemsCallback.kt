package com.uzicus.ltimer.ui.base.adapter

interface DiffItemsCallback<in T> {
    fun areItemsTheSame(oldItem: T, newItem: T): Boolean
    fun areContentsTheSame(oldItem: T, newItem: T): Boolean
}