package com.example.mobileattester.data.util.abs

import android.util.Log
import com.example.mobileattester.data.network.Response
import com.example.mobileattester.data.network.ResponseStateManager
import com.example.mobileattester.data.network.Status
import com.example.mobileattester.data.network.retryIO
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

const val NOTIFY_BATCH_FETCHED = "ABatchHasBeenFetched"
private const val TAG = "BatchedDataHandler"

// Typealiases for the functions that need to be provided for the Batched data handler.
typealias FetchIdList<T> = suspend () -> List<T>
typealias FetchIdData<T, U> = suspend (T) -> U

//class BatchManager<T>(private val : FetchIdData){
//    private val batches = mutableSetOf<Batch<T>>()
//    private val batchStates = mutableMapOf<Int, Status>()
//
//    fun createBatch(data: List<T>) {
//        val batchNum = batches.size
//        batches.add(
//            Batch(
//                number = batchNum,
//                data = data
//            )
//        )
//        batchStates[batchNum] = Status.IDLE
//    }
//}


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
 *  Call fetchNextBatch() whenever data for the next batch of ids is required.
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
    // Ids fetched from network in here
    private val idResponseManager = ResponseStateManager<List<T>>()

    // Fetched ids are divided into batches, and the data for these ids is fetched in batches.
    private var batches: List<List<T>> = listOf()
    private var batchStates: MutableMap<Int/* Index of batch */, Status> = mutableMapOf()

    private val fetchedData: MutableMap<T, BatchElement<U>> = mutableMapOf()

    private val job = Job()
    private val scope = CoroutineScope(job)

    private val _dataFlow: MutableStateFlow<Response<List<U>>> = MutableStateFlow(Response.idle())
    private val _idCount: MutableStateFlow<Int> = MutableStateFlow(0)

    // All of the fetched data in a simple list, wrapped in a Response object
    val dataFlow: StateFlow<Response<List<U>>> = _dataFlow
    val idCount: StateFlow<Int> = _idCount

    init {
        initIds()
    }

    /** Call to fetch the next available batch in line */
    fun fetchNextBatch() = fetchFreeBatch()
    fun startFetchLoop() {}
    fun stopFetchLoop() {}

    /**
     * Refresh the contained data
     * @param hardReset Set to true to also fetch the ids again
     */
    fun refreshData(hardReset: Boolean = false) {
        cancelJobs("Refresh data called")
        if (hardReset) initIds()
    }

    fun refreshSingleValue(id: T) {
        if (fetchedData[id]?.status == Status.LOADING) {
            return
        }

        fetchedData[id] = BatchElement(
            fetchedData[id]?.data,
            Status.LOADING
        )

        scope.launch(Dispatchers.IO) {
            val success = tryFun {
                val data = fetchDataForId(id)
                fetchedData[id] = BatchElement(data, Status.SUCCESS)
            }
            if (!success) {
                fetchedData[id] = BatchElement(null, Status.ERROR)
            }
        }
    }

    fun getDataForId(id: T): U? {
        return fetchedData[id]?.data
    }

    fun dataAsList(filters: List<DataFilter>): List<U> {
        return rec(filters, dataAsList())
    }

    private fun rec(filters: List<DataFilter>, data: List<U>): List<U> {
        return when (filters.firstOrNull()) {
            null -> return data
            else -> {
                Log.d(TAG, "rec: REC ${filters.size}")
                val fs = filters.toMutableList()
                val d = filterList(fs.removeAt(0), data)
                rec(fs, d)
            }
        }
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
        if (idResponseManager.response.value.status == Status.LOADING) {
            return
        }

        scope.launch {
            // When initialized, try to fetch the ids.
            val success = tryFun { idResponseManager.setSuccess(fetchIdList()) }
            if (!success) {
                idResponseManager.setError("Error happened while fetching IDs")
                return@launch
            }
            /*
                IDs were successfully fetched, reset the batch stuff, since there might
                be new additions to the id list from before.

                Could also have logic to re-fetch data for each id, but currently we
                are holding all the data gotten for the ids before in memory, and reusing the
                data?
            */
            val ids = idResponseManager.response.value.data!!
            batches = ids.chunked(batchSize)
            batches.forEachIndexed { index, _ ->
                batchStates[index] = Status.IDLE
            }
            _idCount.value = ids.size
            fetchFreeBatch()
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

    /**
     * Call to fetch data for a batch which is not yet loading/fetched.
     */
    private fun fetchFreeBatch() {
        val batch = batchStates.firstNotNullOfOrNull {
            when (it.value) {
                Status.LOADING -> null
                Status.SUCCESS -> null
                else -> it
            }
        } ?: return

        batchStates[batch.key] = Status.LOADING

        scope.launch {
            val ids = batches[batch.key]
            batchStates[batch.key] = when (fetchData(ids)) {
                true -> Status.SUCCESS
                false -> Status.ERROR
            }
        }
    }

    /**
     * Fetches data for the provided list of ids, and sets the status/data of the id
     * based on the response.
     * @return true if nothing failed
     */
    private suspend fun fetchData(ids: List<T>): Boolean {
        var errors = false
        ids.map { id ->
            scope.launch(Dispatchers.IO) {
                val success = tryFun {
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

    private fun cancelJobs(reason: String = "Default") {
        Log.w(TAG, "cancelJobs: Cancelling jobs")
        // TODO Proper state setting for cancelled jobs (not loading anymore?)
        job.cancelChildren(CancellationException(reason))
    }
}

