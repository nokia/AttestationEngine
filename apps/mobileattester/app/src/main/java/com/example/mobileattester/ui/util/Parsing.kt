package com.example.mobileattester.ui.util

import kotlin.math.roundToLong

// Removes https & other excess from the url
// baseUrl: http://192.168.0.1:4050/
// result: 192.168.0.1:4050
fun parseBaseUrl(baseUrl : String) : String
{
    var nUrl = baseUrl.dropWhile { c -> c != '/' && !c.isDigit() }
    if(!nUrl.first().isLetterOrDigit())
        nUrl = nUrl.dropWhile { c -> c == '/' || c.isWhitespace()  }
    return nUrl.takeWhile { c -> c != '/' }
}

// Parses a timestamp into
fun parseTimestamp(verifiedAt : String) : Timestamp?
{
    val parsedVerifiedAt: Long? = verifiedAt.toDoubleOrNull()?.roundToLong()
    return parsedVerifiedAt?.let { Timestamp(it) }
}

class Timestamp(val time: Long) : Comparable<Long>
{
    fun timeSince() : Timestamp
    {
        val timeInSeconds: Long = System.currentTimeMillis() / 1000
        return Timestamp(timeInSeconds.minus(time))
    }

    fun toHours() : Int = time.div(3600).toInt() // total seconds / 3600 seconds = hours

    override fun compareTo(other: Long): Int = time.compareTo(other = other)

}