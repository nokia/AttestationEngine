package com.example.mobileattester.data.model

import org.json.JSONObject

interface Policy {
    val itemid: String
    val intent: String
    val parameters: JSONObject
    val type: String
    val description: String?
}

data class PolicyA10(
    override val itemid: String,
    override val intent: String,
    override val parameters: JSONObject,
    override val type: String,
    override val description: String?
) : Policy