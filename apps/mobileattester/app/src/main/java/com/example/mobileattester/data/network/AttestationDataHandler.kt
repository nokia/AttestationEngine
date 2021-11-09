package com.example.mobileattester.data.network

import android.util.Log
import com.google.gson.GsonBuilder
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Middleman for REST-api service and X.
 * @see AttestationDataService
 */
interface AttestationDataHandler {
    /** Call to change the base url used for requests */
    fun setBaseUrl(url: String)

    suspend fun getElementIds(): List<String>
}

// ---------- Implementation ------------
// ---------- Implementation ------------

class AttestationDataHandlerImpl(
    private var baseUrl: String,
) : AttestationDataHandler {

    // TODO -----------  Move out of here -----------

    private lateinit var apiService: AttestationDataService

    init {
        buildService()
        Log.d("TEST", "Url: $baseUrl");
    }

    private fun buildService() {
        val gson = GsonBuilder().setLenient().create()

        apiService = Retrofit.Builder().baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create(gson)).build()
            .create(AttestationDataService::class.java)
    }

    // TODO ----------------------------------------

    override fun setBaseUrl(url: String) {
        baseUrl = url
        buildService()
    }

    override suspend fun getElementIds(): List<String> {
        return apiService.getElementIds()
    }
}