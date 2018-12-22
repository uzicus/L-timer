package com.tkachenkod.ltimer.utils

import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.Single

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

inline fun <T> Observable<Optional<T>>.toMaybeValue(): Observable<T> {
    return flatMapMaybe { optional ->
        optional.toMaybeValue()
    }
}

inline fun <T> Single<Optional<T>>.toMaybeValue(): Maybe<T> {
    return flatMapMaybe { optional ->
        optional.toMaybeValue()
    }
}

inline fun <T> Optional<T>.toMaybeValue(): Maybe<T> {
    return valueOrNull?.let { Maybe.just(it) } ?: Maybe.never()
}
