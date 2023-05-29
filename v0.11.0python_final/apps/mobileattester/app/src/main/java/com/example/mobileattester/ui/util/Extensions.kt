package com.example.mobileattester.ui.util

import com.example.mobileattester.data.model.ElementResult
import com.google.gson.JsonObject
import org.json.JSONObject
import kotlin.math.roundToLong

fun Collection<ElementResult>.latestResults(hours: Int = 24): Collection<ElementResult> {
    return this.takeWhile {
        val hourInSeconds = 3600
        val timeInSeconds: Long = System.currentTimeMillis() / 1000
        val verifiedAt: Long? = it.verifiedAt.toDoubleOrNull()?.roundToLong()

        (timeInSeconds.minus(verifiedAt ?: 0)) < (hourInSeconds * hours)
    }
}

fun Int.shownHoursToString(): String {
    return when {
        this % (24 * 7 * 4 * 12) == 0 // year
        -> "${this / (24 * 7 * 4 * 12)}Y"
        this % (24 * 7 * 4) == 0 // month
        -> "${this / (24 * 7 * 4)}M"
        this % (24 * 7) == 0 // week
        -> "${this / (24 * 7)}W"
        else -> "${this}H" // hour
    }
}

fun Int.hoursHWMYRounded(): Int {
    val mul = when {
        this < 24 * 7 -> 24
        this < 24 * 7 * 4 -> 24 * 7
        this < 24 * 7 * 4 * 12 -> 24 * 7 * 4
        else -> 24 * 7 * 4 * 12
    }

    return this + mul - (this % mul).also { if (it == 0) return this }
}

fun JsonObject.formatted(): String {
    return JSONObject(this.toString()).toString(4)
}