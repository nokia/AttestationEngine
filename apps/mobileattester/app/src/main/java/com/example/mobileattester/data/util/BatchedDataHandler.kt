package com.example.mobileattester.data.util

import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import com.example.mobileattester.data.network.Response
import com.example.mobileattester.data.network.Status
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/**
 *  Data fetching in batches.
 */
class BatchedDataHandler<T, U>(
    private val identifierList: List<T>,
    private val fetchData: suspend (id: T) -> Response<U>,
    private val batchSize: Int
) {
    private val listChunks = identifierList.chunked(batchSize)

    private val batches = mutableMapOf<Int, List<Pair<T, U>>>()
    private val batchesLoading = mutableSetOf<Int>()

    private val job = Job()
    private val coroutineScope = CoroutineScope(job)

    /**
     * Call to start fetching for the next batch, which does not yet contain data.
     * Returns null when there are no more batches to get.
     */
    fun fetchNextBatch(): LiveData<Status> {
        val nextBatch = (batches.keys.maxOrNull() ?: -1) + 1

        if (batchesLoading.contains(nextBatch)) {
            return liveData {
                Status.ERROR
            }
        }

        batchesLoading.add(nextBatch)

        return liveData(Dispatchers.IO) {
            emit(Status.LOADING)

            try {
                val ids = listChunks.getOrNull(nextBatch)?.iterator()
                val temp = mutableListOf<Pair<T, U>>()

                while (ids?.hasNext() == true) {
                    coroutineScope.launch(Dispatchers.IO) {
                        val id = ids.next()
                        val res = fetchData(id)
                        res.data?.let {
                            temp.add(Pair(id, it))
                        } ?: throw Exception("Error getting data in a batch for id : $id")
                    }
                }

                job.join()

                batches[nextBatch] = temp
            } catch (e: Exception) {
                println(e)
            } finally {
                batchesLoading.remove(nextBatch)
            }
        }
    }


    fun getDataForBatch(batchNumber: Int): List<U> {
        return batches[batchNumber]?.map {
            it.second
        } ?: listOf()
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

    fun getTotalBatchCount(): Int = listChunks.size
}