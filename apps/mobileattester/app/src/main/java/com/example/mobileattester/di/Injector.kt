package com.example.mobileattester.di

import android.content.Context
import androidx.lifecycle.ViewModelProvider
import com.example.mobileattester.data.location.LocationHandler
import com.example.mobileattester.data.model.ElementResult
import com.example.mobileattester.data.network.AttestationDataHandler
import com.example.mobileattester.data.network.AttestationDataHandlerImpl
import com.example.mobileattester.data.repository.AttestationRepository
import com.example.mobileattester.data.repository.AttestationRepositoryImpl
import com.example.mobileattester.data.util.*
import com.example.mobileattester.data.util.abs.AsyncRunner
import com.example.mobileattester.data.util.abs.DataFilter
import com.example.mobileattester.data.util.abs.Notifier
import com.example.mobileattester.ui.util.Timestamp
import com.example.mobileattester.ui.viewmodel.AttestationViewModelImplFactory

const val TWENTY_FOUR_H_IN_MS = 86_400_000

object Injector {

    /**
     * Batch size used for fetching Elements.
     *
     * Currently we get everything at once. This is due to
     * not having a results-endpoint, which we could get data for overview from.
     * If the batch size is changed, and
     * @see OverviewProviderImpl
     * is not changed, the overview will only show data for the elements in the
     * batches that are downloaded. Initially only one batch is fetched, and more
     * loaded once the user goes through the list of elements. The overview updates
     * whenever we get more data, but is not accurate until all elements are fetched.
     */
    private const val DEFAULT_BATCH_SIZE = Int.MAX_VALUE


    /**
     * @param address Address to init attestation service with
     */
    fun provideAttestationViewModelFactory(
        address: String,
        ctx: Context,
    ): ViewModelProvider.Factory {
        val handler: AttestationDataHandler =
            AttestationDataHandlerImpl("http://$address/")
        val attestationRepo: AttestationRepository = AttestationRepositoryImpl(handler)

        /**
         * Create a notifier for updates
         */
        val notifier = Notifier()

        /*
            Here, Initialize the BatchedDataHandlers of different types.

            Link the repository methods to the corresponding handlers to get the
            data each of them are managing.
        */
        val elementDataHandler = ElementDataHandler(
            DEFAULT_BATCH_SIZE,
            { attestationRepo.getElementIds() },
            { attestationRepo.getElement(it) },
            notifier
        )

        val policyDataHandler = PolicyDataHandler(
            DEFAULT_BATCH_SIZE,
            { attestationRepo.getPolicyIds() },
            { attestationRepo.getPolicy(it) }
        )

        val overviewProvider = OverviewProviderImpl(dataHandler = handler)


        notifier.apply {
            addSubscriber(elementDataHandler)
            addSubscriber(overviewProvider)
        }

        val attestUtil = AttestUtil(
            notifier,
            handler,
            policyDataHandler
        )

        val fnRunner = AsyncRunner(notifier)
        val updateUtil = UpdateUtil(fnRunner, handler)

        // Location
        val locationHandler = LocationHandler(ctx)
        val locationEditor = ElementLocationEditor(locationHandler)
        locationHandler.startLocationUpdates()

        val mapManager = MapManager(locationEditor)

        return AttestationViewModelImplFactory(
            attestationRepo,
            elementDataHandler,
            attestUtil,
            updateUtil,
            overviewProvider,
            mapManager
        )
    }
}