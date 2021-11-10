package com.example.mobileattester.data.repository

import com.example.mobileattester.data.network.AttestationDataHandler

/**
 * Repository providing methods for Attestation stuff
 */
interface AttestationRepository {
    /** Call to change the url used */
    fun setBaseUrl(url: String)

    suspend fun getElementIds(): List<String>
}

// TODO Caching stuff?
class AttestationRepositoryImpl(
    private val handler: AttestationDataHandler,
) : AttestationRepository {
    override fun setBaseUrl(url: String) = handler.setBaseUrl(url)

    override suspend fun getElementIds(): List<String> {
        return handler.getElementIds()
    }
}