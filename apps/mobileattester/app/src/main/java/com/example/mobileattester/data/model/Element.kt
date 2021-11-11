package com.example.mobileattester.data.model

/**
 * Represents an Element in the application.
 * This interface should contain all the data we are interested to see in the UI.
 */
data class Element(
    val itemid: String,
    val name: String,
    val endpoint: String,
    val types: List<String>,
    val protocol: String,
    val description: String?
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