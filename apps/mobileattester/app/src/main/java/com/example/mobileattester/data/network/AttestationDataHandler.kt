package com.example.mobileattester.data.network

import android.util.Log
import com.example.mobileattester.data.model.*
import com.google.gson.GsonBuilder
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import kotlinx.coroutines.flow.MutableStateFlow
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import okhttp3.OkHttpClient

import okhttp3.logging.HttpLoggingInterceptor


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

    // --- Results ---
    suspend fun getResult(itemid: String): ElementResult
    suspend fun getElementResults(itemid: String, limit: Int): List<ElementResult>

    // --- Attestation ---
    suspend fun attestElement(eid: String, pid: String): String
    suspend fun verifyClaim(cid: String, rul: String): String

    // --- Rules ---
    suspend fun getRules(): List<Rule>

    // --- Claims ---
    suspend fun getClaim(itemid: String): Claim

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

        apiService = Retrofit.Builder().baseUrl(initialUrl).client(getOkHttpClient())
            .addConverterFactory(GsonConverterFactory.create(gson)).build()
            .create(AttestationDataService::class.java)
    }

    override fun rebuildService(withUrl: String) {
        println("REBUILDSERVICE CALLED: $withUrl")
        initialUrl = withUrl
        buildService()
        currentUrl.value = initialUrl
    }

    private fun getOkHttpClient(): OkHttpClient? {
        //Log display level
        val level = HttpLoggingInterceptor.Level.BODY
        //New log interceptor
        val loggingInterceptor = HttpLoggingInterceptor { message ->
            Log.d("RETROFIT",
                "OkHttp====Message:$message")
        }
        loggingInterceptor.level = level
        //Custom OKHTTP
        val httpClientBuilder = OkHttpClient.Builder()
        //OKHTTP to add interceptors loggingInterceptor
        httpClientBuilder.addInterceptor(loggingInterceptor)
        return httpClientBuilder.build()
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

    override suspend fun getResult(itemid: String): ElementResult = apiService.getResult(itemid)

    override suspend fun getElementResults(itemid: String, limit: Int): List<ElementResult> =
        apiService.getElementResults(itemid, limit)

    override suspend fun attestElement(eid: String, pid: String): String {
        val params = AttestationParams(
            eid = eid,
            pid = pid
        )
        return apiService.attestElement(params)
    }

    override suspend fun verifyClaim(cid: String, rul: String): String {
        val params = VerifyParams(
            cid,
            listOf(rul, JsonObject())
        )
        return apiService.verifyClaim(params)
    }

    override suspend fun getRules(): List<Rule> = apiService.getRules()
    override suspend fun getClaim(itemid: String): Claim = apiService.getClaim(itemid)

}