package com.example.mobileattester.ui.pages

import android.annotation.SuppressLint
import androidx.appcompat.content.res.AppCompatResources
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterEnd
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.os.bundleOf
import androidx.navigation.NavController
import com.example.mobileattester.R
import com.example.mobileattester.data.model.Element
import com.example.mobileattester.data.model.ElementResult
import com.example.mobileattester.ui.components.TagRow
import com.example.mobileattester.ui.components.anim.FadeInWithDelay
import com.example.mobileattester.ui.components.common.DecorText
import com.example.mobileattester.ui.components.common.HeaderRoundedBottom
import com.example.mobileattester.ui.components.common.OutlinedIconButton
import com.example.mobileattester.ui.theme.*
import com.example.mobileattester.ui.util.*
import com.example.mobileattester.ui.viewmodel.AttestationViewModel
import compose.icons.TablerIcons
import compose.icons.tablericons.Checkbox
import compose.icons.tablericons.ChevronRight
import compose.icons.tablericons.CurrentLocation
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

const val ARG_ELEMENT_ID = "item_id"

@Composable
fun Element(navController: NavController, viewModel: AttestationViewModel) {
    val clickedElementId = navController.currentBackStackEntry?.arguments?.getString(ARG_ELEMENT_ID)
    val element = viewModel.getElementFromCache(clickedElementId ?: "") ?: run {
        ElementNull()
        return
    }

    fun onAttestClick() {
        navController.navigate(Screen.Attest.route, bundleOf(Pair(ARG_ELEMENT_ID, element.itemid)))
    }

    fun onLocationClick() {
        navController.navigate(Screen.Map.route,
            bundleOf(Pair(ARG_MAP_SINGLE_ELEMENT_ID, element.itemid)))
    }

    // Content
    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
        // Element header
        HeaderRoundedBottom {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 24.dp)) {
                Text(
                    text = element.name,
                    fontSize = FONTSIZE_XXL,
                    fontWeight = FontWeight.Bold,
                )
                Text(text = element.endpoint,
                    style = MaterialTheme.typography.body1,
                    modifier = Modifier.padding(vertical = 8.dp, horizontal = 2.dp))
            }
        }
        // Content
        Column(Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
            TagRow(element.types)
            Spacer(modifier = Modifier.size(26.dp))
            ElementActions(onAttestClick = ::onAttestClick, onLocationClick = ::onLocationClick)
            Spacer(modifier = Modifier.size(26.dp))
            Text(text = element.description ?: "", color = DarkGrey)
            Spacer(modifier = Modifier.size(26.dp))
            Divider(color = DividerColor, thickness = 1.dp)
            ElementMap(element = element) {
                println("ON LOCATION CLICKED")
                onLocationClick()
            }
            Divider(color = DividerColor, thickness = 1.dp)
            Spacer(modifier = Modifier.size(26.dp))
            ElementResult(navController, element)
        }
    }
}

@SuppressLint("ClickableViewAccessibility")
@Composable
private fun ElementMap(element: Element, onClick: () -> Unit) {
    val loc = element.geoPoint() ?: return
    val ctx = LocalContext.current

    Text(modifier = Modifier.padding(top = 16.dp, bottom = 24.dp),
        text = "Location",
        fontSize = FONTSIZE_XL)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .clip(ROUNDED_SM),
        contentAlignment = CenterEnd
    ) {
        AndroidView(
            factory = {
                MapView(it).apply {
                    setTileSource(TileSourceFactory.WIKIMEDIA)
                    isTilesScaledToDpi = true
                    controller.setZoom(18.0)
                    this.controller.setCenter(loc)
                    setBuiltInZoomControls(false)
                    setOnTouchListener { v, _ -> true }
                    val marker = Marker(this).apply {
                        icon =
                            AppCompatResources.getDrawable(ctx,
                                R.drawable.ic_baseline_location_on_32)
                                .apply {
                                    this?.setTint(ctx.getColor(R.color.primary))
                                }
                        position = loc
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                    }
                    this.overlays?.add(marker)
                }
            },
            modifier = Modifier
                .clip(ROUNDED_SM),
        )
        Icon(
            imageVector = TablerIcons.ChevronRight,
            modifier = Modifier.size(32.dp),
            contentDescription = null,
        )
        Box(modifier = Modifier
            .clickable { onClick() }
            .fillMaxSize()) {}
    }

}


