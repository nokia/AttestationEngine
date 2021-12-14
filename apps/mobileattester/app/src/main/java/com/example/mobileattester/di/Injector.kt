package com.example.mobileattester.di

import android.content.Context
import androidx.lifecycle.ViewModelProvider
import com.example.mobileattester.BuildConfig.BATCHSIZE
import com.example.mobileattester.data.location.LocationHandler
import com.example.mobileattester.data.network.AttestationDataHandler
import com.example.mobileattester.data.network.AttestationDataHandlerImpl
import com.example.mobileattester.data.repository.AttestationRepository
import com.example.mobileattester.data.repository.AttestationRepositoryImpl
import com.example.mobileattester.data.util.*
import com.example.mobileattester.data.util.abs.AsyncRunner
import com.example.mobileattester.data.util.abs.Notifier
import com.example.mobileattester.ui.viewmodel.AttestationViewModelImplFactory

object Injector {
    private val DEFAULT_BATCH_SIZE = BATCHSIZE.toInt()

    /**
     * @param address Address to init attestation service with
     */
    fun provideAttestationViewModelFactory(
        address: String,
        ctx: Context,
    ): ViewModelProvider.Factory {
        /**
         * Create a notifier for updating client data when we send requests to server, and
         * the data needs to be updated locally.
         */
        val notifier = Notifier()

        val handler: AttestationDataHandler =
            AttestationDataHandlerImpl("http://$address/", notifier)

        val attestationRepo: AttestationRepository = AttestationRepositoryImpl(handler)

        /*
            Here, Initialize the BatchedDataHandlers of different types.
            Link the repository methods to the corresponding handlers to get the
            data each of them are managing.
        */
        val elementDataHandler = ElementDataHandler(
            DEFAULT_BATCH_SIZE, //
            { attestationRepo.getElementIds() },
            { attestationRepo.getElement(it) },
            notifier,
        )

        val policyDataHandler = PolicyDataHandler(
            Int.MAX_VALUE,  // For policies, a single batch is ok, if policy count is big, consider changing
            { attestationRepo.getPolicyIds() },
            { attestationRepo.getPolicy(it) },
        )

        val overviewProvider = OverviewProviderImpl(dataHandler = handler)
        val engineInfo = EngineInfoImpl(dataHandler = handler)

        notifier.apply {
            addSubscriber(elementDataHandler)
            addSubscriber(overviewProvider)
            addSubscriber(engineInfo)
        }

        val attestUtil = AttestUtil(notifier, handler, policyDataHandler)
        val fnRunner = AsyncRunner(notifier)
        val updateUtil = UpdateUtil(fnRunner, handler)

        val locationHandler = LocationHandler(ctx)
        val locationEditor = ElementLocationEditor(locationHandler)
        locationHandler.startLocationUpdates()

        val mapManager = MapManager(locationEditor)

        return AttestationViewModelImplFactory(attestationRepo,
            elementDataHandler,
            attestUtil,
            updateUtil,
            overviewProvider,
            engineInfo,
            mapManager)
    }
}