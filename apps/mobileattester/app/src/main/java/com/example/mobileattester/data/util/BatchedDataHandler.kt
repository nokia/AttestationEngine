package com.example.mobileattester.data.util

import android.util.Log
import com.example.mobileattester.data.model.Element
import com.example.mobileattester.data.model.Policy
import com.example.mobileattester.data.network.Response
import com.example.mobileattester.data.network.retryIO
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow

const val TIMEOUT = 10_000L
private const val TAG = "BatchedDataHandler"

// Typealiases for the functions that need to be provided for the Batched data handler.
typealias FetchIdList<T> = suspend () -> List<T>
typealias FetchIdData<T, U> = suspend (T) -> U


// ------------------------------------------------------------------------------------------
// ------------ Concrete classes to handle different types of data in batches ---------------
// ------------------------------------------------------------------------------------------

class ElementDataHandler(
    batchSize: Int,
    fetchIdList: FetchIdList<String>,
    fetchDataForId: FetchIdData<String, Element>,
) : BatchedDataHandler<String, Element>(batchSize, fetchIdList, fetchDataForId)

class PolicyDataHandler(
    batchSize: Int,
    fetchIdList: FetchIdList<String>,
    fetchDataForId: FetchIdData<String, Policy>,
) : BatchedDataHandler<String, Policy>(batchSize, fetchIdList, fetchDataForId)


// ------------------------------------------------------------------------------------------
// --------------- Abstract class for above to handle data in batches -----------------------
// ------------------------------------------------------------------------------------------

/**
 *  Data fetching in batches.
 *  T - Id type
 *  U - Data type
 *
 *  TODO ************************
 *  -   Functions to start and end "endless" and automatic batch fetching. Needs to be done
 *      since search is implemented on client side only.
 *
 *      When search is not used, we can fetch the data only for the batches that are up for render.
 *      When the user clicks on search/starts typing, start "endless" batch fetching which runs
 *      until all data is downloaded, or the user no longer uses search.
 *
 *  -   Proper error handling
 */
