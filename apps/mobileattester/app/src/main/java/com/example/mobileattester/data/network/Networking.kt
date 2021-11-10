package com.example.mobileattester.data.network

enum class Status {
    SUCCESS, ERROR, LOADING
}

data class Response<out T>(val status: Status, val data: T? = null, val message: String? = null) {
    companion object {
        fun <T> success(data: T): Response<T> = Response(status = Status.SUCCESS, data = data)

        fun <T> error(data: T?, message: String): Response<T> =
            Response(status = Status.ERROR, data = data, message = message)

        fun <T> loading(data: T?): Response<T> = Response(status = Status.LOADING, data = data)
    }
}