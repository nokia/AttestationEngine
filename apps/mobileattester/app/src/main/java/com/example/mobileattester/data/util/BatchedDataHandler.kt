package com.example.mobileattester.data.util

import android.util.Log
import com.example.mobileattester.data.network.Response
import com.example.mobileattester.data.network.retryIO
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow

const val TIMEOUT = 10_000L
const val TAG = "BatchedDataHandler"

/**
 *  Data fetching in batches.
 *  T - Id type
 *  U - Data type
 */
class BatchedDataHandler<T, U>(
    private var identifierList: List<T>,
    private val batchSize: Int,
    private val fetchData: suspend (id: T) -> U,
) {
    private var listChunks = identifierList.chunked(batchSize)

    private val batches = mutableMapOf<Int, List<Pair<T, U>>>()
    private val batchesLoading = mutableSetOf<Int>()

    val isLoading: MutableStateFlow<Boolean> = MutableStateFlow(batchesLoading.size != 0)

    /**
     * Call to start fetching for the next batch.
     */
    suspend fun fetchNextBatch(): Response<List<U>> {
        val nextBatch = (batches.keys.maxOrNull() ?: -1) + 1

        val isLoading = batchesLoading.contains(nextBatch)
        if (isLoading) {
            return Response.error(null, "This batch is currently loading")
        }
        setLoading(nextBatch)

        try {
            val ids = listChunks.getOrNull(nextBatch)
            val temp = mutableListOf<Pair<T, U>>()

            // Kotlin gives warning here otherwise even when blocking is fine
            @Suppress("BlockingMethodInNonBlockingContext") runBlocking {
                val jobs = ids?.map { id ->
                    launch(Dispatchers.IO) {
                        retryIO {
                            withTimeout(TIMEOUT) {
                                val res = fetchData(id)
                                res.let {
                                    temp.add(Pair(id, it))
                                }
                            }
                        }
                    }
                }

                jobs?.joinAll()
            }
            batches[nextBatch] = temp
        } catch (e: Exception) {
            Log.d(TAG, "failed to fetch next batch[$nextBatch]: $e")
            return Response.error(null, "Failed to fetch batch[$nextBatch] data: $e")
        } finally {
            setNotLoading(nextBatch)
        }

        return Response.success(batches[nextBatch]!!.map {
            it.second
        })
    }

    fun clearBatches() {
        val keys = batches.keys.map { it }
        keys.forEach {
            batches.remove(it)
        }
    }

    fun allChunksLoaded(): Boolean {
        val loaded = batches.containsKey(listChunks.lastIndex)
        Log.d(TAG, "allChunksLoaded: $loaded")
        return loaded
    }

    fun replaceIdList(idList: List<T>) {
        this.identifierList = idList
        listChunks = identifierList.chunked(batchSize)
        clearBatches()
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

    private fun setLoading(batchNumber: Int) {
        batchesLoading.add(batchNumber)
        isLoading.value = batchesLoading.size != 0
    }

    private fun setNotLoading(batchNumber: Int) {
        batchesLoading.remove(batchNumber)
        isLoading.value = batchesLoading.size != 0
    }
}