package com.example.mobileattester.data.network

import com.example.mobileattester.data.model.Element
import com.example.mobileattester.data.model.ExpectedValue
import com.example.mobileattester.data.model.Policy
import com.google.gson.GsonBuilder
import kotlinx.coroutines.flow.MutableStateFlow
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Middleman for REST-api service and X.
 * @see AttestationDataService
 */
interface AttestationDataHandler {

    // --- URL ---
    /** Base url currently used for api calls */
    val currentUrl: MutableStateFlow<String>

    /** Call to rebuild the api service with a new url */
    fun rebuildService(withUrl: String)

    // --- Elements ---
    suspend fun getElementIds(): List<String>
    suspend fun getElement(itemid: String): Element
    suspend fun getAllTypes(): List<String>

    // --- Policies ---
    suspend fun getPolicyIds(): List<String>
    suspend fun getPolicy(itemid: String): Policy

    // --- Expected values ---
    suspend fun getExpectedValue(itemid: String): ExpectedValue
    suspend fun getExpectedValueByElementPolicyIds(eid: String, pid: String): ExpectedValue
}

// ---------- Implementation ------------
// ---------- Implementation ------------

class AttestationDataHandlerImpl(
    private var initialUrl: String,
) : AttestationDataHandler {

    // TODO -----------  Move out of here -----------

    override val currentUrl: MutableStateFlow<String> = MutableStateFlow(initialUrl)
    private lateinit var apiService: AttestationDataService

    init {
        buildService()
    }

    private fun buildService() {
        val gson = GsonBuilder().setLenient().create()

        apiService = Retrofit.Builder().baseUrl(initialUrl)
            .addConverterFactory(GsonConverterFactory.create(gson)).build()
            .create(AttestationDataService::class.java)
    }

    override fun rebuildService(withUrl: String) {
        println("REBUILDSERVICE CALLED: $withUrl")
        initialUrl = withUrl
        buildService()
        currentUrl.value = initialUrl
    }

    // TODO ----------------------------------------

    override suspend fun getElementIds(): List<String> = apiService.getElementIds()

    override suspend fun getElement(itemid: String): Element = apiService.getElement(itemid)
    override suspend fun getAllTypes(): List<String> = apiService.getAllTypes()

    override suspend fun getPolicyIds(): List<String> = apiService.getPolicyIds()
    override suspend fun getPolicy(itemid: String): Policy = apiService.getPolicy(itemid)

    override suspend fun getExpectedValue(itemid: String): ExpectedValue =
        apiService.getExpectedValue(itemid)

    override suspend fun getExpectedValueByElementPolicyIds(
        eid: String,
        pid: String,
    ): ExpectedValue = apiService.getExpectedValueByElementPolicyIds(eid, pid)
}