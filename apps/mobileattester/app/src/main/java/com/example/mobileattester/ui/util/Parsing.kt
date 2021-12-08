package com.example.mobileattester.ui.util

import android.net.InetAddresses
import android.os.Build
import android.util.Patterns
import java.net.URI
import java.text.SimpleDateFormat
import kotlin.math.roundToLong

/* ADDRESS */

private val defaultPort =
    Preferences.defaultConfig.first().takeLastWhile { it.isDigit() }.ifEmpty { "8520" }.toUInt()

// Removes https & other excess from the url
// addMissing will provide sensible defaults if url has missing data
// baseUrl: http://192.168.0.1:4050/
// result: 192.168.0.1:4050

fun parseBaseUrl(url: String, addMissing: Boolean = true): String? {

        val addr: URI = try {
            URI.create(url)
        } catch (err: Exception) {
            return if(url.take("http".length) != "http")
                parseBaseUrl("http://$url")
            else
                null
        }


    // 192.168.0.1:8520 OR domain.com:8520
    return if (addr.host != null && addr.host.isNotEmpty()) {
        if(addr.port >= 0 && validPort(addr.port.toString()))
            "${addr.host}:${addr.port}"
        else if(addMissing)
            "${addr.host}:${defaultPort}"
        else addr.host
    } else if(url.take("http".length) != "http")
        return parseBaseUrl("http://$url")
    else
        null

}

// Validates ip address.
// A port will invalidate the ip address unless provided
fun validIpAddress(url: String, port: String? = null): Boolean =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
        InetAddresses.isNumericAddress(url.dropLast(port?.length?.plus(1) ?: 0))
    else
        Patterns.IP_ADDRESS.matcher(url.dropLast(port?.length?.plus(1) ?: 0)).matches()

// Validates port
fun validPort(port: String?) = port?.toUShortOrNull() != null


/* TIME */
typealias Timeframe = Pair<Timestamp, Timestamp>

class Timestamp(val time: Long) : Comparable<Long> {

    // For development
    override fun toString(): String {
        // Create a DateFormatter object for displaying date in specified format.
        // Create a DateFormatter object for displaying date in specified format.
        val formatter = SimpleDateFormat("dd/MM/yyyy-hh:mm:ss")

        // Create a calendar object that will convert the date and time value in milliseconds to date.
        return formatter.format(time * 1000)
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

        fun now(): Timestamp = Timestamp(System.currentTimeMillis() / 1000)
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
    fun minus(value: Long) = Timestamp(this.time - value)
    fun add(value: Long) = Timestamp(this.time + value)
    fun div(value: Long) = Timestamp(this.time / value)
    fun mul(value: Long) = Timestamp(this.time * value)
}