package com.example.mobileattester.data.network

import retrofit2.http.GET

/**
 * Communication with REST
 */
interface AttestationDataService {

    @GET("elements")
    suspend fun getElementIds(): List<String>
}
