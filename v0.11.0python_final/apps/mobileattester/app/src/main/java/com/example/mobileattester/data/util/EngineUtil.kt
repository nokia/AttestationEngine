package com.example.mobileattester.data.util

import android.util.Log
import com.example.mobileattester.data.model.ElementResult
import com.example.mobileattester.data.model.Spec
import com.example.mobileattester.data.network.AttestationDataHandler
import com.example.mobileattester.data.util.abs.NotificationSubscriber
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class BaseUrlChanged(val url: String)

interface EngineInfo : NotificationSubscriber {
    val spec: StateFlow<Spec>
}

private const val TAG = "EngineUtil"

/**
 * Implementation, which reads the results from fetched elements.
 */
class EngineInfoImpl(
    private val dataHandler: AttestationDataHandler,
) : EngineInfo {
    private val job = Job()
    private val scope = CoroutineScope(job)

    override val spec: MutableStateFlow<Spec> = MutableStateFlow(Spec())

    override fun <T> notify(data: T) {
        when (data) {
            is BaseUrlChanged -> scope.launch {
                try {
                    spec.value = dataHandler.getSpec()
                } catch (e: Exception) {
                    Log.e(TAG, "notify: $e")
                }
            }
        }
    }
}