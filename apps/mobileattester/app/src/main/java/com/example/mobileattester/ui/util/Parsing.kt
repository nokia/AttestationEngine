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
// baseUrl: http://192.168.0.1:4050/
// result: 192.168.0.1:4050
fun parseBaseUrl(url: String): String? {

        val addr: URI = try {
            URI.create(url)
        } catch (err: Exception) {
            val parsedPort = url.takeLastWhile { it.isDigit() && it != ':' }

            return if(validPort(parsedPort) && validIpAddress(url, port = parsedPort))
                "${url.dropLast(parsedPort.length.plus(1))}:${parsedPort}"
            else
                null
        }

    // 192.168.0.1:8520 OR domain.com:8520
    return if (addr.host != null && addr.host.isNotEmpty()) {
        if(addr.port >= 0)
            "${addr.host}:${addr.port}"
        else
            "${addr.host}:${defaultPort}"
    } else
        oldBareUrlParser(url)

}

private fun oldBareUrlParser(url : String) : String?
{
    var nUrl = url.dropLastWhile { it == '/' }
    val parsedPort = nUrl.takeLastWhile { it != ':' && it.isDigit() }
    if (parsedPort.isEmpty()) return null

    nUrl = nUrl.dropLast(parsedPort.length.plus(1))

    if(nUrl.take("www.".length) == "www.")
        nUrl.drop("www.".length)

    nUrl = nUrl.dropWhile { !it.isLetterOrDigit() }.trim()

    if(!validPort(parsedPort) || !validIpAddress(url, parsedPort)) return null

    return "$nUrl:$parsedPort"
}

fun validIpAddress(url: String, port: String? = null): Boolean =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
        InetAddresses.isNumericAddress(url.dropLast(port?.length?.plus(1) ?: 0))
    else
        Patterns.IP_ADDRESS.matcher(url.dropLast(port?.length?.plus(1) ?: 0)).matches()

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
}