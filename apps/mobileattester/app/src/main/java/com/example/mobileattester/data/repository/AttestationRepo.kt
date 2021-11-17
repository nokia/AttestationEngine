package com.example.mobileattester.data.repository

import com.example.mobileattester.data.model.Element
import com.example.mobileattester.data.model.ExpectedValue
import com.example.mobileattester.data.model.Policy
import com.example.mobileattester.data.network.AttestationDataHandler
import com.example.mobileattester.data.util.BatchedDataProvider
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * Repository providing methods for Attestation stuff
 */
interface AttestationRepository {

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

class AttestationRepositoryImpl(
    private val handler: AttestationDataHandler,
) : AttestationRepository, BatchedDataProvider<String,Element> {

    override val currentUrl: MutableStateFlow<String> = handler.currentUrl

    override fun rebuildService(withUrl: String) = handler.rebuildService(withUrl)

    override suspend fun getElementIds(): List<String> = handler.getElementIds()
    override suspend fun getElement(itemid: String): Element = handler.getElement(itemid)
    override suspend fun getAllTypes(): List<String> = handler.getAllTypes()

    override suspend fun getPolicyIds(): List<String> = handler.getPolicyIds()
    override suspend fun getPolicy(itemid: String): Policy = handler.getPolicy(itemid)

    override suspend fun getExpectedValue(itemid: String): ExpectedValue =
        handler.getExpectedValue(itemid)

    override suspend fun getExpectedValueByElementPolicyIds(
        eid: String,
        pid: String,
    ): ExpectedValue = handler.getExpectedValueByElementPolicyIds(eid, pid)

    override suspend fun getIdList(): List<String> = getElementIds()
    override suspend fun getDataForId(id: String): Element = getElement(id)
}