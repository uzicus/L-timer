package com.tkachenkod.ltimer.utils

sealed class Optional<out T> {

    val valueOrNull: T?
        get() = (this as? Some<T>)?.value

    val isEmpty: Boolean
        get() = this is EMPTY

    companion object {

        fun <T> ofNullable(value: T?): Optional<T> {
            return value?.let { Some(it) } ?: EMPTY
        }

    }

    class Some<out T>(val value: T): Optional<T>()

    object EMPTY: Optional<Nothing>()
}