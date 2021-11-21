package com.example.mobileattester.data.network

import android.util.Log
import kotlinx.coroutines.delay
import java.io.IOException

enum class Status {
    SUCCESS, ERROR, LOADING
}


data class Response<out T>(val status: Status, val data: T? = null, val message: String? = null) {
    companion object {
        fun <T> success(data: T): Response<T> = Response(status = Status.SUCCESS, data = data)

        fun <T> error(data: T? = null, message: String): Response<T> =
            Response(status = Status.ERROR, data = data, message = message)

        fun <T> loading(data: T? = null): Response<T> =
            Response(status = Status.LOADING, data = data)
    }
}

// TODO Fix
suspend fun <T> retryIO(
    times: Int = Int.MAX_VALUE,
    catchErrors: Boolean = true, // If we want to catch the error elsewhere
    initialDelay: Long = 100, // Delay after first fail
    maxDelay: Long = 1000,
    factor: Double = 2.0,
    block: suspend () -> T,
): T? {
    var currentDelay = initialDelay

    repeat(times) {
        try {
            return block()
        } catch (e: IOException) {
            if (!catchErrors) {
                // If we don't want to catch errors here
                throw e
            }
            Log.d("retryIO", "FAILED OPERATION $e")
        }
        delay(currentDelay)
        currentDelay = (currentDelay * factor).toLong().coerceAtMost(maxDelay)
    }

    return null
}