package com.example.mobileattester.di

import androidx.lifecycle.ViewModelProvider
import com.example.mobileattester.data.network.AttestationDataHandler
import com.example.mobileattester.data.network.AttestationDataHandlerImpl
import com.example.mobileattester.data.repository.AttestationRepository
import com.example.mobileattester.data.repository.AttestationRepositoryImpl
import com.example.mobileattester.data.util.AttestUtil
import com.example.mobileattester.data.util.ElementDataHandler
import com.example.mobileattester.data.util.PolicyDataHandler
import com.example.mobileattester.data.util.UpdateUtil
import com.example.mobileattester.data.util.abs.AsyncRunner
import com.example.mobileattester.data.util.abs.Notifier
import com.example.mobileattester.ui.viewmodel.AttestationViewModelImplFactory


object Injector {

    // Change to edit batch size
    private const val DEFAULT_BATCH_SIZE = Int.MAX_VALUE

    /**
     * @param address Address to init attestation service with
     */
    fun provideAttestationViewModelFactory(address: String): ViewModelProvider.Factory {
        val handler: AttestationDataHandler =
            AttestationDataHandlerImpl("http://$address/")
        val attestationRepo: AttestationRepository = AttestationRepositoryImpl(handler)

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

        /**
         * Create a notifier for updates
         */
        val notifier = Notifier().apply {
            subscribe(elementDataHandler)
        }

        val attestUtil = AttestUtil(
            notifier,
            handler,
            policyDataHandler
        )

        val fnRunner = AsyncRunner(notifier)
        val updateUtil = UpdateUtil(fnRunner, handler)

        return AttestationViewModelImplFactory(
            attestationRepo,
            elementDataHandler,
            attestUtil,
            updateUtil,
        )
    }
}