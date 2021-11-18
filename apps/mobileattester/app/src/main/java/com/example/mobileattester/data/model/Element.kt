package com.example.mobileattester.data.model

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
    @Transient var results: List<ElementResult>
)

/**
 * A10 Element impl.
 */
//data class ElementA10(
//    override val itemid: String,
//    override val name: String,
//    override val endpoint: String,
//    override val description: String?,
//    override val protocol: String,
//    @SerializedName("type") override val types: List<String>,
//) : Element