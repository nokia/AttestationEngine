package com.example.mobileattester

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.mobileattester.di.Injector
import com.example.mobileattester.ui.pages.Elements
import com.example.mobileattester.ui.pages.More
import com.example.mobileattester.ui.pages.Scanner
import com.example.mobileattester.ui.theme.MobileAttesterTheme
import com.example.mobileattester.ui.util.NavUtils
import com.example.mobileattester.ui.viewmodel.AttestationViewModel
import com.example.mobileattester.ui.viewmodel.AttestationViewModelImpl
import compose.icons.TablerIcons
import compose.icons.tablericons.DeviceDesktop
import compose.icons.tablericons.Dots
import compose.icons.tablericons.Qrcode
import com.google.accompanist.permissions.ExperimentalPermissionsApi

class MainActivity : ComponentActivity() {
    /*
    Init viewmodel in here, get in a Composable with:

    val viewModel: AttestationViewModelImpl = viewModel(viewModelStoreOwner = LocalContext.current as ComponentActivity)

    */
    private lateinit var viewModel: AttestationViewModel

    @ExperimentalPermissionsApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProvider(this,
                Injector.provideAttestationViewModelFactory(baseUrl = "http://172.30.92.46:8520/")).get(
                AttestationViewModelImpl::class.java)

        setContent {
            MobileAttesterTheme {
                NavUtils.Navigator()
            }
            //MainScreenView()
        }
    }
}

