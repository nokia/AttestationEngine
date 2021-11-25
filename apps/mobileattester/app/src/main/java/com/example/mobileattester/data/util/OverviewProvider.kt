package com.example.mobileattester.data.util

import com.example.mobileattester.data.model.Element
import com.example.mobileattester.data.model.ElementResult
import com.example.mobileattester.data.util.abs.DataFilter
import com.example.mobileattester.data.util.abs.NotificationSubscriber
import com.example.mobileattester.ui.util.Timestamp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.CancellationException

interface OverviewProvider : NotificationSubscriber {
    /** Map containing lists of elements, filtered by created DataFilters */
    val elementsByResults: MutableStateFlow<Map<String, List<Element>>>

    /** Add a new element list which is filtered by its results with the provided DataFilter */
    fun addFilterByResults(key: String, filter: DataFilter)
}

/**
 * Implementation, which reads the results from fetched elements.
 * TODO A result endpoint in the REST API, so that this would
 *      not need to rely on downloading all the element data first
 */
class OverviewProviderImpl(
    private val elementDataHandler: ElementDataHandler,
) : OverviewProvider {
    private val job = Job()
    private val scope = CoroutineScope(job)
    private val addedFilters: MutableMap<String, DataFilter> = mutableMapOf()

    override val elementsByResults: MutableStateFlow<Map<String, List<Element>>> =
        MutableStateFlow(mapOf())

    override fun addFilterByResults(key: String, filter: DataFilter) {
        addedFilters[key] = filter
        //updateOverviews()
    }

    override fun <T> notify(data: T) {
        // Keep overviews up-to-date
        updateOverviews()
    }

    private fun updateOverviews() {
        job.cancelChildren(CancellationException("Relaunching updates"))
        println("OVERVIEW updating ${addedFilters.size} lists")

        // Update result based elements
        scope.launch {
            val temp = mutableMapOf<String, List<Element>>()

            addedFilters.keys.map { key ->
                val filter = addedFilters[key]!!
                val elements = elementDataHandler.dataAsList()
                val filtered = mutableListOf<Element>()

                for (element in elements) {
                    val matchingResult = element.results.find {
                        it.filter(filter)
                    }

                    if (matchingResult != null) {
                        filtered.add(element)
                    }
                }
                temp[key] = filtered
            }

            val cp = elementsByResults.value.toMutableMap()
            temp.forEach {
                println("OVERVIEW: ${it.key} size :${it.value.size}")
                cp[it.key] = it.value
            }

            elementsByResults.value = cp
        }
    }

    companion object {
        const val OVERVIEW_ATTESTED_ELEMENTS = "ove_attested"
        const val OVERVIEW_ATTESTED_ELEMENTS_OK = "ove_attested_ok"
        const val OVERVIEW_ATTESTED_ELEMENTS_24H = "ove_attested_24"
        const val OVERVIEW_ATTESTED_ELEMENTS_OK_24H = "ove_attested_ok_24"
    }
}