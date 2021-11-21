package com.example.mobileattester.di

import androidx.lifecycle.ViewModelProvider
import com.example.mobileattester.data.network.AttestationDataHandler
import com.example.mobileattester.data.network.AttestationDataHandlerImpl
import com.example.mobileattester.data.repository.AttestationRepository
import com.example.mobileattester.data.repository.AttestationRepositoryImpl
import com.example.mobileattester.data.util.AttestUtil
import com.example.mobileattester.data.util.ElementDataHandler
import com.example.mobileattester.data.util.PolicyDataHandler
import com.example.mobileattester.data.util.RuleDataHandler
import com.example.mobileattester.ui.util.Preferences
import com.example.mobileattester.ui.viewmodel.AttestationViewModelImplFactory


object Injector {

    // Change to edit batch size
    private const val DEFAULT_BATCH_SIZE = Int.MAX_VALUE

    private val handler: AttestationDataHandler =
        AttestationDataHandlerImpl("http://192.168.16.83:8520/")
    private val attestationRepo: AttestationRepository = AttestationRepositoryImpl(handler)

    init {
        println("URL VALUE : ${handler.currentUrl.value}")

    }

    fun provideAttestationViewModelFactory(): ViewModelProvider.Factory {

        /*
            Here, Initialize the BatchedDataHandlers of different types.

            Link the repository methods to the corresponding handlers to get the
            data each of them are managing.
        */
        val elementDataHandler = ElementDataHandler(
            DEFAULT_BATCH_SIZE,
            { attestationRepo.getElementIds() },
            { attestationRepo.getElement(it) }
        )

        val policyDataHandler = PolicyDataHandler(
            DEFAULT_BATCH_SIZE,
            { attestationRepo.getPolicyIds() },
            { attestationRepo.getPolicy(it) }
        )

        val attestUtil = AttestUtil(
            handler,
            policyDataHandler
        )

        return AttestationViewModelImplFactory(attestationRepo, elementDataHandler, attestUtil)
    }
}