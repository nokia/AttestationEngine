package com.example.mobileattester.di

import androidx.lifecycle.ViewModelProvider
import com.example.mobileattester.data.network.AttestationDataHandlerImpl
import com.example.mobileattester.data.repository.AttestationRepositoryImpl
import com.example.mobileattester.ui.viewmodel.AttestationViewModelImplFactory

object Injector {

    fun provideAttestationViewModelFactory(): ViewModelProvider.Factory {
        val handler = AttestationDataHandlerImpl()
        val repo = AttestationRepositoryImpl(handler)
        return AttestationViewModelImplFactory(repo)
    }
}