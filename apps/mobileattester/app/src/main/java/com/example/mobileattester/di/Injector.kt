package com.example.mobileattester.di

import androidx.lifecycle.ViewModelProvider
import com.example.mobileattester.data.network.AttestationDataHandlerImpl
import com.example.mobileattester.data.repository.AttestationRepositoryImpl
import com.example.mobileattester.ui.viewmodel.AttestationViewModelImplFactory

object Injector {

    fun provideAttestationViewModelFactory(baseUrl: String): ViewModelProvider.Factory {
        val handler = AttestationDataHandlerImpl(baseUrl)
        val repo = AttestationRepositoryImpl(handler)
        return AttestationViewModelImplFactory(repo)
    }
}