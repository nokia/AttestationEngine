package com.example.mobileattester.di

import androidx.lifecycle.ViewModelProvider
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
import java.util.*

const val TWENTY_FOUR_H_IN_MS = 86_400_000

object Injector {

    // Change to edit batch size
    private const val DEFAULT_BATCH_SIZE = Int.MAX_VALUE
    val notifier = Notifier()

    /**
     * @param address Address to init attestation service with
     */

    fun not() {
        notifier.notifyAll("")
    }

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

        val overviewProvider = initOverviewProvider(elementDataHandler)

        /**
         * Create a notifier for updates
         */
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

        return AttestationViewModelImplFactory(
            attestationRepo,
            elementDataHandler,
            attestUtil,
            updateUtil,
            overviewProvider
        )
    }

    private fun initOverviewProvider(
        elementDataHandler: ElementDataHandler,
    ): OverviewProvider {
        val t = OverviewProviderImpl(
            elementDataHandler
        )

        val now = System.currentTimeMillis()
        val yd = now - TWENTY_FOUR_H_IN_MS
        val day = Pair(Timestamp(yd), Timestamp(now))

        // Create filters for the overview stuff we are interested to see
        val all = DataFilter("")
        val all24 =
            DataFilter("", timeFrame = day, setOf(ElementResult.FILTER_FLAG_WITHIN_TIMEFRAME))
        val ok = DataFilter("", flags = setOf(ElementResult.FILTER_FLAG_ONLY_RESULT_OK))
        val ok24 = DataFilter("",
            flags = setOf(
                ElementResult.FILTER_FLAG_WITHIN_TIMEFRAME,
                ElementResult.FILTER_FLAG_ONLY_RESULT_OK,
            ),
            timeFrame = day)

        t.addFilterByResults(OverviewProviderImpl.OVERVIEW_ATTESTED_ELEMENTS, all)
        t.addFilterByResults(OverviewProviderImpl.OVERVIEW_ATTESTED_ELEMENTS_24H, all24)
        t.addFilterByResults(OverviewProviderImpl.OVERVIEW_ATTESTED_ELEMENTS_OK, ok)
        t.addFilterByResults(OverviewProviderImpl.OVERVIEW_ATTESTED_ELEMENTS_OK_24H, ok24)

        return t
    }
}