package com.example.mobileattester.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.mobileattester.data.model.Element
import com.example.mobileattester.data.model.ElementResult
import com.example.mobileattester.data.model.emptyElement
import com.example.mobileattester.data.network.Response
import com.example.mobileattester.data.repository.AttestationRepository
import com.example.mobileattester.data.util.AttestationUtil
import com.example.mobileattester.data.util.ElementDataHandler
import com.example.mobileattester.data.util.UpdateUtil
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*

interface AttestationViewModel {
    val isRefreshing: StateFlow<Boolean>
    val isLoading: StateFlow<Boolean>
    val currentUrl: StateFlow<String>

    /** Response object containing the element data/errors */
    val elementFlowResponse: StateFlow<Response<List<Element>>>

    /** Total count of elements in the system */
    val elementCount: StateFlow<Response<Int>>

    /** Get element data, which has already been downloaded */
    fun getElementFromCache(itemid: String): Element?
    fun filterElements(filters: String? = null): List<Element>
    fun getMoreElements()
    fun refreshElements()
    fun refreshElement(itemid: String)

    /** Returns ElementResult if it exists in downloaded data */
    fun findElementResult(resultId: String): ElementResult?

    /** Switch the base url used for the engine */
    fun switchBaseUrl(url: String)

    fun useAttestationUtil(): AttestationUtil
    fun useUpdateUtil(): UpdateUtil
}

// --------- Implementation ---------

// Repo should be replaced with handlers / create a facade for everything?
class AttestationViewModelImpl(
    private val repo: AttestationRepository,
    private val elementDataHandler: ElementDataHandler,
    private val attestationUtil: AttestationUtil,
    private val updateUtil: UpdateUtil,
) : AttestationViewModel, ViewModel() {

    companion object {
        const val FETCH_START_BUFFER = 3
    }


    override val isRefreshing: StateFlow<Boolean> = elementDataHandler.isRefreshing
    override val isLoading: StateFlow<Boolean> = elementDataHandler.isLoading
    override val currentUrl: StateFlow<String> = repo.currentUrl
    override val elementFlowResponse: StateFlow<Response<List<Element>>> =
        elementDataHandler.dataFlow
    override val elementCount: StateFlow<Response<Int>> = elementDataHandler.idCount


    override fun getElementFromCache(itemid: String): Element? =
        elementDataHandler.getDataForId(itemid)


    override fun getMoreElements() = elementDataHandler.fetchNextBatch()
    override fun filterElements(filters: String?) = elementDataHandler.dataAsList(filters)
    override fun refreshElements() = elementDataHandler.refreshData(hardReset = true)
    override fun refreshElement(itemid: String) = elementDataHandler.refreshSingleValue(itemid)

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

    override fun switchBaseUrl(url: String) {
        println("SwitchedBaseUrl")
        repo.rebuildService(url)
        elementDataHandler.refreshData(true)
        attestationUtil.reset(true)
    }

    override fun useAttestationUtil(): AttestationUtil = attestationUtil
    override fun useUpdateUtil(): UpdateUtil = updateUtil
}

class AttestationViewModelImplFactory(
    private val repo: AttestationRepository,
    private val elementDataHandler: ElementDataHandler,
    private val attestUtil: AttestationUtil,
    private val updateUtil: UpdateUtil,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return AttestationViewModelImpl(repo, elementDataHandler, attestUtil, updateUtil) as T
    }
}
