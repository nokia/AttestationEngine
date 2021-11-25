package com.example.mobileattester.data.model

import com.example.mobileattester.data.util.abs.DataFilter
import com.example.mobileattester.data.util.abs.Filterable
import com.example.mobileattester.ui.util.Timestamp
import com.google.gson.annotations.SerializedName


/**
 * Represents an Element in the application.
 * This should contain all the data we are interested to see in the UI.
 */
data class Element(
    val itemid: String,
    val name: String,
    val endpoint: String,
    @SerializedName("type") val types: List<String>,
    val protocol: String,
    val description: String?,
    @Transient var results: List<ElementResult>,
) : Filterable {

    override fun filter(f: DataFilter): Boolean {
        return matchFields(f.keywords)
    }

    /**
     * Returns true if all the strings in the list
     * are matched in one of the searched fields of this instance.
     */
    private fun matchFields(l: List<String>): Boolean {
        return l.all { s ->
            name.lowercase().contains(s)
                    || endpoint.lowercase().contains(s)
                    || types.find { it.lowercase().contains(s) } != null
                    || protocol.lowercase().contains(s)
                    || description?.lowercase()?.contains(s) == true
        }
    }
}

fun emptyElement(): Element {
    return Element(
        "", "", "", listOf(), "", "", listOf()
    )
}