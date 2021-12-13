package com.example.mobileattester.data.util.abs

import android.util.Log
import com.example.mobileattester.data.network.Response
import com.example.mobileattester.data.network.Status
import com.example.mobileattester.data.network.retryIO
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

private const val TAG = "BatchedDataHandler"

// Typealiases for the functions that need to be provided for the Batched data handler.
typealias FetchIdList<T> = suspend () -> List<T>
typealias FetchIdData<T, U> = suspend (T) -> U

data class BatchElement<T>(
    val data: T?,
    val status: Status,
)

/**
 *  Data fetching in batches.
 *
 *  Gets a list of all the item ids ->
 *  Creates chunks of the ids, size of param batchSize  ->
 *  Initially fetches data for one chunk.
 *
 *  T - Id type
 *  U - Data type
 */
abstract class BatchedDataHandler<T, U>(
    private val batchSize: Int,
    private val fetchIdList: FetchIdList<T>,
    private val fetchDataForId: FetchIdData<T, U>,
    private val notifier: Notifier? = null,
) : NotificationSubscriber {
    // Fetched ids are divided into batches, and the data for these ids is fetched in batches.
    private var batches: List<List<T>> = listOf()
    private var batchStates: MutableMap<Int/* Index of batch */, Status> = mutableMapOf()

    private val fetchedIds: MutableList<T> = mutableListOf()
    private val fetchedData: MutableMap<T, BatchElement<U>> = mutableMapOf()

    private val job = Job()
    private val loopJob = Job()
    private val scope = CoroutineScope(job)
    private val loopScope = CoroutineScope(loopJob)

    private val _dataFlow: MutableStateFlow<Response<List<U>>> = MutableStateFlow(Response.idle())
    private val _currentFilters: MutableStateFlow<List<DataFilter>> = MutableStateFlow(listOf())
    private val _idCount: MutableStateFlow<Response<Int>> = MutableStateFlow(Response.idle(0))
    private val _loading: MutableStateFlow<Boolean> = MutableStateFlow(false)
    private val _refreshing: MutableStateFlow<Boolean> = MutableStateFlow(false)

    val dataFlow: StateFlow<Response<List<U>>> = _dataFlow
    val appliedFilters: StateFlow<List<DataFilter>> = _currentFilters
    val idCount: StateFlow<Response<Int>> = _idCount
    val loading: StateFlow<Boolean> = _loading
    val refreshing: MutableStateFlow<Boolean> = _refreshing

    init {
        initIds()
    }

    // --------------------------------- Public ---------------------------------------
    // --------------------------------- Public ---------------------------------------
    // --------------------------------- Public ---------------------------------------

    /** Call to fetch the next available batch in line */
    fun fetchNextBatch() {
        scope.launch {
            fetchFreeBatch()
        }
    }

    /** Call to start a loop, that continuously fetches batches one-by-one */
    fun startFetchLoop() {
        if (loopJob.children.count() != 0 || allBatchesFetched()) return

        loopScope.launch {
            Log.d(TAG, "startFetchLoop: called")

            while (!allBatchesFetched()) {
                fetchFreeBatch()
            }
        }
    }

    /** Stop the fetch loop */
    fun stopFetchLoop() {
        loopJob.cancelChildren(CancellationException("Cancelled."))
    }

    /**
     * Refresh the contained data
     * @param hardReset Set to true to also fetch the ids again
     */
    fun refreshData(hardReset: Boolean = false) {
        if (refreshing.value) return

        refreshing.value = true
        when (hardReset || _idCount.value.status == Status.ERROR) {
            true -> initIds()
            false -> {
                scope.launch {
                    cleanUp(batches = true, filters = false, ids = false)
                    fetchFreeBatch()
                }
            }
        }

        scope.launch {
            delay(500)
            refreshing.value = false
        }
    }

    fun refreshSingleValue(id: T) {
        fetchedData[id] = BatchElement(fetchedData[id]?.data, Status.LOADING)

        scope.launch(Dispatchers.IO) {
            val success = tryFun {
                val data = fetchDataForId(id)
                fetchedData[id] = BatchElement(data, Status.SUCCESS)
                _dataFlow.value = Response.success(
                    recursiveFilter(_currentFilters.value, getNotNullBatchData()),
                )
            }
            if (!success) {
                fetchedData[id] = BatchElement(null, Status.ERROR)
            }
        }
    }

    fun getDataFromCache(id: T): U? {
        return fetchedData[id]?.data
    }

    fun applyFilters(filters: List<DataFilter>) {
        _currentFilters.value = filters

        _dataFlow.value = Response(
            status = _dataFlow.value.status,
            data = recursiveFilter(filters, fetchedData.map { it.value.data }.filterNotNull()),
            message = _dataFlow.value.message,
        )
    }

    // --------------------------------- Private ---------------------------------------
    // --------------------------------- Private ---------------------------------------
    // --------------------------------- Private ---------------------------------------

    private fun recursiveFilter(filters: List<DataFilter>, data: List<U>): List<U> {
        return when (filters.firstOrNull()) {
            null -> return data
            else -> {
                val fs = filters.toMutableList()
                val d = filterList(fs.removeAt(0), data)
                recursiveFilter(fs, d)
            }
        }
    }

    private fun filterList(filter: DataFilter?, list: List<U>): List<U> {
        if (filter == null) {
            return list
        }

        val matchingValues = mutableListOf<U>()
        list.forEach { entry ->
            when (entry) {
                is Filterable -> {
                    if (entry.filter(filter)) {
                        matchingValues.add(entry)
                    }
                }
                else -> {
                    // We can return here, none of the values will be Filterable
                    return@forEach
                }
            }
        }
        return matchingValues
    }

    private fun initIds() {
        scope.launch {
            updateLoadingState()
            _idCount.value = Response.loading()
            cleanUp(batches = true, filters = true, ids = true)

            try {
                retryIO {
                    fetchedIds.addAll(fetchIdList())
                }
                _idCount.value = Response.success(fetchedIds.size)
            } catch (e: Exception) {
                Log.e(TAG, "initIds: $e")
                _idCount.value = Response.error(null, "ID fetch error: $e")
                return@launch
            }

            batches = fetchedIds.chunked(batchSize)
            batches.forEachIndexed { index, _ ->
                batchStates[index] = Status.IDLE
            }
            fetchFreeBatch()
        }
    }

    /**
     * Call to fetch data for a batch which is not yet loading/fetched.
     */
    private suspend fun fetchFreeBatch() {
        // Get the first idle batch
        val batch = batchStates.firstNotNullOfOrNull {
            when (it.value) {
                Status.IDLE -> it
                else -> null
            }
        } ?: return

        batchStates[batch.key] = Status.LOADING
        updateLoadingState()

        val batchIds = batches[batch.key]

        batchStates[batch.key] = when (fetchDataForIds(batchIds)) {
            false -> Status.SUCCESS.also {
                _dataFlow.value = Response.success(
                    recursiveFilter(_currentFilters.value, getNotNullBatchData()),
                )
            }
            true -> Status.ERROR.also {
                _dataFlow.value = Response.error(recursiveFilter(
                    _currentFilters.value,
                    getNotNullBatchData(),
                ), "An error occurred.")
                handleErrorBatch(batch.key)
            }
        }
        updateLoadingState()

    }

    private fun getNotNullBatchData(): List<U> {
        return fetchedData.map { it.value.data }.filterNotNull()
    }

    private fun handleErrorBatch(bn: Int) {
        // TODO retry for failed values inside batches that have failed
        batchStates[bn] = Status.SUCCESS
    }

    /**
     * Fetches data for the provided list of ids, and sets the status/data of the id
     * based on the response.
     * @return boolean, did something fail?
     */
    private suspend fun fetchDataForIds(ids: List<T>): Boolean {
        var errors = false
        ids.map { id ->
            scope.launch(Dispatchers.IO) {
                val success = tryFun {
                    Log.d(TAG, "fetchData: for id: $id")
                    val data = fetchDataForId(id)
                    fetchedData[id] = BatchElement(data, Status.SUCCESS)
                }
                if (!success) {
                    fetchedData[id] = BatchElement(null, Status.ERROR)
                    errors = true
                }
            }
        }.joinAll()
        return errors
    }

    private suspend fun tryFun(func: suspend () -> Unit): Boolean {
        return try {
            retryIO(times = 10, initialDelay = 50, factor = 2.0) {
                func()
            }
            true
        } catch (e: Exception) {
            Log.e(TAG, "startJob: $e")
            false
        }
    }

    private fun cleanUp(
        batches: Boolean,
        filters: Boolean,
        ids: Boolean,
    ) {
        if (batches) {
            fetchedData.clear()
            this.batches.forEachIndexed { index, _ ->
                batchStates[index] = Status.IDLE
            }
            _dataFlow.value = Response.idle(listOf())
        }

        if (filters) {
            _currentFilters.value = listOf()
        }

        if (ids) {
            fetchedIds.clear()
            this.batches = listOf()
            batchStates.clear()
            _idCount.value = Response.loading(null)
        }
    }

    private fun cancelJobs(reason: String = "Default") {
        Log.w(TAG, "cancelJobs: Cancelling jobs")
        job.cancelChildren(CancellationException(reason))
    }

    private fun updateLoadingState() {
        val itemsLoading = fetchedData.map { it.value.status == Status.LOADING }.contains(true)
        val batchesLoading = batchStates.map { it.value == Status.LOADING }.contains(true)

        _loading.value = itemsLoading || batchesLoading
    }

    private fun allBatchesFetched(): Boolean {
        return !batchStates.map { it.value == Status.SUCCESS || it.value == Status.ERROR }
            .contains(false)
    }
}


