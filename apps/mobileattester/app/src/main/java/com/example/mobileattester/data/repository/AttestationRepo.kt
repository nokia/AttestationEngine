package com.example.mobileattester.data.repository

import androidx.lifecycle.MutableLiveData
import com.example.mobileattester.data.model.Element
import com.example.mobileattester.data.model.ExpectedValue
import com.example.mobileattester.data.model.Policy
import com.example.mobileattester.data.network.AttestationDataHandler

/**
 * Repository providing methods for Attestation stuff
 */
interface AttestationRepository {
    /** Call to change the url used */
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

// TODO Caching stuff
class AttestationRepositoryImpl(
    private val handler: AttestationDataHandler,
) : AttestationRepository {
    override var baseUrl = handler.baseUrl
    override fun isDefaultBaseUrl(): Boolean = handler.isDefaultBaseUrl()

    override suspend fun getElementIds(): List<String> {
        return handler.getElementIds()
    }

    override suspend fun getElement(itemid: String): Element {
        return handler.getElement(itemid)
    }

    override suspend fun getAllTypes(): List<String> {
        return handler.getAllTypes()
    }

    override suspend fun getPolicyIds(): List<String> {
        return handler.getPolicyIds()
    }

    override suspend fun getPolicy(itemid: String): Policy {
        return handler.getPolicy(itemid)
    }

    override suspend fun getExpectedValue(itemid: String): ExpectedValue {
        return handler.getExpectedValue(itemid)
    }

    override suspend fun getExpectedValueByElementPolicyIds(
        eid: String,
        pid: String
    ): ExpectedValue {
        return handler.getExpectedValueByElementPolicyIds(eid, pid)
    }
}