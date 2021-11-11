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

        fun <T> error(data: T?, message: String): Response<T> =
            Response(status = Status.ERROR, data = data, message = message)

        fun <T> loading(data: T? = null): Response<T> =
            Response(status = Status.LOADING, data = data)
    }
}

suspend fun <T> retryIO(
    times: Int = Int.MAX_VALUE,
    initialDelay: Long = 100,
    maxDelay: Long = 1000,
    factor: Double = 2.0,
    block: suspend () -> T,
): T {
    var currentDelay = initialDelay

    repeat(times - 1) {
        try {
            return block()
        } catch (e: IOException) {
            Log.d("retryIO", "FAILED OPERATION $e")
            // you can log an error here and/or make a more finer-grained
            // analysis of the cause to see if retry is needed
        }
        delay(currentDelay)
        currentDelay = (currentDelay * factor).toLong().coerceAtMost(maxDelay)
    }
    return block()
}