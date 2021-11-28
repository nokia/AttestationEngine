package com.example.mobileattester.data.model

import android.location.Location
import com.example.mobileattester.data.util.abs.DataFilter
import com.example.mobileattester.data.util.abs.Filterable
import com.google.gson.*
import com.google.gson.annotations.SerializedName
import java.lang.reflect.Type

import com.google.gson.JsonObject
import org.osmdroid.util.GeoPoint


/**
 * Represents an Element in the application.
 * This should contain all the data we are interested to see in the UI.
 */
data class Element(
    val itemid: String,
    val name: String,
    val endpoint: String,
    @SerializedName("type") val types: List<String>,
    val protocol: String,
    val description: String?,
    val location: List<String>?,
    @Transient var results: List<ElementResult>,
) : Filterable {

    override fun filter(f: DataFilter): Boolean {
        return matchFields(f.keywords)
    }

    fun geoPoint(): GeoPoint? {
        val lat = location?.get(0)
        val long = location?.get(1)

        if (lat == null || long == null) {
            return null
        }

        return GeoPoint(lat.toDouble(), long.toDouble())
    }

    fun cloneWithNewLocation(location: Location): Element {
        return Element(
            itemid,
            name,
            endpoint,
            types,
            protocol,
            description,
            listOf(location.latitude.toString(), location.longitude.toString()),
            results
        )
    }

    /**
     * Returns true if all the strings in the list
     * are matched in one of the searched fields of this instance.
     */
    private fun matchFields(l: List<String>): Boolean {
        return l.all { s ->
            name.lowercase().contains(s)
                    || endpoint.lowercase().contains(s)
                    || types.find { it.lowercase().contains(s) } != null
                    || protocol.lowercase().contains(s)
                    || description?.lowercase()?.contains(s) == true
        }
    }
}

fun emptyElement(): Element {
    return Element(
        "", "", "", listOf(), "", "", listOf(), listOf()
    )
}