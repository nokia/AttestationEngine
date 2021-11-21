package com.example.mobileattester.data.network

import com.google.gson.JsonElement
import com.google.gson.annotations.SerializedName
import org.json.JSONArray
import org.json.JSONObject

data class AttestationParams(
    val eid: String,
    val pid: String,
    val cps: JSONObject = JSONObject(),
)

data class VerifyParams(
    val cid: String,
    val rule: List<Any>,
)
