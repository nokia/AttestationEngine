package com.example.mobileattester.data.util

import android.location.Location
import com.example.mobileattester.data.location.LocationHandler
import com.example.mobileattester.data.location.LocationUpdateListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Provides "edit" functionality to a location.
 */
interface LocationEditor {
    val editStateToggled: StateFlow<Boolean>

    /** Currently set temporary location, editable */
    val currentLocation: StateFlow<Location?>

    val deviceLocation: StateFlow<Location?>

    /** Change the currentLocation */
    fun setLocation(location: Location)

    /** Reset the location to the last known location of the device */
    fun resetLocation()

    /** Request a location update which runs once */
    fun requestLocationUpdates()

    fun toggleEditState()

    fun reset()
}

/**
 * @param locationHandler A handler which provides device location updates
 */
class ElementLocationEditor(
    private val locationHandler: LocationHandler,
) : LocationEditor, LocationUpdateListener {
    private val _deviceLocation: MutableStateFlow<Location?> = MutableStateFlow(null)
    private val editableLocation: MutableStateFlow<Location?> = MutableStateFlow(null)
    private val _editStateToggled = MutableStateFlow(false)

    override val editStateToggled: StateFlow<Boolean> = _editStateToggled
    override val currentLocation: StateFlow<Location?> = editableLocation
    override val deviceLocation: StateFlow<Location?> = _deviceLocation

    init {
        locationHandler.registerLocationUpdateListener(this)
    }

    override fun onLocationUpdate(location: Location) {
        println("@@ LocationEditor location update: $location")
        _deviceLocation.value = location
    }

    override fun setLocation(location: Location) {
        editableLocation.value = location
    }

    override fun resetLocation() {
        editableLocation.value = _deviceLocation.value
    }

    override fun requestLocationUpdates() {
        locationHandler.startLocationUpdates()
    }

    override fun toggleEditState() {
        _editStateToggled.value = !_editStateToggled.value
    }

    override fun reset() {
        editableLocation.value = _deviceLocation.value
        _editStateToggled.value = false
    }
}