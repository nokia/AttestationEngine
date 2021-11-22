package com.example.mobileattester

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.ViewModelProvider
import com.example.mobileattester.di.Injector
import com.example.mobileattester.ui.theme.MobileAttesterTheme
import com.example.mobileattester.ui.util.NavUtils
import com.example.mobileattester.ui.util.Preferences
import com.example.mobileattester.ui.viewmodel.AttestationViewModel
import com.example.mobileattester.ui.viewmodel.AttestationViewModelImpl
import com.google.accompanist.permissions.ExperimentalPermissionsApi

class MainActivity : ComponentActivity() {
    private lateinit var viewModel: AttestationViewModel

    @ExperimentalPermissionsApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        /**
         * APPLICATION CURRENTLY CRASHES AFTER A WHILE IF AN INVALID ADDRESS IS PROVIDED
         * TO RETROFIT SERVICE.
         */

        viewModel = ViewModelProvider(this,
            Injector.provideAttestationViewModelFactory(Preferences.defaultConfig.first())).get(
            AttestationViewModelImpl::class.java)

        setContent {
            MobileAttesterTheme {
                NavUtils.Navigator()
            }
        }
    }
}

