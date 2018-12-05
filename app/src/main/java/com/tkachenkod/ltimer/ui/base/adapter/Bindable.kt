package com.tkachenkod.ltimer.ui.base.adapter

interface Bindable<in T> {
    fun bind(item: T)
}