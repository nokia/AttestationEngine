package com.example.mobileattester.ui.pages

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.os.bundleOf
import androidx.navigation.NavController
import com.example.mobileattester.data.model.Element
import com.example.mobileattester.data.model.ElementResult
import com.example.mobileattester.ui.components.TagRow
import com.example.mobileattester.ui.components.common.HeaderRoundedBottom
import com.example.mobileattester.ui.components.common.OutlinedIconButton
import com.example.mobileattester.ui.components.common.TextWithIcon
import com.example.mobileattester.ui.theme.DarkGrey
import com.example.mobileattester.ui.theme.Error
import com.example.mobileattester.ui.theme.Ok
import com.example.mobileattester.ui.theme.PrimaryDark
import com.example.mobileattester.ui.util.Screen
import com.example.mobileattester.ui.util.navigate
import com.example.mobileattester.ui.util.parseBaseUrl
import com.example.mobileattester.ui.viewmodel.AttestationViewModel
import compose.icons.TablerIcons
import compose.icons.tablericons.*
import java.text.SimpleDateFormat
import kotlin.math.roundToLong

const val ARG_ITEM_ID = "item_id"

@Composable
fun Element(navController: NavController, viewModel: AttestationViewModel) {
    val clickedElementId =
        navController.currentBackStackEntry?.arguments?.get(ARG_ITEM_ID).toString().let {
            if (it.startsWith("http")) {
                println("Found Link: $it")
                parseBaseUrl(it)
            } else
                it.trim()
        }

    val element = viewModel.getElementFromCache(clickedElementId)

    if (element == null) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "This element was not found, please ensure that the server contains this element",
                modifier = Modifier.padding(16.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
    } else {

        fun onAttestClick() {
            navController.navigate(Screen.Attest.route, bundleOf(Pair(ARG_ITEM_ID, element.itemid)))
        }

        val scrollState = ScrollState(0)

        Column(modifier = Modifier.verticalScroll(scrollState)) {
            // Render element header
            HeaderRoundedBottom {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 24.dp)) {
                    Text(text = element.name, style = MaterialTheme.typography.h3)
                    Text(
                        text = element.endpoint,
                        style = MaterialTheme.typography.body1,
                        modifier = Modifier.padding(vertical = 8.dp, horizontal = 2.dp)
                    )
                }
            }

            // Content
            Column(Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
                TagRow(element.types)
                Spacer(modifier = Modifier.size(26.dp))
                ElementActions(onAttestClick = ::onAttestClick)
                Spacer(modifier = Modifier.size(26.dp))
                Text(
                    text = element.description ?: "",
                    color = DarkGrey,
                )
                Spacer(modifier = Modifier.size(26.dp))
                Divider(color = Color(197, 197, 197), thickness = 1.dp)
                Spacer(modifier = Modifier.size(26.dp))
                ElementResult(element)
            }

        }
    }
}

@Composable
fun ElementResult(element: Element) {
    val resultHoursShown = remember { mutableStateOf(24) }
    val latestResults = element.results.latestResults(resultHoursShown.value)


    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text("Results", fontSize = 24.sp)
        TextWithIcon(
            text = resultHoursShown.value.shownHoursToString(),
            icon = TablerIcons.ListCheck,
            color = PrimaryDark
        )
    }
    SpacerSmall()

    ElementResultSummary(latestResults)

    SpacerSmall()
    SpacerSmall()

    ElementResultFull(latestResults, latestResults.size == element.results.size) {
        // On more items requested fetch a batch of items by time.
        val hourInSeconds = 3600
        val resultSeconds =
            element.results[latestResults.size].verifiedAt.toDoubleOrNull()!!.roundToLong()
        val curTimeInSeconds: Long = System.currentTimeMillis() / 1000
        resultHoursShown.value =
            curTimeInSeconds.minus(resultSeconds).div(hourInSeconds).toInt().hoursHWMYRounded()
    }
}

@Composable
fun ElementResultSummary(results: Collection<ElementResult>) {
    val passed = results.count { it.result == 0 }
    val failed = results.size - passed
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            OutlinedIconButton(
                text = results.size.toString(),
                rounded = true,
                width = 20.dp,
                height = 20.dp,
                color = MaterialTheme.colors.primary,
                filled = true
            ) // TODO: Use AspectRatio
            Text(text = "Attestations", color = MaterialTheme.colors.primary)
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            OutlinedIconButton(
                text = passed.toString(),
                rounded = true,
                width = 20.dp,
                height = 20.dp,
                color = Ok,
                filled = true
            ) // TODO: Use AspectRatio
            Text(text = "Passed", color = Ok)
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            OutlinedIconButton(
                text = failed.toString(),
                rounded = true,
                width = 20.dp,
                height = 20.dp,
                color = Error,
                filled = true
            ) // TODO: Use AspectRatio
            Text(text = "Failed", color = Error)
        }
    }
}

@Composable
fun ElementResultFull(
    results: Collection<ElementResult>,
    allShown: Boolean,
    onMoreRequested: () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
    ) {
        results.forEach {
            Row() {
                OutlinedIconButton(
                    icon = if (it.result == 0) TablerIcons.Check else TablerIcons.X,
                    width = 30.dp,
                    height = 30.dp,
                    border = null,
                    color = if (it.result == 0) Ok else Error,
                    rounded = true
                )
                Text(text = it.ruleName, maxLines = 1)
                Text(
                    text = SimpleDateFormat(
                        "dd.MM.",
                        java.util.Locale.getDefault()
                    ).format(it.verifiedAt.toFloat() * 1000L),
                    maxLines = 1,
                    textAlign = TextAlign.End,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        if (!allShown) {
            Text(
                text = "More results",
                modifier = Modifier.clickable { onMoreRequested() },
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun ElementActions(onAttestClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        OutlinedIconButton(TablerIcons.EyeCheck, text = "Attest") {
            onAttestClick()
        }
        SpacerSmall()
        OutlinedIconButton(TablerIcons.ZoomCheck, text = "Verify") {}
    }
}


@Composable
fun SpacerSmall() {
    Spacer(modifier = Modifier.size(8.dp))
}

private fun Collection<ElementResult>.latestResults(hours: Int = 24): Collection<ElementResult> {
    return this.takeWhile {
        val hourInSeconds = 3600
        val timeInSeconds: Long = System.currentTimeMillis() / 1000
        val verifiedAt: Long? = it.verifiedAt.toDoubleOrNull()?.roundToLong()

        (timeInSeconds.minus(verifiedAt ?: 0)) < (hourInSeconds * hours)
    }
}

private fun Int.shownHoursToString(): String {
    return when {
        this % (24 * 7 * 4 * 12) == 0 // year
        -> "${this / (24 * 7 * 4 * 12)}Y"
        this % (24 * 7 * 4) == 0 // month
        -> "${this / (24 * 7 * 4)}M"
        this % (24 * 7) == 0 // week
        -> "${this / (24 * 7)}W"
        else -> "${this}H" // hour
    }
}

private fun Int.hoursHWMYRounded(): Int {
    val mul = when {
        this < 24 * 7
        -> 24
        this < 24 * 7 * 4
        -> 24 * 7
        this < 24 * 7 * 4 * 12
        -> 24 * 7 * 4
        else
        -> 24 * 7 * 4 * 12
    }

    return this + mul - (this % mul).also { if (it == 0) return this }
}