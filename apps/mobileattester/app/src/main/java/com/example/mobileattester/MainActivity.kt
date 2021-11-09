package com.example.mobileattester

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.mobileattester.ui.theme.MobileAttesterTheme
import com.example.mobileattester.util.NavUtils

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MobileAttesterTheme {
                NavUtils.Navigator()
            }
        }
    }
}

