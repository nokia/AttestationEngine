package com.example.mobileattester.data.network

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.example.mobileattester.data.model.Element
import com.example.mobileattester.data.model.ExpectedValue
import com.example.mobileattester.data.model.Policy
import com.example.mobileattester.ui.util.Preferences
import com.google.gson.GsonBuilder
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Middleman for REST-api service and X.
 * @see AttestationDataService
 */
interface AttestationDataHandler {
    /** Call to change the base url used for requests */
    var baseUrl : MutableLiveData<String>
    fun isDefaultBaseUrl() : Boolean

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

class AttestationDataHandlerImpl() : AttestationDataHandler {

    // TODO -----------  Move out of here -----------

    override var baseUrl : MutableLiveData<String> = MutableLiveData("http://${Preferences.defaultConfig.first()}/")
        set(value) {
            field = value
            buildService()
        }
    
    private lateinit var apiService: AttestationDataService

    init {
        buildService()
        Log.d("TEST", "Url: ${baseUrl.value}")
    }

    private fun buildService() {
        val gson = GsonBuilder().setLenient().create()

        apiService = Retrofit.Builder().baseUrl(baseUrl.value)
            .addConverterFactory(GsonConverterFactory.create(gson)).build()
            .create(AttestationDataService::class.java)
    }

    // TODO ----------------------------------------
    
    //override fun getBaseUrl(): String = baseUrl ?: "http://${Preferences.defaultConfig.first()}/"
    override fun isDefaultBaseUrl(): Boolean = baseUrl.value == Preferences.defaultConfig.first()

    override suspend fun getElementIds(): List<String> = apiService.getElementIds()
    override suspend fun getElement(itemid: String): Element = apiService.getElement(itemid)
    override suspend fun getAllTypes(): List<String> = apiService.getAllTypes()

    override suspend fun getPolicyIds(): List<String> = apiService.getPolicyIds()
    override suspend fun getPolicy(itemid: String): Policy = apiService.getPolicy(itemid)

    override suspend fun getExpectedValue(itemid: String): ExpectedValue =
        apiService.getExpectedValue(itemid)

    override suspend fun getExpectedValueByElementPolicyIds(
        eid: String,
        pid: String
    ): ExpectedValue = apiService.getExpectedValueByElementPolicyIds(eid, pid)
}