package com.example.mobileattester.data.util

import android.util.Log
import com.example.mobileattester.data.model.ElementResult
import com.example.mobileattester.data.network.AttestationDataHandler
import com.example.mobileattester.ui.util.Timestamp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

interface OverviewProvider {
    /** Lists of all results  */
    val results: StateFlow<Map<Timestamp, List<ElementResult>>>

    fun addOverview(timestamp: Timestamp?)
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
    private val _results = MutableStateFlow(mutableMapOf<Timestamp, List<ElementResult>>())

    override val results: StateFlow<Map<Timestamp, List<ElementResult>>> = _results

    override fun addOverview(timestamp: Timestamp?) {
        scope.launch {
            try {
                val results = dataHandler.getLatestResults(timestamp?.time?.toFloat())
                val cp = _results.value.toMutableMap()
//                _results.value =

            } catch (e: Exception) {
                Log.e(TAG, "addOverview: $e")
            }
        }
    }
}

