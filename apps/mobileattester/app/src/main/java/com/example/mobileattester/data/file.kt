package com.example.mobileattester.data

import com.example.mobileattester.ui.util.Preferences
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

interface AttestationEngineApi {

    fun getRetrofit(): Retrofit {
        return Retrofit.Builder().baseUrl(Companion.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build() //Doesn't require the adapter
    }

    // Elements
    @GET("elements")
    suspend fun getElementIds(): List<String>

    companion object {
        private var BASE_URL = "https://${Preferences.currentEngine}/"
    }
}

enum class Status {
    SUCCESS, ERROR, LOADING
}

data class Response<out T>(val status: Status, val data: T?, val message: String?) {
    companion object {
        fun <T> success(data: T): Response<T> =
            Response(status = Status.SUCCESS, data = data, message = null)

        fun <T> error(data: T?, message: String): Response<T> =
            Response(status = Status.ERROR, data = data, message = message)

        fun <T> loading(data: T?): Response<T> =
            Response(status = Status.LOADING, data = data, message = null)
    }
}