package com.example.mobileattester.data.model

import com.google.gson.annotations.SerializedName

/**
 * Represents an Element in the application.
 * This should contain all the data we are interested to see in the UI.
 */
data class ElementResult(
    @SerializedName("_id") val resultId: String,
    // missing "additional: []"
    val claimID: String,
    val elementID: String,
    val ev: ExpectedValueElementResult,
    val itemid: String,
    val message: String,
    val policyID: String,
    val result: Int,
    val ruleName: String,
    val ruleParameters: HashMap<String, String>,
    val verifiedAt: String,
)
