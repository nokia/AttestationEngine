package com.example.mobileattester.di

import androidx.lifecycle.ViewModelProvider
import com.example.mobileattester.data.network.AttestationDataHandlerImpl
import com.example.mobileattester.data.repository.AttestationRepositoryImpl
import com.example.mobileattester.data.util.ElementDataHandler
import com.example.mobileattester.ui.viewmodel.AttestationViewModelImplFactory

object Injector {

    /**
     * @param baseUrl url to use when initializing the service. Call method from viewModel to change this.
     */
    fun provideAttestationViewModelFactory(baseUrl: String): ViewModelProvider.Factory {
        val handler = AttestationDataHandlerImpl(baseUrl)
        val repo = AttestationRepositoryImpl(handler)
        val elementDataHandler = ElementDataHandler(repo, batchSize = 5)
        return AttestationViewModelImplFactory(repo, elementDataHandler)
    }
}