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

class MainActivity : ComponentActivity() {
    /*
    Init viewmodel in here, get in a Composable with:

    val viewModel: AttestationViewModelImpl = viewModel(viewModelStoreOwner = LocalContext.current as ComponentActivity)

    */
    private lateinit var viewModel: AttestationViewModel

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

val pages = listOf(Pair("Home", Icons.Filled.Home),
    Pair("Elements", TablerIcons.DeviceDesktop),
    Pair("Scanner", TablerIcons.Qrcode),
    Pair("More", TablerIcons.Dots))

@Composable
@Preview
fun MainScreenView() {
    MobileAttesterTheme {
        Surface(color = MaterialTheme.colors.background) {
            val navController = rememberNavController()
            Scaffold(bottomBar = {
                Row(
                    modifier = Modifier.fillMaxWidth().background(Color(45, 48, 71)),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                ) {
                    pages.forEach { // Create bottom button for each page
                        BottomIcon(label = it.first, icon = it.second) { page ->  // OnClick
                            while (navController.popBackStack()) {
                            } // Remove backstack
                            navController.navigate(page)
                        }
                    }
                }
            }) { innerPadding -> // CONTENT
                NavHost(navController = navController,
                    startDestination = pages.first().first,
                    modifier = Modifier.padding(innerPadding)) {
                    pages.forEach {
                        when (it.first) {
                            //"Home" -> composable(it.first) { _ -> Home(navController) }
                            "Elements" -> composable(it.first) { _ -> Elements(navController) }
                            "Scanner" -> composable(it.first) { _ -> Scanner(navController) }
                            "More" -> composable(it.first) { _ -> More(navController) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BottomIcon(label: String, icon: ImageVector, onClick: (page: String) -> Unit = {}) {
    Box {
        Button(onClick = { onClick(label) },
            colors = ButtonDefaults.buttonColors(backgroundColor = Color.Transparent,
                contentColor = Color.White),
            elevation = null) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(icon, contentDescription = label)
                Text(label)
            }
        }
    }
}

