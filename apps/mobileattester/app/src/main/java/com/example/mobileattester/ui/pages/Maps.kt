package com.example.mobileattester.ui.pages

import android.Manifest
import android.content.Intent
import android.location.Location
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.navigation.NavController
import com.example.mobileattester.data.model.Element
import com.example.mobileattester.data.network.Response
import com.example.mobileattester.data.network.Status
import com.example.mobileattester.data.util.MapMode
import com.example.mobileattester.ui.components.anim.FadeInWithDelay
import com.example.mobileattester.ui.components.common.Fab
import com.example.mobileattester.ui.components.common.LoadingIndicator
import com.example.mobileattester.ui.theme.*
import com.example.mobileattester.ui.util.PermissionDeniedRequestSettings
import com.example.mobileattester.ui.util.PermissionsRationale
import com.example.mobileattester.ui.util.Screen
import com.example.mobileattester.ui.util.navigate
import com.example.mobileattester.ui.viewmodel.AttestationViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionsRequired
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import compose.icons.TablerIcons
import compose.icons.tablericons.AlertCircle
import compose.icons.tablericons.ArrowsMaximize
import compose.icons.tablericons.Check
import compose.icons.tablericons.CurrentLocation
import org.osmdroid.views.MapView

/**  */
const val ARG_MAP_SINGLE_ELEMENT_ID = "argMapSingleElement"

private val LOCATION_PERMISSIONS = listOf(Manifest.permission.ACCESS_FINE_LOCATION,
    Manifest.permission.READ_EXTERNAL_STORAGE,
    Manifest.permission.WRITE_EXTERNAL_STORAGE)

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MapWrapper(
    navController: NavController,
    viewModel: AttestationViewModel,
) {
    val context = LocalContext.current
    val permissionState = rememberMultiplePermissionsState(permissions = LOCATION_PERMISSIONS)
    val elementId =
        navController.currentBackStackEntry?.arguments?.getString(ARG_MAP_SINGLE_ELEMENT_ID)
    val element = viewModel.getElementFromCache(elementId ?: "")
    val mapMode = viewModel.mapManager.mapMode.collectAsState()
    val elementUpdateResponse = viewModel.updateUtil.elementUpdateFlow.collectAsState()
    val deviceLocation = viewModel.mapManager.getCurrentLocation().collectAsState()
    val updateSent = remember {
        mutableStateOf(false)
    }
    val map = remember {
        MapView(context).also {
            setup(navController, viewModel, it)
        }
    }

    DisposableEffect(LocalLifecycleOwner.current) {

        viewModel.mapManager.registerElementButtonClickHandler {
            navController.navigate(Screen.Element.route, bundleOf(Pair(ARG_ELEMENT_ID, it.itemid)))
        }

        onDispose {
            viewModel.mapManager.resetMapState()
            viewModel.mapManager.unregisterElementButtonClickHandler()
            navController.currentBackStackEntry?.arguments?.remove(ARG_MAP_SINGLE_ELEMENT_ID)
        }
    }

    PermissionsRequired(multiplePermissionsState = permissionState, permissionsNotGrantedContent = {
        PermissionsRationale("Please grant location and storage permissions to access the map.") { permissionState.launchMultiplePermissionRequest() }
    }, permissionsNotAvailableContent = {
        PermissionDeniedRequestSettings(text = "Requested permissions were denied. Missing permissions must be granted manually from settings.") {
            try {
                permissionState.revokedPermissions.first().launchPermissionRequest()
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    Uri.parse("package:" + context.packageName))
                ContextCompat.startActivity(context, intent, null)
            } catch (err: Error) {
                navController.navigate(Screen.Home.route)
            }
        }
    }) {

        // Map content
        Box(contentAlignment = Alignment.BottomStart) {
            AndroidView(factory = { map })

            if (element != null) {
                fun saveNewLocationRequest() {
                    val editedLocation = viewModel.mapManager.getEditedLocation()
                    if (editedLocation.value != null) {
                        viewModel.mapManager.lockInteractions()
                        val e = element.cloneWithNewLocation(editedLocation.value!!)
                        viewModel.updateUtil.updateElement(e)
                        updateSent.value = true
                    }
                }
                when (updateSent.value) {
                    false -> AdditionalUI(mapMode = mapMode.value,
                        deviceLocation = deviceLocation.value,
                        element = element,
                        onEditLocation = {
                            viewModel.mapManager.useEditLocation(map, element)
                        },
                        onSaveNewLocation = { saveNewLocationRequest() },
                        onCancelEdit = {
                            viewModel.mapManager.displayElement(map, element)
                        },
                        onCenter = {
                            viewModel.mapManager.centerToDevice()
                        })
                    true -> OperationStatusIndication(elementUpdateResponse,
                        onRetry = { saveNewLocationRequest() },
                        onClose = { navController.navigateUp() })
                }
            }
        }
    }
}