@Composable
private fun ElementNull() {
    FadeInWithDelay(1000) {
        Column(modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally) {
            Text("This element was not found, please ensure that the server contains this element",
                modifier = Modifier.padding(16.dp))
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun ElementResult(navController: NavController, element: Element) {
    val resultHoursShown = remember { mutableStateOf(24) }
    val latestResults = element.results.latestResults(resultHoursShown.value)

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text("Results", fontSize = FONTSIZE_XL)
        Text(resultHoursShown.value.shownHoursToString(), fontSize = FONTSIZE_XL)
    }
    Spacer(Modifier.size(32.dp))

    ElementResultSummary(latestResults)

    Spacer(Modifier.size(24.dp))

    val onMoreResultsRequested: () -> Unit = {
        val result = element.results.drop(latestResults.size).find {
            Timestamp.fromSecondsString(it.verifiedAt)!!.timeSince()
                .toHours() > resultHoursShown.value
        }

        val resultTimestamp = Timestamp.fromSecondsString(result!!.verifiedAt)!!

        resultHoursShown.value = resultTimestamp.timeSince().toHours().hoursHWMYRounded()

    }


    ElementResultFull(latestResults, latestResults.size == element.results.size, onResultClicked = {
        navController.navigate(Screen.Result.route, bundleOf(Pair(ARG_RESULT_ID, it.itemid)))
    }, onMoreRequested = onMoreResultsRequested)
}

@Composable
private fun ElementResultSummary(results: Collection<ElementResult>) {
    val passed = results.count { it.result == 0 }
    val failed = results.size - passed

    @Composable
    fun LocalComp(num: Int, text: String, color: Color) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            OutlinedIconButton(text = num.toString(),
                rounded = true,
                width = 20.dp,
                height = 20.dp,
                color = color,
                filled = true) // TODO: Use AspectRatio
            DecorText(txt = text, color = color)
        }
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        LocalComp(num = results.size, text = "Attest.", Primary)
        LocalComp(num = passed, text = "Passed", Ok)
        LocalComp(num = failed, text = "Failed", Error)
    }
}

@Composable
private fun ElementResultFull(
    results: Collection<ElementResult>,
    allShown: Boolean,
    onResultClicked: (ElementResult) -> Unit,
    onMoreRequested: () -> Unit,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()) {
        results.forEach {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onResultClicked(it) }
                    .padding(vertical = 6.dp),
                verticalAlignment = CenterVertically,
            ) {
                OutlinedIconButton(icon = getResultIcon(it),
                    width = 24.dp,
                    height = 24.dp,
                    border = null,
                    color = getCodeColor(it.result),
                    rounded = true)
                Text(
                    modifier = Modifier.padding(horizontal = 4.dp),
                    text = it.ruleName,
                    maxLines = 1,
                )
                Text(
                    text = getTimeFormatted(it.verifiedAt, DatePattern.DateOnly),
                    maxLines = 1,
                    textAlign = TextAlign.End,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }

        if (!allShown) {
            TextButton(modifier = Modifier.padding(16.dp), onClick = { onMoreRequested() }) {
                Text(
                    text = "More results (+24h)",
                    textAlign = TextAlign.Center,
                )
            }
        } else {
            if (results.isEmpty()) {
                Text(
                    modifier = Modifier.padding(24.dp),
                    text = "No results available",
                    textAlign = TextAlign.Center,
                )
            } else {
                Text(
                    modifier = Modifier.padding(24.dp),
                    text = "All results listed.",
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

@Composable
private fun ElementActions(onAttestClick: () -> Unit, onLocationClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth(),
        verticalAlignment = CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        OutlinedIconButton(TablerIcons.Checkbox, rounded = true, color = Ok) {
            onAttestClick()
        }
        Spacer(modifier = Modifier.size(8.dp))
        OutlinedIconButton(TablerIcons.CurrentLocation, rounded = true) {
            onLocationClick()
        }
    }
}
