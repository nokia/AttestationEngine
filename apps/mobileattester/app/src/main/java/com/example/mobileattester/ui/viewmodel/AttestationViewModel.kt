package com.example.mobileattester.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.mobileattester.data.model.Element
import com.example.mobileattester.data.repository.AttestationRepository
import com.example.mobileattester.data.util.ElementDataHandler
import kotlinx.coroutines.flow.StateFlow

interface AttestationViewModel {
    val isRefreshing: StateFlow<Boolean>
    val isLoading: StateFlow<Boolean>
    val currentUrl: StateFlow<String>

    /** Elements which have been downloaded to the client */
    val elementFlow: StateFlow<List<Element>>

    /** Total count of elements in the system */
    val elementCount: StateFlow<Int>

    /** Get element data, which has already been downloaded */
    fun getElementFromCache(itemid: String): Element?
    fun filterElements(filters: String): List<Element>
    fun getMoreElements()
    fun refreshElements()


    /** Switch the base url used for the engine */
    fun switchBaseUrl(url: String)
}

// --------- Implementation ---------

// Repo should be replaced with handlers / create a facade for everything?
class AttestationViewModelImpl(
    private val repo: AttestationRepository,
    private val elementDataHandler: ElementDataHandler,
) : AttestationViewModel, ViewModel() {

    companion object {
        // Index of rendered element + FETCH_START_BUFFER > currently fetched count => Fetch the next batch
        const val FETCH_START_BUFFER = 3
    }

    override val isRefreshing: StateFlow<Boolean> = elementDataHandler.isRefreshing
    override val isLoading: StateFlow<Boolean> = elementDataHandler.isLoading
    override val currentUrl: StateFlow<String> = repo.currentUrl
    override val elementFlow: StateFlow<List<Element>> = elementDataHandler.dataFlow
    override val elementCount: StateFlow<Int> = elementDataHandler.idCount


    override fun getElementFromCache(itemid: String): Element? =
        elementDataHandler.getDataForId(itemid)

    override fun getMoreElements() = elementDataHandler.fetchNextBatch()
    override fun filterElements(filters: String) = elementDataHandler.dataAsList(filters)
    override fun refreshElements() = elementDataHandler.refreshData(hardReset = true)

    override fun switchBaseUrl(url: String) {
        repo.rebuildService(url)
        elementDataHandler.refreshData(hardReset = true)
    }
}

class AttestationViewModelImplFactory(
    private val repo: AttestationRepository,
    private val elementDataHandler: ElementDataHandler,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return AttestationViewModelImpl(repo, elementDataHandler) as T
    }
}
