package com.example.mobileattester.ui.pages

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.preference.PreferenceManager.getDefaultSharedPreferences
import android.provider.Settings
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.example.mobileattester.data.network.Status
import com.example.mobileattester.data.util.MapMode
import com.example.mobileattester.ui.components.common.LoadingIndicator
import com.example.mobileattester.ui.util.PermissionDeniedRequestSettings
import com.example.mobileattester.ui.util.PermissionsRationale
import com.example.mobileattester.ui.util.Screen
import com.example.mobileattester.ui.viewmodel.AttestationViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionsRequired
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import org.osmdroid.config.Configuration.getInstance
import org.osmdroid.views.MapView

/**  */
const val ARG_MAP_SINGLE_ELEMENT_ID = "argMapSingleElement"

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
    val context = LocalContext.current
    val permissionState = rememberMultiplePermissionsState(permissions = LOCATION_PERMISSIONS)
    val elementId = navController.currentBackStackEntry?.arguments?.getString(
        ARG_MAP_SINGLE_ELEMENT_ID
    )
    val element = viewModel.getElementFromCache(elementId ?: "")

    val map = remember {
        MapView(context).also {
            setup(navController, viewModel, it)
        }
    }

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
        Box(contentAlignment = Alignment.BottomStart) {
            AndroidView(
                factory = {
                    map
                },
            )
            AdditionalUI(
                viewModel = viewModel,
                onEditLocation = {
                    println("onEditLocation")
                    if (element != null) {
                        println("onEditLocation: Not Null")
                        viewModel.useMapManager().useEditLocation(map, element)
                    }
                },
                onSaveNewLocation = {
                    println("onSaveNewLocation")
                    val editedLocation = viewModel.useMapManager().getEditedLocation()
                    if (editedLocation != null) {
                        println("onSaveNewLocation: Not null")
                        val e =
                            element?.cloneWithNewLocation(editedLocation) ?: return@AdditionalUI
                        println("onSaveNewLocation: Sending update")
                        viewModel.useUpdateUtil().updateElement(e)
                    }
                },
            )
            OperationStatusIndication(viewModel = viewModel)
        }
    }
}

@Composable
private fun OperationStatusIndication(
    viewModel: AttestationViewModel,
) {
    val elementUpdateResponse = viewModel.useUpdateUtil().elementUpdateFlow.collectAsState().value

    @Composable
    fun Wrapper(content: @Composable() () -> Unit) {
        Column(modifier = Modifier.fillMaxSize()) {
            content()
        }
    }

    when (elementUpdateResponse.status) {
        Status.IDLE -> {
        }
        Status.ERROR -> Wrapper {
            Text(text = "Error updating element")
        }
        Status.LOADING -> Wrapper {
            LoadingIndicator()
        }
        Status.SUCCESS -> Wrapper {
            Text("Element location successfully updated")
        }
    }
}

// Provides buttons etc. for the map based on what it requires 
@Composable
private fun AdditionalUI(
    viewModel: AttestationViewModel,
    onEditLocation: () -> Unit,
    onSaveNewLocation: () -> Unit,
) {
    val location = viewModel.useMapManager().getEditedLocation()

    when (viewModel.useMapManager().mapMode.collectAsState().value) {
        MapMode.SINGLE_ELEMENT -> {
            Column() {
                Button(onClick = onEditLocation) {
                    Text(text = "Edit element location")
                }
            }
//            Button(onClick = { viewModel.useMapManager().clearMarkers() }) {
//                Text(text = "CLEAR")
//            }
        }
        MapMode.EDIT_LOCATION -> {
            Column() {
                Button(onClick = onSaveNewLocation) {
                    Text(text = "Save new location")
                }
                if (location != null) {
                    Button(onClick = { viewModel.useMapManager().centerToDevice() }) {
                        Text(text = "Center to your location")
                    }
                }
            }
//            Button(onClick = { viewModel.useMapManager().clearMarkers() }) {
//                Text(text = "CLEAR")
//            }
        }
        MapMode.ALL_ELEMENTS -> {
            Text(text = "Displaying all elements")
        }
    }
}

// Logic to choose different functionality for the map based on need
private fun setup(
    navController: NavController,
    viewModel: AttestationViewModel,
    mapView: MapView,
) {
    val elementId = navController.currentBackStackEntry?.arguments?.getString(
        ARG_MAP_SINGLE_ELEMENT_ID
    ) ?: return
    val element = viewModel.getElementFromCache(elementId) ?: return

    val hasLocation = viewModel.useMapManager().displayElement(mapView, element)

    if (!hasLocation) {
        viewModel.useMapManager().useEditLocation(mapView, element)
    }
}
