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

class ElementDataHandler(dataProvider: BatchedDataProvider<String, Element>, batchSize: Int) :
    BatchedDataHandler<String, Element>(dataProvider, batchSize) {}

/**
 *  Data fetching in batches.
 *  T - Id type
 *  U - Data type
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
        println("FetchNextBatch called, nextBatch: $batchNumber")

        val isLoading = batchesLoading.contains(batchNumber)
        if (isLoading || allChunksLoaded()) {
            return
        }
        setLoading(batchNumber)

        try {
            // Get next batches chunk from the list
            val ids = listChunks?.getOrNull(batchNumber)
            val temp = mutableListOf<Pair<T, U>>()

            // Kotlin gives warning here otherwise even when blocking is fine
            @Suppress("BlockingMethodInNonBlockingContext") runBlocking {

                // Fetch data for every id in the chunk. If one fails, whole chunk is disregarded
                val jobs = ids?.map { id ->
                    launch(Dispatchers.IO) {
                        retryIO(catchErrors = true) { // Errors should be handled here
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
            }

            batches[batchNumber] = temp
            dataFlow.value = dataAsList()

            Log.d(TAG, "fetchNextBatch: Successfully loaded batch $batchNumber")
        } catch (e: Exception) {
            Log.d(TAG, "failed to fetch next batch[$batchNumber]: $e")
        } finally {
            setNotLoading(batchNumber)
        }
    }

    /**
     * Refresh data, if hardReset true also reloads the id list.
     * @param hardReset Set to true to fetch also the ids again
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

        _refreshingData.value = false
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

    private suspend fun reloadIdList() {
        idList = listOf()
        listChunks = listOf()
        idCount.value = 0

        idList = dataProvider.getIdList()
        listChunks = idList?.chunked(batchSize)
        idCount.value = idList?.size ?: 0
    }

    private fun clearBatches() {
        val keys = batches.keys.map { it }
        keys.forEach {
            batches.remove(it)
        }
    }

    private fun dataAsList(): List<U> {
        val loadedBatchValues = mutableListOf<U>()

        batches.entries.forEach { entry ->
            val values = entry.value.map {
                it.second
            }
            loadedBatchValues.addAll(values)
        }

        println("DataAsList: ${loadedBatchValues.size}")

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