package com.example.mobileattester.ui.pages

import android.app.Activity
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import com.journeyapps.barcodescanner.CaptureManager
import com.journeyapps.barcodescanner.CompoundBarcodeView

@Composable
@Preview
fun Scanner(navController: NavController? = null) {
    val context = LocalContext.current
    var scanFlag by remember {
        mutableStateOf(false)
    }
    val compoundBarcodeView = remember {
        object: CompoundBarcodeView(context) {
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
                    //Do something and when you finish this something
                    //put scanFlag = false to scan another item
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