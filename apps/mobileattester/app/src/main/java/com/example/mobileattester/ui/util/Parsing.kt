package com.example.mobileattester.ui.util

import java.sql.Time
import java.text.SimpleDateFormat
import kotlin.math.roundToLong

// Removes https & other excess from the url
// baseUrl: http://192.168.0.1:4050/
// result: 192.168.0.1:4050
fun parseBaseUrl(baseUrl: String): String {
    var nUrl = baseUrl.dropWhile { c -> c != '/' && !c.isDigit() }
    if (!nUrl.first().isLetterOrDigit())
        nUrl = nUrl.dropWhile { c -> c == '/' || c.isWhitespace() }
    return nUrl.takeWhile { c -> c != '/' }
}

typealias Timeframe = Pair<Timestamp, Timestamp>

class Timestamp(val time: Long) : Comparable<Long>
{

    // For development
    override fun toString(): String {
        // Create a DateFormatter object for displaying date in specified format.
        // Create a DateFormatter object for displaying date in specified format.
        val formatter = SimpleDateFormat("dd/MM/yyyy-hh:mm:ss")

        // Create a calendar object that will convert the date and time value in milliseconds to date.
        return formatter.format(time*1000)
    }

    companion object {
        /** Parses the server res timestamp string into Instance */
        fun fromSecondsString(str: String): Timestamp? {
            val l = str.toDoubleOrNull()?.roundToLong()

            if (l != null) {
                return Timestamp(l)
            }
            return null
        }

        fun now() : Timestamp = Timestamp(System.currentTimeMillis() / 1000)
    }

    fun timeSince(): Timestamp {
        val timeInSeconds: Long = System.currentTimeMillis() / 1000
        return Timestamp(timeInSeconds.minus(time))
    }

    /**
     * Inclusive range.
     */
    fun isBetween(start: Timestamp, end: Timestamp): Boolean {
        return time >= start.time && time <= end.time
    }

    fun toHours(): Int = time.div(3600).toInt() // total seconds / 3600 seconds = hours

    override fun compareTo(other: Long): Int = time.compareTo(other = other)
    fun minus(value: Timestamp) = Timestamp(this.time - value.time)
    fun add(value: Timestamp) = Timestamp(this.time + value.time)
    fun div(value: Timestamp) = Timestamp(this.time / value.time)
    fun mul(value: Timestamp) = Timestamp(this.time * value.time)
}