@Composable
private fun OperationStatusIndication(
    elementUpdateResponse: State<Response<String>>,
    onRetry: () -> Unit,
    onClose: () -> Unit,
) {
    Box(modifier = Modifier
        .fillMaxWidth()
        .fillMaxHeight(),
        contentAlignment = Alignment.TopCenter) {
        when (elementUpdateResponse.value.status) {
            Status.IDLE -> {
            }
            Status.ERROR -> PopUp(icon = TablerIcons.AlertCircle,
                text = "Error updating element.",
                btnText = "Retry",
                color = Error,
                onClick = onRetry)
            Status.LOADING -> {
                Box(modifier = Modifier.padding(24.dp)) {
                    LoadingIndicator()
                }
            }
            Status.SUCCESS -> PopUp(icon = TablerIcons.Check,
                text = "Location set successfully!",
                btnText = "Close map",
                color = Ok,
                onClick = onClose)
        }
    }
}

// Provides buttons etc. for the map based on what it requires
@Composable
private fun AdditionalUI(
    mapMode: MapMode?,
    deviceLocation: Location?,
    element: Element,
    onEditLocation: () -> Unit,
    onSaveNewLocation: () -> Unit,
    onCancelEdit: () -> Unit,
    onCenter: () -> Unit,
) {
    when (mapMode) {
        MapMode.SINGLE_ELEMENT -> Fab(
            icon = TablerIcons.ArrowsMaximize,
            onClick = onEditLocation,
            color = White,
        )
        MapMode.EDIT_LOCATION -> Column(Modifier
            .fillMaxHeight()
            .fillMaxWidth(),
            verticalArrangement = Arrangement.SpaceBetween) {
            Surface(color = Primary,
                shape = RoundedCornerShape(bottomEnd = 6.dp),
                elevation = ELEVATION_XS) {
                Text(modifier = Modifier.padding(10.dp, 6.dp),
                    text = "Setting location: ${element.name}",
                    color = White)
            }
            Column {
                if (deviceLocation != null) {
                    Fab(icon = TablerIcons.CurrentLocation, color = White, onClick = onCenter)
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp, 18.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    OutlinedButton(
                        onClick = onCancelEdit,
                        border = BorderStroke(1.dp, Primary),
                    ) {
                        Text(text = "Cancel")
                    }
                    Button(onClick = onSaveNewLocation) {
                        Text(text = "Save")
                    }
                }
            }
        }
        MapMode.ALL_ELEMENTS -> Text(text = "Displaying all elements")
    }
}

// Logic to choose different functionality for the map based on need
private fun setup(
    navController: NavController,
    viewModel: AttestationViewModel,
    mapView: MapView,
) {
    // Single element id was provided in nav args
    navController.currentBackStackEntry?.arguments?.getString(ARG_MAP_SINGLE_ELEMENT_ID)
        ?.let { id ->
            val element = viewModel.getElementFromCache(id) ?: return
            val hasLocation = viewModel.mapManager.displayElement(mapView, element)
            if (!hasLocation) {
                viewModel.mapManager.useEditLocation(mapView, element)
            }
            return@setup
        }

    // Use map with all element locations displayed
    viewModel.mapManager.displayElements(mapView, viewModel.filterElements())
}

@Composable
private fun PopUp(
    icon: ImageVector,
    text: String,
    btnText: String,
    color: Color,
    onClick: () -> Unit,
) {
    FadeInWithDelay(0) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            color = color,
            elevation = ELEVATION_SM,
            shape = ROUNDED_MD,
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        icon,
                        null,
                        tint = White,
                        modifier = Modifier.size(32.dp),
                    )
                    Text(modifier = Modifier.padding(horizontal = 8.dp), text = text, color = White)
                }
                TextButton(onClick = onClick) {
                    Text(text = btnText, color = White, textDecoration = TextDecoration.Underline)
                }
            }
        }
    }
}
