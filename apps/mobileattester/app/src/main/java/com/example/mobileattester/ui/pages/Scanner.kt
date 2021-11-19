package com.example.mobileattester.ui.pages

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat.startActivity
import androidx.core.os.bundleOf
import androidx.navigation.NavController
import com.example.mobileattester.ui.util.Screen
import com.example.mobileattester.ui.util.navigate
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionRequired
import com.google.accompanist.permissions.rememberPermissionState
import com.journeyapps.barcodescanner.CaptureManager
import com.journeyapps.barcodescanner.CompoundBarcodeView


@ExperimentalPermissionsApi
@Composable
@Preview
fun Scanner(navController: NavController? = null) {

    val context = LocalContext.current


    // Check if device has camera
    if (context.packageManager.hasSystemFeature(
            PackageManager.FEATURE_CAMERA_ANY
        )
    ) {
        val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)
        PermissionRequired(
            permissionState = cameraPermissionState,
            permissionNotGrantedContent = {
                Rationale(
                    onRequestPermission = { cameraPermissionState.launchPermissionRequest() }
                )
            },
            permissionNotAvailableContent = {
                PermissionDenied {
                    try {
                        cameraPermissionState.launchPermissionRequest()
                        val intent = Intent(
                            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                            Uri.parse("package:" + context.packageName)
                        )
                        startActivity(context, intent, null)
                    } catch (err: Error) {
                        navController?.navigate(Screen.Home.route)
                    }
                }
            }
        ) {

            val compoundBarcodeView = remember {
                object : CompoundBarcodeView(context) {
                    init {
                        viewFinder.setLaserVisibility(false)
                        resume()
                    }
                }.apply {
                    val capture = CaptureManager(context as Activity, this)
                    capture.initializeFromIntent(context.intent, null)
                    this.setStatusText("")

                    // Stop focus looper if already scanned, fixes error that occurs when user scans too often.
                    this.cameraSettings.isAutoFocusEnabled = true

                    this.decodeSingle { result ->
                        result.text?.let { _ ->
                            this.cameraSettings.isAutoFocusEnabled = false

                            navController!!.navigate(
                                Screen.Element.route,
                                bundleOf(Pair(ARG_ITEM_ID, result.toString()))
                            )
                        }
                    }

                }
            }

            DisposableEffect(LocalLifecycleOwner.current) {
                // Ensure scanner pauses on dispose
                this.onDispose {
                    compoundBarcodeView.pause()
                }
            }

            AndroidView(
                modifier = Modifier,
                factory = { compoundBarcodeView },
            )


        }
    } else {
        // Indicate that the device does not support QR scanning.
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "This device does not have a camera.",
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}


@Composable
private fun Rationale(
    onRequestPermission: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Please grant the camera permission to scan QR codes.",
            modifier = Modifier.padding(16.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = onRequestPermission) {
            Text("Grant permission")
        }
    }
}

@Composable
private fun PermissionDenied(
    onRequestPermission: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Requesting camera permission was denied. It must be granted manually from the settings",
            modifier = Modifier.padding(16.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = { onRequestPermission() }) {
            Text("Go to settings")
        }
    }
}
