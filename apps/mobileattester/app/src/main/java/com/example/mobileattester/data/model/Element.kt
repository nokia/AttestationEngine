package com.example.mobileattester.data.model

import com.example.mobileattester.data.util.abs.Searchable
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
) : Searchable {

    override fun filter(s: String): Boolean {
        return name.lowercase().contains(s)
                || endpoint.lowercase().contains(s)
                || types.find { it.lowercase().contains(s) } != null
                || protocol.lowercase().contains(s)
                || description?.lowercase()?.contains(s) == true
    }
}

fun emptyElement(): Element {
    return Element(
        "", "", "", listOf(), "", "", listOf()
    )
}