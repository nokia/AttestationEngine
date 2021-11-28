package com.example.mobileattester.data.network

import android.util.Log
import kotlinx.coroutines.delay
import java.io.IOException

enum class Status {
    SUCCESS, ERROR, LOADING, IDLE
}


data class Response<out T>(val status: Status, val data: T? = null, val message: String? = null) {
    companion object {
        fun <T> success(data: T): Response<T> = Response(status = Status.SUCCESS, data = data)

        fun <T> error(data: T? = null, message: String): Response<T> =
            Response(status = Status.ERROR, data = data, message = message)

        fun <T> loading(data: T? = null): Response<T> =
            Response(status = Status.LOADING, data = data)

        fun <T> idle(): Response<T> =
            Response(status = Status.IDLE)
    }
}

/**
 * Retries a given block x times with increasing delay between retries. (5 by default)
 * @throws Exception If the block fails x times
 */
suspend fun <T> retryIO(
    times: Int = 5,
    initialDelay: Long = 100, // Delay after first fail
    maxDelay: Long = 1000,
    factor: Double = 2.0,
    block: suspend () -> T,
): T? {
    var currentDelay = initialDelay

    repeat(times) {
        try {
            return block()
        } finally {
            delay(currentDelay)
            currentDelay = (currentDelay * factor).toLong().coerceAtMost(maxDelay)
        }
    }

    throw Exception("Operation $block failed $times.")
}