abstract class BatchedDataHandler<T, U>(
    private val batchSize: Int,
    private val fetchIdList: FetchIdList<T>,
    private val fetchDataForId: FetchIdData<T, U>,
) {
    private val job = Job()
    private val scope = CoroutineScope(job)

    private var idList: List<T>? = null
    private var listChunks: List<List<T>>? = null
    private var _refreshingData = MutableStateFlow(false)

    /**
     * Fetched data
     */
    private val batches = mutableMapOf<Int, List<Pair<T, U>>>()
    private val batchesLoading = MutableStateFlow(mutableSetOf<Int>())

    /** Data from batches in a list */
    val dataFlow = MutableStateFlow(Response.loading(listOf<U>()))

    /** Any of the batches currently loading? */
    val isLoading: MutableStateFlow<Boolean> = MutableStateFlow(batchesLoading.value.size != 0)

    /** Is the data currently refreshing (idList / batches) */
    val isRefreshing: MutableStateFlow<Boolean> = _refreshingData
    val idCount: MutableStateFlow<Response<Int>> = MutableStateFlow(Response.loading(null))

    init {
        scope.launch(Dispatchers.IO) {
            reloadIdList()
            fetchNextBatch()
        }
    }

    /**
     * Call to start fetch for the next batch.
     */
    fun fetchNextBatch() {
        val batchNumber = (batches.keys.maxOrNull() ?: -1) + 1
        val isLoading = batchesLoading.value.contains(batchNumber)
        if (isLoading || allChunksLoaded()) {
            return
        }

        setLoading(batchNumber)

        scope.launch {
            try {
                batches[batchNumber] = fetchBatch(batchNumber)
                dataFlow.value = Response.success(dataAsList())
            } catch (e: Exception) {
                Log.d(TAG, "failed to fetch next batch[$batchNumber]: $e")
                dataFlow.value = Response.error(message = "Data could not be fetched: $e")
            } finally {
                setNotLoading(batchNumber)
            }
        }
    }

    /**
     * Refresh data
     * @param hardReset Set to true to also fetch the ids again
     */
    fun refreshData(hardReset: Boolean = false) {
        if (_refreshingData.value && !hardReset) {
            return
        }

        _refreshingData.value = true
        clearBatches()
        job.cancelChildren(CancellationException("Refreshing data"))

        scope.launch(Dispatchers.IO) {
            try {
                retryIO {
                    if (hardReset) {
                        reloadIdList()
                    }
                }
                fetchNextBatch()
            } catch (e: Exception) {
                Log.d(TAG, "refreshData: $e")
            } finally {
                scope.launch {
                    delay(500) // TODO Remove fake delay
                    _refreshingData.value = false
                }
            }
        }
    }

    fun getDataForId(id: T): U? {
        for (list in batches.values) {
            val data = list.find { it.first == id }
            data?.let {
                return it.second
            }
        }
        return null
    }

    /**
     * Call to refresh data for a single item
     */
    fun refreshSingleValue(id: T) {
        scope.launch {
            try {
                val updatedData = fetchDataForId(id)

                for (list in batches) {
                    list.value.forEachIndexed { index, it ->
                        if (it.first == id) {
                            val cp = list.value.map { it }.toMutableList()
                            cp[index] = Pair(id, updatedData)

                            batches[list.key] = cp
                            dataFlow.value = Response.success(dataAsList())
                            println("Successful update for id $id")
                            return@launch
                        }
                    }
                }
            } catch (e: Exception) {
                println("Cannot refresh single value: $e")
            }
        }
    }

    /**
     * Returns all data from downloaded batches as a list and optionally filters using keywords separated by spaces.
     */
    fun dataAsList(filters: String? = null): List<U> {
        val loadedBatchValues = mutableListOf<U>()


        batches.entries.forEach { entry ->
            entry.value.forEach {
                if (filters == null)
                    loadedBatchValues.add(it.second)
                else {
                    if (it.second is Element) {
                        if (filters.split(' ').all { filter ->
                                when {
                                    // TODO Abstraction
                                    (it.second as Element).name.lowercase()
                                        .contains(filter.lowercase()) -> true
                                    ((it.second as Element).description?.lowercase()
                                        ?: "").contains(filter.lowercase()) -> true
                                    (it.second as Element).endpoint.lowercase()
                                        .contains(filter.lowercase()) -> true
                                    (it.second as Element).protocol.lowercase()
                                        .contains(filter.lowercase()) -> true
                                    (it.second as Element).types.any { tag ->
                                        tag.lowercase().contains(filter.lowercase())
                                    } -> true
                                    else -> false
                                }
                            })
                            loadedBatchValues.add(it.second)
                    }
                }
            }
        }

        return loadedBatchValues
    }

    // ---- Private ----
    // ---- Private ----

    private suspend fun fetchBatch(batchNumber: Int): List<Pair<T, U>> {
        // Get next chunk from the list
        val ids = listChunks?.getOrNull(batchNumber)
            ?: throw Exception("The provided batch number does not exist.")
        var error: String? = null

        val temp = mutableListOf<Pair<T, U>>()
        ids.map { id ->
            /*
            Fetch data simultaneously for all the ids in the chunk, try each of them 5 times.

            If even one of them fails 5+ times, error is thrown and the batch is handled as not
            fetched.

            Could be changed to accepting other values from batch, marking the failed to be
            fetched again.
            */
            scope.launch(Dispatchers.IO) {
                try {
                    retryIO {
                        val res = fetchDataForId(id)
                        temp.add(Pair(id, res))
                    }
                } catch (e: Exception) {
                    println("Exception: $e")
                    error = e.message.toString()
                }
            }
        }.joinAll()

        if (error != null) {
            throw Exception(error)
        }

        return temp
    }

    private suspend fun reloadIdList() {
        try {
            idList = listOf()
            listChunks = listOf()
            idCount.value = Response.loading(null)

            idList = fetchIdList()
            listChunks = idList!!.chunked(batchSize)
            idCount.value = Response.success(idList!!.size)
        } catch (e: Exception) {
            Log.d(TAG, "reloadIdList: Error loading ids: $e")
            idCount.value = Response.error(message = "Ids could not be loaded: $e")
        }
    }

    private fun clearBatches() {
        val keys = batches.keys.map { it }
        keys.forEach {
            batches.remove(it)
        }
        dataFlow.value = Response.loading(listOf())
    }

    private fun allChunksLoaded(): Boolean = batches.containsKey(listChunks?.lastIndex)

    private fun setLoading(batchNumber: Int) {
        batchesLoading.value.add(batchNumber)
    }

    private fun setNotLoading(batchNumber: Int) {
        batchesLoading.value.remove(batchNumber)
    }
}