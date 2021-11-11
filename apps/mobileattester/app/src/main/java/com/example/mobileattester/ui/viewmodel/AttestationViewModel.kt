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
    val elements: StateFlow<List<Element>>

    var baseUrl: MutableLiveData<String>
    fun isDefaultBaseUrl(): Boolean

    fun getElement(itemid: String): Element?
    fun getMoreElements()
    fun refreshElements()
}

// --------- Implementation ---------

class AttestationViewModelImpl(
    private val repo: AttestationRepository,
) : AttestationViewModel, ViewModel() {
    private var listOfElementIds: List<String> = listOf()

    private val batchedDataHandler =
        BatchedDataHandler<String, Element>(identifierList = listOf(), batchSize = BATCH_SIZE) {
            repo.getElement(it)
        }

    private val _elements = MutableStateFlow(listOf<Element>())
    private val _isRefreshing = MutableStateFlow(false)

    override val isRefreshing: StateFlow<Boolean> = _isRefreshing
    override val isLoading: StateFlow<Boolean> = batchedDataHandler.isLoading
    override val elements: StateFlow<List<Element>> = _elements

    override var baseUrl = repo.baseUrl
    override fun isDefaultBaseUrl(): Boolean = repo.isDefaultBaseUrl()

    init {
        println("Fetching element ids initially")
        fetchElementIds()
    }

    // ---- Public ----

    override fun getElement(itemid: String): Element? = batchedDataHandler.getDataForId(itemid)

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
        batchedDataHandler.clearBatches()
        _elements.value = listOf()
        getMoreElements()
        _isRefreshing.value = false
    }

    // ---- Private ----

    private fun fetchElementIds() {
        launchCoroutine() {
            listOfElementIds = repo.getElementIds()
            println("ELEMENT ID LIST SIZE: ${listOfElementIds.size} ")
            batchedDataHandler.replaceIdList(listOfElementIds)

            println("Getting initial batch")
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

    companion object {
        const val BATCH_SIZE = 5
        const val FETCH_START_BUFFER = 3
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
