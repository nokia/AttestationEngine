package com.example.mobileattester

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.ViewModelProvider
import com.example.mobileattester.di.Injector
import com.example.mobileattester.ui.theme.MobileAttesterTheme
import com.example.mobileattester.ui.util.NavUtils
import com.example.mobileattester.ui.viewmodel.AttestationViewModel
import com.example.mobileattester.ui.viewmodel.AttestationViewModelImpl
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

        viewModel = ViewModelProvider(
            this,
            Injector.provideAttestationViewModelFactory(baseUrl = "http://172.30.88.184:8520/")
        ).get(
            AttestationViewModelImpl::class.java
        )

        setContent {
            MobileAttesterTheme {
                NavUtils.Navigator()
            }
            //MainScreenView()
        }
    }
}

