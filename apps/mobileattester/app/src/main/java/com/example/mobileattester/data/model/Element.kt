package com.example.mobileattester.data.model

import com.google.gson.annotations.SerializedName

/**
 * Represents an Element in the application.
 * This interface should contain all the data we are interested to see in the UI.
 */
interface Element {
    val name: String
    val endpoint: String
    val description: String?
    val types: List<String>
}

/**
 * A10 Element impl.
 */
data class ElementA10(
    override val name: String,
    override val endpoint: String,
    override val description: String?,
    @SerializedName("type") override val types: List<String>,
) : Element