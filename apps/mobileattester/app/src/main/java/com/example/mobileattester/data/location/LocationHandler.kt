package com.example.mobileattester.data.location

import android.annotation.SuppressLint
import android.content.Context
import android.content.Context.LOCATION_SERVICE
import android.location.Location
import android.location.LocationManager
import androidx.core.location.LocationListenerCompat
import com.example.mobileattester.ui.util.locationProvider

// Time passed to trigger a location update (seconds)
const val LOCATION_UPDATE_FREQ = 0L

// Distance travelled to trigger a location update (meters)
const val LOCATION_DISTANCE_FREQ = 0f

// Implement in class to register it for location updates
interface LocationUpdateListener {
    fun onLocationUpdate(location: Location)
}

/**
 * Listens to location updates from system and forwards them to the class
 * which has been registered to get the location updates.
 *
 * Note: Does not check any permissions.
 */
class LocationHandler(
    val ctx: Context,
) : LocationListenerCompat {

    // Class which acts as a delegate for the location updates
    private var locationUpdateListener: LocationUpdateListener? = null
    private var locationManager: LocationManager = ctx.getSystemService(LOCATION_SERVICE) as
            LocationManager

    override fun onLocationChanged(location: Location) {
        println("@@ LocationUpdate")
        locationUpdateListener?.onLocationUpdate(location)
    }

    fun registerLocationUpdateListener(listener: LocationUpdateListener) {
        println("@@ Registered listener")
        this.locationUpdateListener = listener
    }
    @SuppressLint("MissingPermission")
    fun startLocationUpdates() {
        println("@@ Request location")
        if (locationProvider(ctx) != null) // Location supported by machine
            locationManager.requestLocationUpdates(locationProvider(ctx)!!, 1000, 15f, this)

    }

    fun stopLocationUpdates() {
        locationManager.removeUpdates(this)
    }
}