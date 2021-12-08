package com.example.mobileattester.data.util

import androidx.compose.runtime.MutableState
import com.example.mobileattester.data.model.Element
import com.example.mobileattester.data.model.ElementResult
import com.example.mobileattester.data.network.AttestationDataHandler
import com.example.mobileattester.data.network.Response
import com.example.mobileattester.data.util.abs.DataFilter
import com.example.mobileattester.data.util.abs.NotificationSubscriber
import com.example.mobileattester.ui.util.Timestamp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.CancellationException

interface OverviewProvider {
    /** Lists of all results  */
    val results: MutableMap<Timestamp?, MutableStateFlow<List<ElementResult>>>
    fun addOverview(timestamp: Timestamp?)
}

/**
 * Implementation, which reads the results from fetched elements.
 */
class OverviewProviderImpl(
    private val dataHandler: AttestationDataHandler,
): OverviewProvider {
    private val job = Job()
    private val scope = CoroutineScope(job)

    override val results: MutableMap<Timestamp?, MutableStateFlow<List<ElementResult>>> = mutableMapOf()

    private fun refreshData()
    {
        job.cancelChildren()
        scope.launch {
            for (i in results.keys) {
                results[i]?.value = dataHandler.getLatestResults(i?.time?.toFloat())
            }
        }
    }

    override fun addOverview(timestamp: Timestamp?)
    {
        if (!results.contains(timestamp))
            results[timestamp] = MutableStateFlow(listOf())

        scope.launch {
            results[timestamp]!!.value =
                dataHandler.getLatestResults(timestamp?.time?.toFloat())
        }
    }
}