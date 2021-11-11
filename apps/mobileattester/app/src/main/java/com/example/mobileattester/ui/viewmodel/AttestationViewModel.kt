package com.example.mobileattester.ui.viewmodel

import androidx.lifecycle.*
import com.example.mobileattester.data.model.Element
import com.example.mobileattester.data.network.retryIO
import com.example.mobileattester.data.repository.AttestationRepository
import com.example.mobileattester.data.util.BatchedDataHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

interface AttestationViewModel {
    val isRefreshing: StateFlow<Boolean>
    val isLoading: StateFlow<Boolean>

    /** Elements which have been downloaded to the client */
    val elementFlow: StateFlow<List<Element>>

    /** Total count of elements in the system */
    val elementCount: StateFlow<Int>

    /** Get element data, which has already been downloaded */
    fun getElementFromCache(itemid: String): Element?
    fun getMoreElements()
    fun refreshElements()

    var baseUrl: MutableLiveData<String>
    fun isDefaultBaseUrl(): Boolean
}

// --------- Implementation ---------

class AttestationViewModelImpl(
    private val repo: AttestationRepository,
) : AttestationViewModel, ViewModel() {

    companion object {
        // How many element to fetch at a time
        const val BATCH_SIZE = 5

        // Index of rendered element + FETCH_START_BUFFER > currently fetched count => Fetch the next batch
        const val FETCH_START_BUFFER = 3
    }

    // All element ids in the current system
    private var listOfElementIds: List<String> = listOf()

    private val batchedDataHandler =
        BatchedDataHandler<String, Element>(identifierList = listOf(), batchSize = BATCH_SIZE) {
            repo.getElement(it)
        }

    private val _elementCount = MutableStateFlow(0)
    private val _elements = MutableStateFlow(listOf<Element>())
    private val _isRefreshing = MutableStateFlow(false)

    override val isRefreshing: StateFlow<Boolean> = _isRefreshing
    override val isLoading: StateFlow<Boolean> = batchedDataHandler.isLoading
    override val elementFlow: StateFlow<List<Element>> = _elements
    override val elementCount: StateFlow<Int> = _elementCount

    override var baseUrl = repo.baseUrl

    override fun isDefaultBaseUrl(): Boolean = repo.isDefaultBaseUrl()

    init {
        println("Fetching element ids")
        fetchElementIds()
    }

    // ---- Public ----

    override fun getElementFromCache(itemid: String): Element? =
        batchedDataHandler.getDataForId(itemid)

    override fun getMoreElements() {
        if (batchedDataHandler.allChunksLoaded()) {
            return
        }

        launchCoroutine {
            println("getMoreCalled")

            val res = batchedDataHandler.fetchNextBatch().data ?: run {
                println("Returning")
                return@launchCoroutine
            }

            println("Res length: ${res.size}")
            val copy = mutableListOf<Element>().apply {
                this.addAll(_elements.value)
                this.addAll(res)
            }
            _elements.value = copy
        }
    }

    override fun refreshElements() {
        _isRefreshing.value = true

        // Clear batch cache + local element values
        batchedDataHandler.clearBatches()
        _elements.value = listOf()

        // Start the loading process again
        getMoreElements()

        _isRefreshing.value = false
    }

    // ---- Private ----

    private fun fetchElementIds() {
        launchCoroutine() {
            listOfElementIds = repo.getElementIds()
            batchedDataHandler.replaceIdList(listOfElementIds)
            _elementCount.value = listOfElementIds.count()

            // Load initial batch
            getMoreElements()
        }
    }

    private fun launchCoroutine(rt: Int = 5, func: suspend () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            retryIO(rt) {
                func()
            }
        }
    }

}

class AttestationViewModelImplFactory(
    private val repo: AttestationRepository,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return AttestationViewModelImpl(repo) as T
    }
}
