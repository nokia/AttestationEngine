package com.example.mobileattester.pages

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.journeyapps.barcodescanner.CaptureManager
import com.journeyapps.barcodescanner.CompoundBarcodeView

private const val PERMISSIONS_REQUEST_CODE = 10
private val PERMISSIONS_REQUIRED = arrayOf(Manifest.permission.CAMERA)

@Composable
@Preview
fun Scanner(navController: NavController? = null) {

    val context = LocalContext.current

    // Check if device has camera
    var hasCamera by remember {
        mutableStateOf(false)
    }
    if (context.packageManager.hasSystemFeature(
            PackageManager.FEATURE_CAMERA_FRONT
        )
    ) {
        hasCamera = true
    } else {
        // Gracefully degrade your app experience in case there is not front-facing camera.
        val snackbarHostState = remember { SnackbarHostState() }
        SnackbarHost(
            modifier = Modifier,
            hostState = snackbarHostState,
            snackbar = {
                Card(
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(2.dp, Color.White),
                    modifier = Modifier
                        .padding(16.dp)
                        .wrapContentSize()
                ) {
                    Column(
                        modifier = Modifier.padding(8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(imageVector = Icons.Default.Notifications, contentDescription = "")
                        Text("Front-facing camera not found")
                    }


                }
            }
        )
    }
    var hasCameraPermission by remember {
        mutableStateOf(false)
    }

    // Check if app has permissions for camera
    LaunchedEffect(key1 = Unit, block = {
        if (!hasPermissions(context)) {
            ActivityCompat.requestPermissions(
                context as Activity,
                PERMISSIONS_REQUIRED,
                PERMISSIONS_REQUEST_CODE
            )
        } else {
            hasCameraPermission = true
        }
    })

    var scanFlag by remember {
        mutableStateOf(false)
    }


    if (hasCamera && hasCameraPermission) {

        val compoundBarcodeView = remember {
            object : CompoundBarcodeView(context){
                init {
                    viewFinder.setLaserVisibility(false)
                }
            }.apply {
                val capture = CaptureManager(context as Activity, this)
                capture.initializeFromIntent(context.intent, null)
                this.setStatusText("")
                this.resume()
                capture.decode()
                this.decodeContinuous { result ->
                    if (scanFlag) {
                        return@decodeContinuous
                    }
                    scanFlag = true
                    result.text?.let { barCodeOrQr ->
                        Log.d("RESULT", result.toString())
                        val intent = Intent(context, Element::class.java)
                        intent.putExtra("ID", result.toString())
                        context.startActivity(intent)
                        scanFlag = false
                    }
                    //If you don't put this scanFlag = false, it will never work again.
                    //you can put a delay over 2 seconds and then scanFlag = false to prevent multiple scanning
                }
            }
        }
        AndroidView(
            modifier = Modifier,
            factory = { compoundBarcodeView },
        )
    }
}

fun hasPermissions(context: Context) = PERMISSIONS_REQUIRED.all {
    ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
}