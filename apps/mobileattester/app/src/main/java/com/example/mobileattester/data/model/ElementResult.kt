package com.example.mobileattester.data.model

import com.google.gson.annotations.SerializedName

const val CODE_RESULT_OK = 0

data class ElementResult(
    // missing "additional: []"
    val claimID: String,
    val elementID: String,
    val ev: ExpectedValueElementResult,
    val itemid: String,
    val message: String,
    val policyID: String,
    val result: Int,
    val ruleName: String,
//    val ruleParameters: HashMap<String, String>,
    val verifiedAt: String,
)
