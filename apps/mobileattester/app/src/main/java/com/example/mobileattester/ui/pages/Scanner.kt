package com.example.mobileattester.ui.pages

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import com.example.mobileattester.ui.util.Screen
import com.example.mobileattester.ui.util.navigate
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionRequired
import com.google.accompanist.permissions.rememberPermissionState
import com.journeyapps.barcodescanner.CaptureManager
import com.journeyapps.barcodescanner.CompoundBarcodeView
import androidx.core.app.ActivityCompat.startActivityForResult

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.startActivity





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
                    } catch (err : Error) {navController?.navigate(Screen.Home.route)}
                }
            }
        ) {

            var scanFlag by remember {
                mutableStateOf(false)
            }

            val compoundBarcodeView = remember {
                object : CompoundBarcodeView(context) {
                    init {
                        viewFinder.setLaserVisibility(false)
                    }
                }.apply {
                    val capture = CaptureManager(context as Activity, this)
                    capture.initializeFromIntent(context.intent, null)
                    this.setStatusText("")
                    this.resume()

                    // Stop focus looper if already scanned, fixes error that occurs when user scans too often.
                    this.cameraSettings.isAutoFocusEnabled = scanFlag

                    capture.decode()
                    this.decodeSingle { result ->
                        if (scanFlag) {
                            return@decodeSingle
                        }
                        scanFlag = true
                        result.text?.let { _ ->
                            scanFlag = false
                            navController!!.navigate(
                                Screen.Element.route,
                                args = Bundle().apply {
                                    putString(
                                        "id",
                                        result.toString()
                                    )
                                })

                        }
                    }

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
