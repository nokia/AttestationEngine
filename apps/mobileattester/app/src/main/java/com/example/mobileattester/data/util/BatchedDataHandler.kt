package com.example.mobileattester.data.util

import android.util.Log
import com.example.mobileattester.data.model.Element
import com.example.mobileattester.data.network.retryIO
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow

const val TIMEOUT = 10_000L
const val TAG = "BatchedDataHandler"

/**
 * Class that can act as a data provider for Batched data handler
 * T - Id type
 * U - Data type
 */
interface BatchedDataProvider<T, U> {
    suspend fun getIdList(): List<T>
    suspend fun getDataForId(id: T): U
}

class ElementDataHandler<T,U>(dataProvider: BatchedDataProvider<T, U>, batchSize: Int) :
    BatchedDataHandler<T, U>(dataProvider, batchSize) {}

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
    private val dataProvider: BatchedDataProvider<T, U>,
    private val batchSize: Int,
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
    private val batchesLoading = mutableSetOf<Int>()

    /** Data from batches in a list */
    val dataFlow = MutableStateFlow(listOf<U>())

    /** Any of the batches currently loading? */
    val isLoading: MutableStateFlow<Boolean> = MutableStateFlow(batchesLoading.size != 0)

    /** Is the data currently refreshing (idList / batches) */
    val isRefreshing: MutableStateFlow<Boolean> = _refreshingData
    val idCount: MutableStateFlow<Int> = MutableStateFlow(0)

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
        val isLoading = batchesLoading.contains(batchNumber)
        if (isLoading || allChunksLoaded()) {
            return
        }
        setLoading(batchNumber)

        try {
            scope.launch {
                batches[batchNumber] = fetchBatch(batchNumber)
                dataFlow.value = dataAsList()
            }
            Log.d(TAG, "fetchNextBatch: Successfully loaded batch $batchNumber")
        } catch (e: Exception) {
            Log.d(TAG, "failed to fetch next batch[$batchNumber]: $e")
        } finally {
            setNotLoading(batchNumber)
        }
    }

    /**
     * Refresh data
     * @param hardReset Set to true to also fetch the ids again
     */
    fun refreshData(hardReset: Boolean = false) {
        if (_refreshingData.value) return

        _refreshingData.value = true
        clearBatches()

        scope.launch(Dispatchers.IO) {
            retryIO(5) {
                if (hardReset) {
                    reloadIdList()
                }
            }
            fetchNextBatch()

        }

        scope.launch {
            delay(500) // TODO Remove fake delay
            _refreshingData.value = false
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

    // ---- Private ----
    // ---- Private ----

    private suspend fun fetchBatch(batchNumber: Int): List<Pair<T, U>> {
        Log.d(TAG, "fetchBatch: Fetching batch $batchNumber")
        return scope.async(Dispatchers.IO) {
            // Get next batches chunk from the list
            val ids = listChunks?.getOrNull(batchNumber)
            val temp = mutableListOf<Pair<T, U>>()

            Log.d(TAG, "fetchBatch: IDS TO GET: $ids")

            val jobs = ids?.map { id ->

                /*
                Fetch data simultaneously for all the ids in the chunk, try each of them 5 times.

                If even one of them fails 5+ times, error is thrown and the batch is handled as not
                fetched.
                */
                launch(Dispatchers.IO) {
                    retryIO(times = 5, catchErrors = false) {
                        withTimeout(TIMEOUT) {
                            val res = dataProvider.getDataForId(id)
                            res.let {
                                temp.add(Pair(id, it))
                            }
                        }
                    }
                }
            }

            jobs?.joinAll()
            Log.d(TAG, "fetchBatch: Batch $batchNumber fetched, length: ${temp.size}")
            return@async temp
        }.await()
    }

    private suspend fun reloadIdList() {
        try {
            retryIO {
                idList = listOf()
                listChunks = listOf()
                idCount.value = 0

                idList = dataProvider.getIdList()
                listChunks = idList!!.chunked(batchSize)
                idCount.value = idList!!.size
            }
        } catch (e: Exception) {
            Log.d(TAG, "reloadIdList: Error loading ids: $e")
        }
    }

    private fun clearBatches() {
        val keys = batches.keys.map { it }
        keys.forEach {
            batches.remove(it)
        }
        dataFlow.value = listOf()
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
                    if(it.second is Element) {
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

        println("DataAsList size: ${loadedBatchValues.size}")
        return loadedBatchValues
    }

    private fun allChunksLoaded(): Boolean = batches.containsKey(listChunks?.lastIndex)

    private fun setLoading(batchNumber: Int) {
        batchesLoading.add(batchNumber)
        isLoading.value = batchesLoading.size != 0
    }

    private fun setNotLoading(batchNumber: Int) {
        batchesLoading.remove(batchNumber)
        isLoading.value = batchesLoading.size != 0
    }
}