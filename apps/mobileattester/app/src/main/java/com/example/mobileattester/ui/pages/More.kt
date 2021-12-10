package com.example.mobileattester.ui.pages

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import com.example.mobileattester.ui.theme.*
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
    ) {
        Info(navController = navController, viewModel = viewModel)
        Map(navController = navController, viewModel = viewModel)
    }
}


@Composable fun Info(navController: NavController, viewModel: AttestationViewModel)
{
    val engineInfo = viewModel.engineInfo.spec.collectAsState()

    Text(text = "Engine Information", modifier = Modifier.padding(16.dp), fontSize = FONTSIZE_XL)
    Surface(elevation = 8.dp, modifier = Modifier.padding(16.dp), shape = ROUNDED_SM ) {
        Column(modifier = Modifier
            .fillMaxWidth().padding(8.dp) )
        {
            Text(text = engineInfo.value.info?.title.toString(), modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center, fontSize = FONTSIZE_XL)
            Text(text = engineInfo.value.info?.description.toString(), modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center, fontSize = FONTSIZE_MD)
            Text(text = engineInfo.value.info?.version.toString().let { "v.$it" }, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Right, fontSize = FONTSIZE_MD)
        }
    }
}

@Composable fun Map(navController: NavController, viewModel: AttestationViewModel)
{
    lateinit var map : MapView

    fun mapInit()
    {
        map.isVerticalMapRepetitionEnabled = false
        map.isTilesScaledToDpi = true
    }

    Column(modifier = Modifier.padding(16.dp)) {
        Text(text = "Elements", modifier = Modifier.padding(bottom = 16.dp), fontSize = FONTSIZE_XL)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .clip(ROUNDED_SM),
            contentAlignment = Alignment.CenterEnd
        ) {
            AndroidView(
                factory = {
                    MapView(it).apply { map = this; mapInit() }
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
