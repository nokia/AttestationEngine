package com.example.mobileattester.data.util

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.view.MotionEvent
import androidx.appcompat.content.res.AppCompatResources
import com.example.mobileattester.R
import com.example.mobileattester.data.model.Element
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import java.lang.ref.WeakReference


enum class MapMode {
    SINGLE_ELEMENT, EDIT_LOCATION, ALL_ELEMENTS
}

/**
 * Provides everything for the map that the application requires
 */
class MapManager(
    private val locationEditor: LocationEditor,
) {
    private var mapView: WeakReference<MapView>? = null
    val mapMode: MutableStateFlow<MapMode?> = MutableStateFlow(null)

    init {
        locationEditor.requestLocationUpdates()
    }

    /**
     * Set the map to show a location for an element.
     * @return true if the element had a location in it. False if the element did not contain
     * a location.
     */
    fun displayElement(map: MapView, element: Element): Boolean {
        initializeMap(map, MapMode.SINGLE_ELEMENT)
        val gp = element.geoPoint() ?: run {
            return false
        }

        val m = addMarker(gp, map.context, "Element position")
        map.controller.setCenter(m.position)
        return true
    }

    /**
     * Edit a location for an element.
     * @return StateFlow with the location that is being edited. Null if location was not provided,
     * and device location could not be found.
     */
    @SuppressLint("ClickableViewAccessibility")
    fun useEditLocation(map: MapView, element: Element): StateFlow<Location?>? {
        initializeMap(map, MapMode.EDIT_LOCATION)
        clearMarkers()

        val locToEdit = if (element.geoPoint() != null) {
            println("@@ Using element location")
            geoToLoc(element.geoPoint()!!)
        } else {
            println("@@ Using device location ${locationEditor.deviceLocation.value}")
            locationEditor.deviceLocation.value
        } ?: return null

        locationEditor.setLocation(locToEdit)

        val lat = locationEditor.currentLocation.value?.latitude ?: return null
        val long = locationEditor.currentLocation.value?.longitude ?: return null
        val geoPoint = GeoPoint(lat, long)

        addMarker(geoPoint, map.context, "New element position").apply {
            setMarkerFollowScreen(this)
        }
        return locationEditor.currentLocation
    }

    fun getEditedLocation(): Location? = locationEditor.currentLocation.value

    fun centerToDevice(): GeoPoint? {
        val lat = locationEditor.deviceLocation.value?.latitude ?: return null
        val long = locationEditor.deviceLocation.value?.longitude ?: return null
        val geoPoint = GeoPoint(lat, long)
        mapView?.get()?.controller?.setCenter(geoPoint)

        // In the case we are centering when in edit mode
        if (this.mapMode.value == MapMode.EDIT_LOCATION) {
            locationEditor.setLocation(geoToLoc(geoPoint))
            clearMarkers()
            val m = mapView?.get()?.context?.let { addMarker(geoPoint, it, "New element position") }
            m?.let { setMarkerFollowScreen(it) }
        }

        return geoPoint
    }

    // ------------------------- Private --------------------------------------
    // ------------------------- Private --------------------------------------

    @SuppressLint("ClickableViewAccessibility")
    private fun setMarkerFollowScreen(marker: Marker) {
        mapView?.get()?.setOnTouchListener { _, e ->
            run {
                if (e.action == MotionEvent.ACTION_MOVE) {
                    val c = mapView?.get()?.mapCenter ?: return@run false
                    locationEditor.setLocation(geoToLoc(c as GeoPoint))
                    marker.position = GeoPoint(c)
                }
                false
            }
        }
    }

    private fun initializeMap(map: MapView, type: MapMode) {
        mapView = WeakReference(map)
        mapView?.get()?.apply {
            setTileSource(TileSourceFactory.WIKIMEDIA)
            isTilesScaledToDpi = true
            setMultiTouchControls(true)
            controller.setZoom(17.0)
        }
        clearMarkers()
        mapMode.value = type
    }

    private fun clearMarkers() {
        mapView?.get()?.overlayManager?.clear()
    }

    private fun geoToLoc(geoPoint: GeoPoint): Location {
        val location = Location("")
        val latitude: Double = geoPoint.latitude
        val longitude: Double = geoPoint.longitude

        location.latitude = latitude
        location.longitude = longitude
        return location
    }

    private fun addMarker(pos: GeoPoint?, ctx: Context, txt: String): Marker {
        // Position
        val marker = Marker(mapView?.get())
        marker.icon =
            AppCompatResources.getDrawable(ctx, R.drawable.ic_baseline_location_on_32).apply {
                this?.setTint(ctx.getColor(R.color.primary))
            }
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
        marker.title = txt
        pos?.let { marker.position = it }
        mapView?.get()?.overlays?.add(marker)
        return marker
    }
}