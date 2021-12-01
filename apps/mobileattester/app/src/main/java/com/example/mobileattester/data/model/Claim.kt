package com.example.mobileattester.data.model

import com.example.mobileattester.ui.util.Timestamp
import com.google.gson.JsonObject

data class Claim(
    val itemid: String,
    val header: JsonObject,
    val payload: Payload,
) {
    /**
     * @return Pair(Time requested, Time received)
     */
    fun getTimestamps(): Pair<Timestamp?, Timestamp?> {
        val requested = header["as_requested"].asString
        val received = header["as_received"].asString


        return Pair(Timestamp.fromSecondsString(requested), Timestamp.fromSecondsString(received))
    }
}

data class Payload(
    val footer: JsonObject,
    val header: JsonObject,
    val payload: JsonObject,
)
