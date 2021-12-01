package com.example.mobileattester.data.network

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
