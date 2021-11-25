package com.example.mobileattester.ui.pages

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.view.MotionEvent
import androidx.appcompat.content.res.AppCompatResources
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.preference.PreferenceManager.getDefaultSharedPreferences
import com.example.mobileattester.R
import com.example.mobileattester.ui.util.PermissionDeniedRequestSettings
import com.example.mobileattester.ui.util.PermissionsRationale
import com.example.mobileattester.ui.util.Screen
import com.example.mobileattester.ui.viewmodel.AttestationViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionsRequired
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import org.osmdroid.config.Configuration.getInstance
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

const val ARG_MARKER_POSITIONS =
    "marker_positions" // listOf<GeoPoint>(), will get new GeoPoint if null or not given
const val ARG_MARKER_NAMES = "marker_names" // listOf<String>()

private val LOCATION_PERMISSIONS = listOf(
    Manifest.permission.ACCESS_FINE_LOCATION,
    Manifest.permission.READ_EXTERNAL_STORAGE,
    Manifest.permission.WRITE_EXTERNAL_STORAGE
)

private lateinit var map: MapView

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MapWrapper(
    navController: NavController,
    viewModel: AttestationViewModel,
) {

    val context = LocalContext.current
    val permissionState = rememberMultiplePermissionsState(permissions = LOCATION_PERMISSIONS)

    // Required by OsmDroid
    getInstance().load(context, getDefaultSharedPreferences(context))

    PermissionsRequired(multiplePermissionsState = permissionState,
        permissionsNotGrantedContent = {
            PermissionsRationale("Please grant location and storage permissions to access the map.")
            { permissionState.launchMultiplePermissionRequest() }
        },
        permissionsNotAvailableContent = {
            PermissionDeniedRequestSettings(text = "Requested permissions were denied. Missing permissions must be granted manually from settings.") {
                try {
                    permissionState.revokedPermissions.first().launchPermissionRequest()
                    val intent = Intent(
                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                        Uri.parse("package:" + context.packageName)
                    )
                    ContextCompat.startActivity(context, intent, null)
                } catch (err: Error) {
                    navController.navigate(Screen.Home.route)
                }
            }
        }) {
        AndroidView(factory = {
            MapView(it).also { m -> map = m; initializeMap(context, navController) }
        })
    }

}

private fun addServerMarker(pos: GeoPoint, ctx: Context): Marker {
    // Position
    val marker = Marker(map)
    marker.icon = AppCompatResources.getDrawable(ctx, R.drawable.ic_baseline_server)
    marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
    marker.position = pos
    map.overlays.add(marker)
    return marker
}

@SuppressLint("ClickableViewAccessibility")
private fun initializeMap(context: Context, navController: NavController) {
    // Load ideal tilemap for showing simple building layouts.
    map.setTileSource(TileSourceFactory.WIKIMEDIA)
    map.isTilesScaledToDpi = true
    map.setMultiTouchControls(true)
    map.controller.setZoom(3.0)

    val markerPositions =
        navController.currentBackStackEntry?.arguments?.getParcelableArrayList<GeoPoint>(
            ARG_MARKER_POSITIONS
        )

    val markerNames =
        navController.currentBackStackEntry?.arguments?.getStringArrayList(
            ARG_MARKER_NAMES
        )

    if (markerPositions != null)
        markerPositions.forEachIndexed { i, it ->
            addServerMarker(it, context).also { it.title = markerNames?.get(i) ?: "" }
        }
    else {
        val serverMarker = addServerMarker(GeoPoint(map.mapCenter),context)
        serverMarker.setOnMarkerClickListener { _, _ ->  false}
        map.setOnTouchListener { _, e ->
            run {
                if (e.action == MotionEvent.ACTION_MOVE)
                    serverMarker.position = GeoPoint(map.mapCenter)
                false
            }
        }
    }
}