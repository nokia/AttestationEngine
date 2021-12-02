package com.example.mobileattester.data.model

import android.util.Log
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

    /**
     * Return Pair<ElementId, ElementName> for this claim
     */
    fun getElementData(): Pair<String, String> {
        val e = header["element"].asJsonObject
        return Pair(e["itemid"].asString, e["hostname"].asString)
    }

    /**
     * Return Pair<PolicyId, PolicyName> for this claim
     */
    fun getPolicyData(): Pair<String, String> {
        val p = header["policy"].asJsonObject
        return Pair(p["itemid"].asString, p["name"].asString)
    }

    fun getQuote() = payload.getQuote()
    fun getPCRs() = payload.getPCRs()
}

data class Payload(
    val footer: JsonObject,
    val header: JsonObject,
    val payload: JsonObject,
) {
    /**
     * Get quote from this payload, if it exists
     */
    fun getQuote(): Quote? {
        return when (payload.has("quote")) {
            true -> Quote.fromPayload(payload)
            false -> null
        }
    }

    fun getPCRs(): List<PCR>? {
        return when (payload.has("pcrs")) {
            true -> PCR.listFromPayload(payload)
            false -> null
        }
    }
}

private const val TAG_PCR = "PCR"

data class PCR(
    val key: String,
    val values: Map<String, String>,
) {
    companion object {
        fun listFromPayload(payload: JsonObject): List<PCR>? {
            return try {
                val pcrs = payload["pcrs"].asJsonObject
                pcrs.keySet().map { key ->
                    val pcrObj = pcrs[key].asJsonObject

                    val values = pcrObj.keySet().map { valueKey ->
                        Pair(valueKey, pcrObj[valueKey].asString)
                    }

                    PCR(key = key, values = values.toMap())
                }
            } catch (e: Exception) {
                Log.d(TAG_PCR, "listFromPayload: error $e")
                null
            }
        }
    }
}

private const val TAG_QUOTE = "Quote"

data class Quote(
    val digest: String,
    val clock: String,
    val reset: String,
    val restart: String,
    val safe: String,
    val firmwareVersion: String,
    val extra: String,
    val magic: String,
    val type: String,
    val signer: String,
) {
    companion object {
        fun fromPayload(payload: JsonObject): Quote? {
            return try {
                val quote = payload["quote"].asJsonObject
                Quote(
                    quote["attested"].asJsonObject["quote"].asJsonObject["pcrDigest"].asString,
                    quote["clockInfo"].asJsonObject["clock"].asString,
                    quote["clockInfo"].asJsonObject["resetCount"].asString,
                    quote["clockInfo"].asJsonObject["restartCount"].asString,
                    quote["clockInfo"].asJsonObject["safe"].asString,
                    quote["firmwareVersion"].asString,
                    quote["extraData"].toString(),
                    quote["magic"].asString,
                    quote["type"].asString,
                    quote["qualifiedSigner"].asString,
                )
            } catch (e: Exception) {
                Log.e(TAG_QUOTE, "fromPayload: ERROR $e")
                null
            }
        }
    }
}
