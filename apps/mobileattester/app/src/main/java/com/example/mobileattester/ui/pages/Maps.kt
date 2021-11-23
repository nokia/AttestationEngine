package com.example.mobileattester.ui.pages

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.preference.PreferenceManager
import android.provider.Settings
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.example.mobileattester.ui.util.PermissionDeniedRequestSettings
import com.example.mobileattester.ui.util.PermissionsRationale
import com.example.mobileattester.ui.util.Screen
import com.example.mobileattester.ui.viewmodel.AttestationViewModel
import com.google.accompanist.permissions.*
import org.osmdroid.views.MapView
import org.osmdroid.config.Configuration.*


private val LOCATION_PERMISSIONS = listOf(
    Manifest.permission.ACCESS_FINE_LOCATION,
    Manifest.permission.READ_EXTERNAL_STORAGE,
    Manifest.permission.WRITE_EXTERNAL_STORAGE
)

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MapWrapper(
    navController: NavController,
    viewModel: AttestationViewModel,
) {

    val ctx = LocalContext.current
    val permissionState = rememberMultiplePermissionsState(permissions = LOCATION_PERMISSIONS)

    // Required by OsmDroid
    getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));

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
                        Uri.parse("package:" + ctx.packageName)
                    )
                    ContextCompat.startActivity(ctx, intent, null)
                } catch (err: Error) {
                    navController.navigate(Screen.Home.route)
                }
            }
        }) {
        MapContent()
    }
}

@Composable
private fun MapContent() {
    AndroidView(factory = {
        MapView(it)
    })
}