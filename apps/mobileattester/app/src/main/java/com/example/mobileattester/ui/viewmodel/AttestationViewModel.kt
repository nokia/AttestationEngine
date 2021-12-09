package com.example.mobileattester.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.mobileattester.data.model.Element
import com.example.mobileattester.data.model.ElementResult
import com.example.mobileattester.data.model.Policy
import com.example.mobileattester.data.network.Response
import com.example.mobileattester.data.repository.AttestationRepository
import com.example.mobileattester.data.util.*
import com.example.mobileattester.data.util.abs.DataFilter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

interface AttestationViewModel {
    val isRefreshing: StateFlow<Boolean>
    val isLoading: StateFlow<Boolean>
    val currentUrl: StateFlow<String>

    /** Response object containing the element data/errors */
    val elementFlowResponse: StateFlow<Response<List<Element>>>

    /** Total count of elements in the system */
    val elementCount: StateFlow<Int>

    /** Get element data, which has already been downloaded */
    fun getElementFromCache(itemid: String): Element?
    fun filterElements(filter: DataFilter? = null): List<Element>
    fun filterElements(filters: List<DataFilter>): List<Element>

    fun getMoreElements()
    fun refreshElements()
    fun refreshElement(itemid: String)

    fun startElementFetchLoop()
    fun stopElementFetchLoop()

    /** Returns ElementResult if it exists in downloaded data */
    fun findElementResult(resultId: String): ElementResult?

    fun getPolicyFromCache(policyId: String): Policy?

    /** Switch the base url used for the engine */
    fun switchBaseUrl(url: String)

    // Use the different classes directly to avoid cluttering in this vm
    fun getLatestResults(hoursSince: Int? = null): MutableStateFlow<List<ElementResult>>

    val attestationUtil: AttestationUtil
    val updateUtil: UpdateUtil
    val overviewProvider: OverviewProvider
    val engineInfo: EngineInfo
    val mapManager: MapManager
}

// --------- Implementation ---------

class AttestationViewModelImpl(
    private val repo: AttestationRepository,
    private val elementDataHandler: ElementDataHandler,
    override val attestationUtil: AttestationUtil,
    override val updateUtil: UpdateUtil,
    override val overviewProvider: OverviewProvider,
    override val engineInfo: EngineInfo,
    override val mapManager: MapManager,
) : AttestationViewModel, ViewModel() {
    companion object {
        const val FETCH_START_BUFFER = 3
    }

    override val isRefreshing: StateFlow<Boolean> = MutableStateFlow(false) // TODO
    override val isLoading: StateFlow<Boolean> = MutableStateFlow(false) // TODO
    override val currentUrl: StateFlow<String> = repo.currentUrl
    override val elementFlowResponse: StateFlow<Response<List<Element>>> =
        elementDataHandler.dataFlow
    override val elementCount: StateFlow<Int> = elementDataHandler.idCount

    override fun getElementFromCache(itemid: String): Element? =
        elementDataHandler.getDataForId(itemid)

    override fun getMoreElements() = elementDataHandler.fetchNextBatch()

    override fun filterElements(filter: DataFilter?): List<Element> =
        elementDataHandler.dataAsList(filter)

    override fun filterElements(filters: List<DataFilter>): List<Element> =
        elementDataHandler.dataAsList(filters)

    override fun refreshElements() = elementDataHandler.refreshData(hardReset = true)
    override fun refreshElement(itemid: String) = elementDataHandler.refreshSingleValue(itemid)

    override fun startElementFetchLoop() = elementDataHandler.startFetchLoop()
    override fun stopElementFetchLoop() = elementDataHandler.stopFetchLoop()

    override fun findElementResult(resultId: String): ElementResult? {
        val data = elementDataHandler.dataFlow.value.data ?: return null

        for (element in data) {
            val result = element.results.find {
                it.itemid == resultId
            }
            if (result != null) return result
        }

        return null
    }

    override fun getLatestResults(hoursSince: Int?): MutableStateFlow<List<ElementResult>> =
        overviewProvider.getOverview(hoursSince)


    override fun getPolicyFromCache(policyId: String): Policy? =
        attestationUtil.getPolicyFromCache(policyId)

    override fun switchBaseUrl(url: String) {
        println("SwitchedBaseUrl")
        repo.rebuildService(url)
        elementDataHandler.refreshData(true)
        attestationUtil.reset(true)
    }
}

class AttestationViewModelImplFactory(
    private val repo: AttestationRepository,
    private val elementDataHandler: ElementDataHandler,
    private val attestUtil: AttestationUtil,
    private val updateUtil: UpdateUtil,
    private val overviewProvider: OverviewProvider,
    private val engineInfo: EngineInfo,
    private val mapManager: MapManager,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return AttestationViewModelImpl(repo,
            elementDataHandler,
            attestUtil,
            updateUtil,
            overviewProvider,
            engineInfo,
            mapManager) as T
    }
}
