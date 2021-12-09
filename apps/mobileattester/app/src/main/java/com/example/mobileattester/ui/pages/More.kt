package com.example.mobileattester.ui.pages

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import com.example.mobileattester.ui.theme.FONTSIZE_XL
import com.example.mobileattester.ui.theme.ROUNDED_SM
import com.example.mobileattester.ui.util.Screen
import com.example.mobileattester.ui.viewmodel.AttestationViewModel
import compose.icons.TablerIcons
import compose.icons.tablericons.ChevronRight
import org.osmdroid.views.MapView

@Composable
fun More(navController: NavController, viewModel: AttestationViewModel) {

    Column(
        Modifier
            .fillMaxSize()
            .padding(16.dp),
    ) {
        val spec = viewModel.engineInfo.spec.collectAsState()
        Text(spec.value.toString())

        Text(text = "Elements", fontSize = FONTSIZE_XL)
        Box(modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .clip(ROUNDED_SM)
            .padding(top = 16.dp),
            contentAlignment = Alignment.CenterEnd) {
            AndroidView(
                factory = {
                    MapView(it).apply {}
                },
                modifier = Modifier.clip(ROUNDED_SM),
            )
            Icon(
                imageVector = TablerIcons.ChevronRight,
                modifier = Modifier.size(32.dp),
                contentDescription = null,
            )
            Box(
                modifier = Modifier
                    .clickable {
                        navController.navigate(Screen.Map.route)
                    }
                    .fillMaxSize(),
            ) {}
        }
    }
}
