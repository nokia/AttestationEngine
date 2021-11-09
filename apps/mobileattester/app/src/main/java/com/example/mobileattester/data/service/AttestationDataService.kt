package com.example.mobileattester.data.service

import com.example.mobileattester.data.model.Element
import retrofit2.http.GET

/**
 * Service communicating with a10REST.
 */
interface AttestationDataService {
    /** Change the url */
    fun setBaseUrl(url: String)

    @GET("elements")
    suspend fun fetchAllElements(): List<Element>
    suspend fun fetchElementsByTags(): List<Element>
}

class AttestationDataServiceImpl(
    private var baseUrl: String,
) : AttestationDataService {

    override fun setBaseUrl(url: String) {
        TODO("Not yet implemented")
    }

    override suspend fun fetchAllElements(): List<Element> {
        TODO("Not yet implemented")
    }

    override suspend fun fetchElementsByTags(): List<Element> {
        TODO("Not yet implemented")
    }
}