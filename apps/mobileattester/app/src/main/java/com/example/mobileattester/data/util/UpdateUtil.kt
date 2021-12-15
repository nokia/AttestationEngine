package com.example.mobileattester.data.util

import com.example.mobileattester.data.model.Element
import com.example.mobileattester.data.network.AttestationDataHandler
import com.example.mobileattester.data.network.Response
import com.example.mobileattester.data.util.abs.AsyncRunner
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * Class to update data in backend with.
 * Uses AsyncRunner to inform interested classes about updates in the
 * backend data sent from this client.
 */
class UpdateUtil(
    private val runner: AsyncRunner,
    private val adh: AttestationDataHandler,
) {
    val elementUpdateFlow: MutableStateFlow<Response<String>> = MutableStateFlow(Response.idle())

    fun updateElement(element: Element) {
        runner.run(
            element,
            function = {
                elementUpdateFlow.value = Response.loading()
                when (adh.updateElement(it)) {
                    AttestationDataHandler.UPDATE_ERROR -> {
                        elementUpdateFlow.value =
                            Response.error(message = "Failed to update element")
                        false
                    }
                    else -> {
                        elementUpdateFlow.value = Response.success("Success")
                        true
                    }
                }
            },
            onException = {
                elementUpdateFlow.value =
                    Response.error(null, "Error updating element ${it.message.toString()}")
            },
        )
    }
}