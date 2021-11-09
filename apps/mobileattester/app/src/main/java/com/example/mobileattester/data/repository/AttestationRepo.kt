package com.example.mobileattester.data.repository

import com.example.mobileattester.data.model.Element
import com.example.mobileattester.data.service.AttestationDataService

/**
 * Repository providing methods for Attestation stuff
 */
interface AttestationRepository {
    /** Call to change the url used */
    fun setBaseUrl(url: String)

    suspend fun getAllElements(): List<Element>
    suspend fun getElementsByTag(): List<Element>
}

class AttestationRepositoryImpl(
    private val service: AttestationDataService,
) : AttestationRepository {
    override fun setBaseUrl(url: String) = service.setBaseUrl(url)

    override suspend fun getAllElements(): List<Element> {
        return service.fetchAllElements()
    }

    override suspend fun getElementsByTag(): List<Element> {
        return service.fetchElementsByTags()
    }
}