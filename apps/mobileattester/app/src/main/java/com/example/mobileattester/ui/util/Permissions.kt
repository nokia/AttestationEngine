package com.example.mobileattester.ui.util

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

fun hasLocationPermission(context: Context, permission: String): Boolean =
    (ContextCompat.checkSelfPermission(
        context, permission
    ) == PackageManager.PERMISSION_GRANTED)

fun locationProvider(context: Context): String? = when {
    hasLocationPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) -> {
        LocationManager.GPS_PROVIDER
    }
    hasLocationPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) -> {
        LocationManager.NETWORK_PROVIDER
    }
    else -> {
        null
    }
}

fun requestPermissions(context: Activity, permissions: List<String>) {
    val requiredPermissions = permissions.map {
        val p = ContextCompat.checkSelfPermission(context, it)
        if (p != PackageManager.PERMISSION_GRANTED) {
            return@map it
        } else null
    }

    requiredPermissions.filterNotNull().let {
        ActivityCompat.requestPermissions(
            context, it.toTypedArray(),
            0
        )
    }
}


@Composable
fun PermissionsRationale(
    text: String,
    onRequestPermission: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text,
            modifier = Modifier.padding(16.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = onRequestPermission) {
            Text("Grant permission")
        }
    }
}


@Composable
fun PermissionDeniedRequestSettings(
    text: String,
    onRequestPermission: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text,
            modifier = Modifier.padding(16.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = { onRequestPermission() }) {
            Text("Go to settings.")
        }
    }
}
