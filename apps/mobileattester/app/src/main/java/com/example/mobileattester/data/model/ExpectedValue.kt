package com.example.mobileattester.data.model

import com.google.gson.annotations.SerializedName

interface ExpectedValue {
    val itemid: String
    val name: String
    val elementId: String
    val policyId: String
    val type: String
    val description: String?
}

data class ExpectedValueA10(
    override val itemid: String,
    override val name: String,
    @SerializedName("elementID") override val elementId: String,
    @SerializedName("policyID") override val policyId: String,
    override val type: String,
    override val description: String? = null
) : ExpectedValue

data class ExpectedValueElementResult(
    override val itemid: String,
    override val name: String,
    val pcrDigest: String, // Current value
    val evs: HashMap<String,String>, // Expected values, if set
    @SerializedName("elementID") override val elementId: String,
    @SerializedName("policyID") override val policyId: String,
    override val type: String,
    override val description: String? = null
) : ExpectedValue