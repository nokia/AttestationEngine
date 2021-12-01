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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat.startActivity
import androidx.core.os.bundleOf
import androidx.navigation.NavController
import com.example.mobileattester.ui.util.*
import com.example.mobileattester.ui.viewmodel.AttestationViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionRequired
import com.google.accompanist.permissions.rememberPermissionState
import com.journeyapps.barcodescanner.CaptureManager
import com.journeyapps.barcodescanner.CompoundBarcodeView
import kotlinx.coroutines.launch


@ExperimentalPermissionsApi
@Composable
fun Scanner(navController: NavController? = null, viewModel: AttestationViewModel) {

    val context = LocalContext.current
    val scope = rememberCoroutineScope()


    // Check if device has camera
    if (context.packageManager.hasSystemFeature(
            PackageManager.FEATURE_CAMERA_ANY
        )
    ) {
        val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)
        PermissionRequired(
            permissionState = cameraPermissionState,
            permissionNotGrantedContent = {
                PermissionsRationale("Please grant the camera permission to scan QR codes.") {
                    cameraPermissionState.launchPermissionRequest()
                }
            },
            permissionNotAvailableContent = {
                PermissionDeniedRequestSettings("Requesting camera permission was denied. It must be granted manually from the settings") {
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
                }.apply outer@ {
                    val capture = CaptureManager(context as Activity, this)
                    capture.initializeFromIntent(context.intent, null)
                    this.setStatusText("")

                    // Stop focus looper if already scanned, fixes error that occurs when user scans too often.
                    this.cameraSettings.isAutoFocusEnabled = true

                    this.decodeContinuous { qr ->
                        println(qr.text)
                        scope.launch {
                            if (viewModel.getElementFromCache(qr.text) != null) {
                                this@outer.cameraSettings.isAutoFocusEnabled = false
                                navController!!.navigate(
                                    Screen.Element.route,
                                    bundleOf(Pair(ARG_ITEM_ID, qr.text))
                                )
                            } else if (qr.text.startsWith("http")) {
                                val parsedId = parseBaseUrl(qr.text, false)
                                println(parsedId)
                                if (parsedId != null && viewModel.getElementFromCache(parsedId) != null) {
                                    this@outer.cameraSettings.isAutoFocusEnabled = false
                                    navController!!.navigate(
                                        Screen.Element.route,
                                        bundleOf(Pair(ARG_ITEM_ID, parsedId))
                                    )
                                }
                            }
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