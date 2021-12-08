package com.example.mobileattester.data.util

import com.example.mobileattester.data.model.ElementResult
import com.example.mobileattester.data.network.AttestationDataHandler
import com.example.mobileattester.ui.util.Timestamp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

interface OverviewProvider {
    /** Lists of all results  */
    fun getOverview(hoursSince: Int?): MutableStateFlow<List<ElementResult>>
    fun refreshOverview(overview: Int?)
    fun removeOverview(hoursSince: Int?): MutableStateFlow<List<ElementResult>>?
}

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
            if (overview == null)
                for (hoursSince in results.keys) {
                    setOverview(hoursSince)
                }
            else
                setOverview(overview)
        }
    }

    override fun getOverview(hoursSince: Int?): MutableStateFlow<List<ElementResult>> =
        results[hoursSince].let {
            if (it == null) {
                results[hoursSince] = MutableStateFlow(listOf())

                scope.launch {
                    setOverview(hoursSince)
                }
            }

            return results[hoursSince]!!
        }

    override fun removeOverview(hoursSince: Int?) = results.remove(hoursSince)

    private suspend fun setOverview(overview: Int?) {
        results[overview]!!.value =
            dataHandler.getLatestResults(
                Timestamp.now()
                    .minus(overview?.let { 3600L * overview })?.time?.toFloat())
    }
}