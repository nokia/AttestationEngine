package com.example.mobileattester.data.util

import com.example.mobileattester.data.model.Element
import com.example.mobileattester.data.network.AttestationDataHandler
import com.example.mobileattester.data.util.abs.AsyncRunner

/**
 * Class to update data in backend with.
 */
class UpdateUtil(
    private val runner: AsyncRunner,
    private val adh: AttestationDataHandler,
) {

    fun updateElement(element: Element) {
        runner.run(element, {
            val res = adh.updateElement(it)
            println("UpdateElement res: $res")
            // Todo Check whether the response is id or not
            true
        })
    }
}