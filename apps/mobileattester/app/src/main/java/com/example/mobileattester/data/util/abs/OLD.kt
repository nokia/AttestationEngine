//package com.example.mobileattester.data.util.abs
//
//import android.util.Log
//import com.example.mobileattester.data.network.Response
//import com.example.mobileattester.data.network.retryIO
//import kotlinx.coroutines.*
//import kotlinx.coroutines.flow.MutableStateFlow
//import kotlinx.coroutines.flow.StateFlow
//
//const val NOTIFY_BATCH_FETCHED = "ABatchHasBeenFetched"
//private const val TAG = "BatchedDataHandler"
//
//// Typealiases for the functions that need to be provided for the Batched data handler.
//typealias FetchIdList<T> = suspend () -> List<T>
//typealias FetchIdData<T, U> = suspend (T) -> U
//
///**
// *  Data fetching in batches.
// *
// *  Gets a list of all the item ids ->
// *  Creates chunks of the ids, size of param batchSize  ->
// *  Initially fetches data for one chunk.
// *
// *  Call fetchNextBatch() whenever data for the next batch of ids is required.
// *
// *  T - Id type
// *  U - Data type
// */
//abstract class BatchedDataHandler<T, U>(
//    private val batchSize: Int,
//    private val fetchIdList: FetchIdList<T>,
//    private val fetchDataForId: FetchIdData<T, U>,
//    private val notifier: Notifier? = null,
//) : NotificationSubscriber {
//    private val job = Job()
//    private val scope = CoroutineScope(job)
//
//    private var idList: List<T>? = null
//    private var listChunks: List<List<T>>? = null
//    private var _refreshingData = MutableStateFlow(false)
//
//    /**
//     * Fetched data
//     */
//    private val batches = mutableMapOf<Int, List<Pair<T, U>>>()
//    private val batchesLoading = MutableStateFlow(mutableSetOf<Int>())
//
//    /** Data from batches in a list */
//    val dataFlow = MutableStateFlow(Response.loading(listOf<U>()))
//
//    /** Any of the batches currently loading? */
//    val isLoading: MutableStateFlow<Boolean> = MutableStateFlow(batchesLoading.value.size != 0)
//
//    /** Is the data currently refreshing (idList / batches) */
//    val isRefreshing: MutableStateFlow<Boolean> = _refreshingData
//    val idCount: MutableStateFlow<Response<Int>> = MutableStateFlow(Response.loading(null))
//
//    init {
//        scope.launch(Dispatchers.IO) {
//            reloadIdList()
//            fetchNextBatch()
//        }
//    }
//
//    /**
//     * Call to start a loop that fetches batches continuously.
//     */
//    fun startFetchLoop() {
//        scope.launch {
//            while (!allChunksLoaded()) {
//                val batchNumber = (batches.keys.maxOrNull() ?: -1) + 1
//                val isLoading = batchesLoading.value.contains(batchNumber)
//                if (!isLoading) {
//                    handleFetch(batchNumber)
//                }
//            }
//        }
//    }
//
//    fun stopFetchLoop() {
//        job.cancelChildren(CancellationException("Fetch loop stopped"))
//    }
//
//    /**
//     * Call to start fetch for the next batch.
//     */
//    fun fetchNextBatch() {
//        val batchNumber = (batches.keys.maxOrNull() ?: -1) + 1
//        val isLoading = batchesLoading.value.contains(batchNumber)
//        if (isLoading || allChunksLoaded()) {
//            return
//        }
//
//        scope.launch {
//            handleFetch(batchNumber)
//        }
//    }
//
//    /**
//     * Refresh data
//     * @param hardReset Set to true to also fetch the ids again
//     */
//    fun refreshData(hardReset: Boolean = false) {
//        if (_refreshingData.value && !hardReset) {
//            return
//        }
//
//        _refreshingData.value = true
//        clearBatches()
//        job.cancelChildren(CancellationException("Refreshing data"))
//
//        scope.launch(Dispatchers.IO) {
//            try {
//                retryIO {
//                    if (hardReset) {
//                        reloadIdList()
//                    }
//                }
//                fetchNextBatch()
//            } catch (e: Exception) {
//                Log.d(TAG, "refreshData: $e")
//            } finally {
//                scope.launch {
//                    _refreshingData.value = false
//                }
//            }
//        }
//    }
//
//    fun getDataForId(id: T): U? {
//        for (list in batches.values) {
//            val data = list.find { it.first == id }
//            data?.let {
//                return it.second
//            }
//        }
//        return null
//    }
//
//    /**
//     * Call to refresh data for a single item
//     */
//    fun refreshSingleValue(id: T) {
//        scope.launch {
//            try {
//                val updatedData = fetchDataForId(id)
//
//                for (list in batches) {
//                    list.value.forEachIndexed { index, it ->
//                        if (it.first == id) {
//                            val cp = list.value.map { it }.toMutableList()
//                            cp[index] = Pair(id, updatedData)
//
//                            batches[list.key] = cp
//                            dataFlow.value = Response.success(dataAsList())
//                            println("Successful update for id $id")
//                            return@launch
//                        }
//                    }
//                }
//            } catch (e: Exception) {
//                println("Cannot refresh single value: $e")
//            }
//        }
//    }
//
//    /**
//     * Returns all data from downloaded batches as a list
//     * and optionally filters using keywords separated by spaces if
//     * U implements Searchable.
//     *
//     * ! If a filter is provided and U does not implement Searchable, this methods will
//     * return an empty list !
//     */
//    fun dataAsList(filterAll: DataFilter? = null, filterAny: DataFilter? = null): List<U> {
//        val loadedBatchValues = mutableListOf<U>()
//
//        batches.entries.forEach { entry ->
//            // For each key-value pair in fetched data
//            entry.value.forEach loop@{
//                if (filterAll == null && filterAny == null) {
//                    loadedBatchValues.add(it.second)
//                    return@loop
//                }
//
//                when (val value = it.second) {
//                    is Filterable -> {
//                        var filtered: U? = null
//
//                        if (filterAll != null) {
//                            val matchesFilter = value.filter(filterAll)
//                            if (matchesFilter) filtered = it.second
//                        }
//
//                        if (filterAny != null) {
//                            val matchesFilter = value.filterAny(filterAny)
//                            if (matchesFilter) {
//                                if (filterAll == null) filtered = it.second
//                            } else filtered = null
//                        }
//
//                        if (filtered != null) {
//                            loadedBatchValues.add(filtered)
//                        }
//                    }
//                    else -> {
//                        // We can return here, none of the values will be Filterable
//                        return@forEach
//                    }
//                }
//            }
//        }
//
//        return loadedBatchValues
//    }
//
//    // ---- Private ----
//    // ---- Private ----
//
//    private suspend fun handleFetch(batchNumber: Int) {
//        setLoading(batchNumber)
//        try {
//            retryIO {
//                batches[batchNumber] = fetchBatch(batchNumber)
//                dataFlow.value = Response.success(dataAsList())
//            }
//        } catch (e: Exception) {
//            Log.d(TAG, "failed to fetch next batch[$batchNumber]: $e")
//            dataFlow.value = Response.error(message = "Error: ${e.message}", data = dataAsList())
//        } finally {
//            setNotLoading(batchNumber)
//            if (allChunksLoaded()) {
//                notifier?.notifyAll(NOTIFY_BATCH_FETCHED)
//            }
//        }
//    }
//
//    private suspend fun fetchBatch(batchNumber: Int): List<Pair<T, U>> {
//        // Get next chunk from the list
//        val ids = listChunks?.getOrNull(batchNumber)
//            ?: throw Exception("Requested batch number does not exist.")
//        var error: String? = null
//
//        val temp = mutableListOf<Pair<T, U>>()
//        ids.map { id ->
//            /*
//            Fetch data simultaneously for all the ids in the chunk, try each of them 5 times.
//
//            If even one of them fails 5+ times, error is thrown and the batch is handled as not
//            fetched.
//
//            Could be changed to accepting other values from batch, marking the failed to be
//            fetched again.
//            */
//            scope.launch(Dispatchers.IO) {
//                try {
//                    retryIO {
//                        val res = fetchDataForId(id)
//                        temp.add(Pair(id, res))
//                    }
//                } catch (e: Exception) {
//                    println("Exception: $e")
//                    error = e.message.toString()
//                }
//            }
//        }.joinAll()
//
//        if (error != null) {
//            throw Exception(error)
//        }
//
//        return temp
//    }
//
//    private suspend fun reloadIdList() {
//        try {
//            idList = listOf()
//            listChunks = listOf()
//            idCount.value = Response.loading(null)
//
//            idList = fetchIdList()
//            listChunks = idList!!.chunked(batchSize)
//            idCount.value = Response.success(idList!!.size)
//        } catch (e: Exception) {
//            Log.d(TAG, "reloadIdList: Error loading ids: $e")
//            idCount.value = Response.error(message = "Ids could not be loaded: $e")
//        }
//    }
//
//    private fun clearBatches() {
//        val keys = batches.keys.map { it }
//        keys.forEach {
//            batches.remove(it)
//        }
//        dataFlow.value = Response.loading(listOf())
//    }
//
//    private fun allChunksLoaded(): Boolean = batches.containsKey(listChunks?.lastIndex)
//
//    private fun setLoading(batchNumber: Int) {
//        batchesLoading.value.add(batchNumber)
//        isLoading.value = batchesLoading.value.isNotEmpty()
//    }
//
//    private fun setNotLoading(batchNumber: Int) {
//        batchesLoading.value.remove(batchNumber)
//        isLoading.value = batchesLoading.value.isNotEmpty()
//    }
//}
//
