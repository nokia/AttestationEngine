package com.example.mobileattester.di

import com.example.mobileattester.data.repository.AttestationRepository
import com.example.mobileattester.data.repository.AttestationRepositoryImpl
import com.example.mobileattester.data.service.AttestationDataService
import com.example.mobileattester.data.service.AttestationDataServiceImpl

object Injector {

    fun provideAttestationRepository(baseUrl: String): AttestationRepository {
        val service: AttestationDataService = AttestationDataServiceImpl(baseUrl)
        return AttestationRepositoryImpl(service)
    }
}