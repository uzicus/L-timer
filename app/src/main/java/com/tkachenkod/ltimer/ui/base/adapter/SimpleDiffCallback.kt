package com.tkachenkod.ltimer.ui.base.adapter

class SimpleDiffCallback<in T> : DiffItemsCallback<T> {

    override fun areItemsTheSame(oldItem: T, newItem: T): Boolean {
        return if (oldItem is Identified<*> && newItem is Identified<*>) {
            oldItem.id == newItem.id
        } else {
            oldItem === newItem
        }
    }

    override fun areContentsTheSame(oldItem: T, newItem: T): Boolean {
        return oldItem == newItem
    }
}