package com.example.mobileattester.data.model

import android.location.Location
import android.util.Log
import com.example.mobileattester.data.util.abs.DataFilter
import com.example.mobileattester.data.util.abs.Filterable
import com.example.mobileattester.data.util.abs.MatchType
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.annotations.SerializedName
import org.osmdroid.util.GeoPoint
import java.lang.reflect.Type

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
    @Transient val serializationError: Boolean,
) : Filterable {

    override fun filter(f: DataFilter): Boolean {
        return when (f.matchType) {
            MatchType.MATCH_ALL -> matchAll(f.keywords)
            MatchType.MATCH_ANY -> matchAny(f.keywords)
        }
    }

    fun geoPoint(): GeoPoint? {
        val lat = location?.getOrNull(0)
        val long = location?.getOrNull(1)

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
            results,
            serializationError,
        )
    }

    /**
     * Returns true if all the strings in the list
     * are matched in one of the searched fields of this instance.
     */
    private fun matchAll(l: List<String>): Boolean {
        return l.all { s ->
            name.lowercase().contains(s) || endpoint.lowercase().contains(s) || types.find {
                it.lowercase().contains(s)
            } != null || protocol.lowercase().contains(s) || description?.lowercase()
                ?.contains(s) == true || itemid.lowercase().contains(s)
        }
    }

    /**
     * Returns true if any of the strings in the list
     * are matched in one of the searched fields of this instance.
     */
    private fun matchAny(l: List<String>): Boolean {
        return l.any { s ->
            (name.lowercase().contains(s) || endpoint.lowercase().contains(s) || types.find {
                it.lowercase().contains(s)
            } != null || protocol.lowercase().contains(s) || description?.lowercase()
                ?.contains(s) == true) || itemid.lowercase().contains(s)
        }
    }

}

fun emptyElement(): Element {
    return Element("", "", "", listOf(), "", "", listOf(), listOf(), false)
}

private const val TAG = "ElementDeserializer"

class ElementDeserializer() : JsonDeserializer<Element> {
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?,
    ): Element {
        val obj = json?.asJsonObject
        return try {
            obj!!
            Element(
                obj.get("itemid").asString,
                obj.get("name").asString,
                obj.get("endpoint").asString,
                obj.get("type").asJsonArray.map { it.asString },
                obj.get("protocol").asString,
                obj.get("description").asString,
                obj.get("location").asJsonArray.map { it.asString },
                listOf(),
                false,
            )
        } catch (e: Exception) {
            Log.d(TAG, "deserialization error: $e")

            Element(
                if (obj != null) obj.get("itemid").asString else "err",
                "----",
                "----",
                listOf(),
                "----",
                "This element seems to be in an incorrect form. ID: ${
                    obj?.get("itemid")?.toString()
                }",
                listOf(),
                listOf(),
                true,
            )
        }
    }

}