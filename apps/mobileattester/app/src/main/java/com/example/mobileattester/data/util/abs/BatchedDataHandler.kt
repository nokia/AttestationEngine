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
    private val scope = CoroutineScope(job)

    private val _dataFlow: MutableStateFlow<Response<List<U?>>> = MutableStateFlow(Response.idle())
    private val _idCount: MutableStateFlow<Response<Int>> = MutableStateFlow(Response.idle(0))
    private val _loading: MutableStateFlow<Boolean> = MutableStateFlow(false)
    private val _refreshing: MutableStateFlow<Boolean> = MutableStateFlow(false)

    val dataFlow: StateFlow<Response<List<U?>>> = _dataFlow
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
    fun fetchNextBatch() = fetchFreeBatch()

    /** Call to start a loop, that continuously fetches batches one-by-one */
    fun startFetchLoop() {}

    /** Stop the fetch loop */
    fun stopFetchLoop() {}

    /**
     * Refresh the contained data
     * @param hardReset Set to true to also fetch the ids again
     */
    fun refreshData(hardReset: Boolean = false) {
        refreshing.value = true
        when (hardReset) {
            true -> initIds()
            false -> {
                clearBatchData()
                fetchFreeBatch()
            }
        }

        scope.launch {
            delay(500)
            refreshing.value = false
        }
    }

    fun refreshSingleValue(id: T) {
        updateLoadingState()
        fetchedData[id] = BatchElement(fetchedData[id]?.data, Status.LOADING)

        scope.launch(Dispatchers.IO) {
            val success = tryFun {
                val data = fetchDataForId(id)
                fetchedData[id] = BatchElement(data, Status.SUCCESS)
            }
            if (!success) {
                fetchedData[id] = BatchElement(null, Status.ERROR)
            }
            updateLoadingState()
        }
    }

    fun getDataForId(id: T): U? {
        return fetchedData[id]?.data
    }

    fun dataAsList(filters: List<DataFilter>): List<U> {
        return recursiveFilter(filters, dataAsList())
    }

    /**
     * Returns all data from downloaded batches as a list
     * and optionally filters if U implements Filterable.
     */
    fun dataAsList(filter: DataFilter? = null): List<U> {
        return filterList(filter, fetchedData.map { it.value.data }.filterNotNull())
    }

    // --------------------------------- Private ---------------------------------------
    // --------------------------------- Private ---------------------------------------
    // --------------------------------- Private ---------------------------------------

    private fun initIds() {
        updateLoadingState()

        scope.launch {
            clearBatchData()
            _idCount.value = Response.loading()
            fetchedIds.clear()

            try {
                fetchedIds.addAll(fetchIdList())
            } catch (e: Exception) {
                _idCount.value = Response.error(null, "ID fetch error: $e")
                return@launch
            }

            batches = fetchedIds.chunked(batchSize)
            batches.forEachIndexed { index, _ ->
                batchStates[index] = Status.IDLE
            }
            _idCount.value = Response.success(fetchedIds.size)
            fetchFreeBatch()
            updateLoadingState()
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

    /**
     * Call to fetch data for a batch which is not yet loading/fetched.
     */
    private fun fetchFreeBatch() {
        // Get the first idle batch
        val batch = batchStates.firstNotNullOfOrNull {
            when (it.value) {
                Status.IDLE -> it
                else -> null
            }
        } ?: return

        updateLoadingState()
        batchStates[batch.key] = Status.LOADING

        scope.launch {
            val batchIds = batches[batch.key]

            batchStates[batch.key] = when (fetchDataForIds(batchIds)) {
                false -> Status.SUCCESS.also {
                    _dataFlow.value = Response.success(dataAsList())
                }
                true -> Status.ERROR.also {
                    _dataFlow.value = Response.error(dataAsList(), "An error occurred.")
                    handleErrorBatch(batch.key)
                }
            }
            updateLoadingState()
        }
    }

    private fun handleErrorBatch(bn: Int) {
        // TODO retry for failed values inside batches that have failed
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

    private fun clearBatchData() {
        _dataFlow.value = Response.loading(listOf())
        fetchedData.clear()
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
}


