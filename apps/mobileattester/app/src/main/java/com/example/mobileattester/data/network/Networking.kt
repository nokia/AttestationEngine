package com.example.mobileattester.data.network

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

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

        fun <T> idle(data: T? = null): Response<T> = Response(status = Status.IDLE, data = data)
    }
}

class ResponseStateManager<T> {
    private val _res = MutableStateFlow(Response.idle<T>())
    val response: StateFlow<Response<T>> = _res

    fun setError(msg: String, data: T? = null) {
        _res.value = Response.error(message = msg, data = data)
    }

    fun setLoading(data: T? = null) {
        _res.value = Response.loading(data)
    }

    fun setSuccess(data: T) {
        _res.value = Response.success(data)
    }

    fun setIdle(data: T? = null) {
        _res.value = Response.idle(data)
    }
}

private const val TAG = "Networking"

/**
 * Retries a given block x times with increasing delay between retries. (5 by default)
 * @throws Exception If the block fails x times
 */
suspend fun <T> retryIO(
    times: Int = 5,
    initialDelay: Long = 100, // Delay after first fail
    maxDelay: Long = 1,
    factor: Double = 2.0,
    block: suspend () -> T,
): T? {
    var currentDelay = initialDelay
    var exc: Exception? = null

    repeat(times) {
        try {
            return@retryIO block()
        } catch (e: Exception) {
            delay(currentDelay)
            currentDelay = (currentDelay * factor).toLong().coerceAtMost(maxDelay)
            exc = e
        }
    }

    throw Exception("Operation $block failed $times. Cause: $exc")
}