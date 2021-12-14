package com.example.mobileattester.data.util

import android.util.Log
import com.example.mobileattester.data.model.ElementResult
import com.example.mobileattester.data.network.AttestationDataHandler
import com.example.mobileattester.data.network.retryIO
import com.example.mobileattester.data.util.abs.NotificationSubscriber
import com.example.mobileattester.ui.util.Timestamp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

interface OverviewProvider : NotificationSubscriber {
    /** Lists of all results  */
    fun getOverview(hoursSince: Int?): MutableStateFlow<List<ElementResult>>
    fun refreshOverview(overview: Int?)
    fun removeOverview(hoursSince: Int?)
}

private const val TAG = "OverviewProvider"

/**
 * Implementation, which reads the results from fetched elements.
 */
class OverviewProviderImpl(
    private val dataHandler: AttestationDataHandler,
) : OverviewProvider {
    private val job = Job()
    private val scope = CoroutineScope(job)

    val results: MutableMap<Int?, MutableStateFlow<List<ElementResult>>> = mutableMapOf()

    override fun refreshOverview(overview: Int?) {
        job.cancelChildren()
        scope.launch {
            if (overview == null) for (hoursSince in results.keys) {
                setOverview(hoursSince)
            }
            else setOverview(overview)
        }
    }

    override fun getOverview(hoursSince: Int?): MutableStateFlow<List<ElementResult>> =
        results[hoursSince].let {
            if (it == null) {
                results[hoursSince] = MutableStateFlow(mutableListOf())

                scope.launch {
                    setOverview(hoursSince)
                }
            }

            return results[hoursSince]!!
        }

    override fun removeOverview(hoursSince: Int?) {
        results.remove(hoursSince)
    }

    override fun <T> notify(data: T) {
        when (data) {
            is ResultAcquired -> addResult(data.result) // Add to all relevant results
            else -> refreshOverview(null)
        }
    }

    private fun addResult(result: ElementResult) {
        for (hoursSince in results.keys) {
            when (hoursSince) {
                // Set the latest result or add if does not exist
                null -> {
                    val latestResults = results[null]!!.value.toMutableList()
                    val resultIndex =
                        latestResults.indexOfFirst { it.elementID == result.elementID }

                    if (resultIndex > 0) latestResults[resultIndex] = result
                    else latestResults.add(result)

                    results[null]!!.value = latestResults
                }

                // Add new result to all overviews
                else -> results[hoursSince]!!.value.toMutableList().also { it.add(result) }.toList()
            }
        }
    }

    private suspend fun setOverview(overview: Int?) {
        try {
            retryIO {
                results[overview]!!.value = dataHandler.getLatestResults(Timestamp.now()
                    .minus(overview?.let { 3600L * overview })?.time?.toFloat()).toMutableList()
            }
        } catch (e: Exception) {
            Log.e(TAG, "setOverview: $e")
        }
    }
}