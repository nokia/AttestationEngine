package com.example.mobileattester

import android.os.Bundle
import android.preference.PreferenceManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModelProvider
import com.example.mobileattester.di.Injector
import com.example.mobileattester.ui.theme.MobileAttesterTheme
import com.example.mobileattester.ui.util.NavUtils
import com.example.mobileattester.ui.util.Preferences
import com.example.mobileattester.ui.viewmodel.AttestationViewModel
import com.example.mobileattester.ui.viewmodel.AttestationViewModelImpl
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import org.osmdroid.config.Configuration

class MainActivity : ComponentActivity() {
    private lateinit var viewModel: AttestationViewModel

    @ExperimentalPermissionsApi
    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        super.onCreate(savedInstanceState)


        viewModel = ViewModelProvider(this,
            Injector.provideAttestationViewModelFactory(Preferences.defaultConfig.first(),
                applicationContext))[AttestationViewModelImpl::class.java]

        Configuration.getInstance()
            .load(applicationContext,
                PreferenceManager.getDefaultSharedPreferences(applicationContext))

        setContent {
            MobileAttesterTheme {
                NavUtils.Navigator()
            }
        }
    }
}

