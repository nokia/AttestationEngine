package com.example.mobileattester.data.model

import org.json.JSONObject

data class Policy(
    val itemid: String,
    val name: String,
    val intent: String,
    //val parameters: JSONObject,
    //val type: String,
    val description: String?,
)