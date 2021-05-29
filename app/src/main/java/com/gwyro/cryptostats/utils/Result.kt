package com.gwyro.cryptostats.utils

sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Failed<T>(val throwable: Throwable) : Result<T>()
}