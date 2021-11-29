package com.example.mobileattester.data.location

import android.annotation.SuppressLint
import android.content.Context
import android.content.Context.LOCATION_SERVICE
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
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
) : LocationListener {

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

    override fun onProviderDisabled(provider: String) {
    }
    override fun onProviderEnabled(provider: String) {
    }

    @SuppressLint("MissingPermission")
    fun startLocationUpdates(context: Context) {
        //somewhere e.g. in "start tracking" button click listener
        locationManager.requestLocationUpdates(
            LocationManager.GPS_PROVIDER,
            LOCATION_UPDATE_FREQ,
            LOCATION_DISTANCE_FREQ,
            this
        )
    }

    @SuppressLint("MissingPermission")
    fun requestCurrentLocation() {
        println("@@ Request single location")
        if (locationProvider(ctx)!! != null)
            locationManager.requestLocationUpdates(locationProvider(ctx)!!, 1000, 15f, this)
        else
        {
            // Location not supported by machine
        }

    }

    fun stopLocationUpdates() {
        locationManager?.removeUpdates(this)
    }
}