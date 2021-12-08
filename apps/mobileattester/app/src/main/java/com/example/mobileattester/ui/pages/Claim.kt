package com.example.mobileattester.ui.pages

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.os.bundleOf
import androidx.navigation.NavController
import com.example.mobileattester.data.model.Claim
import com.example.mobileattester.data.model.PCR
import com.example.mobileattester.data.model.Quote
import com.example.mobileattester.data.util.AttestationUtil
import com.example.mobileattester.ui.components.anim.FadeInWithDelay
import com.example.mobileattester.ui.components.common.HeaderRoundedBottom
import com.example.mobileattester.ui.components.common.TextWithSmallHeader
import com.example.mobileattester.ui.theme.*
import com.example.mobileattester.ui.util.DatePattern
import com.example.mobileattester.ui.util.Screen
import com.example.mobileattester.ui.util.getTimeFormatted
import com.example.mobileattester.ui.util.navigate
import compose.icons.TablerIcons
import compose.icons.tablericons.InfoSquare

const val ARG_CLAIM_ID = "arg_claim_id"

@Composable
fun ClaimWrapper(
    navController: NavController,
    attestationUtil: AttestationUtil,
) {
    navController.currentBackStackEntry?.arguments?.getString(ARG_CLAIM_ID)?.let { id ->
        // Fetch the claim if not yet done
        attestationUtil.fetchClaim(id)
        val claims = attestationUtil.claimFlow.collectAsState()
        val claim = claims.value.data?.find { it.itemid == id }
        if (claim != null) {
            Claim(navController = navController, claim = claim)
        } else {
            FadeInWithDelay(2000) {
                Text(text = "Claim data not found")
            }
        }
        return@ClaimWrapper
    }

    // If no navigation argument was found, use the latest claim.

    val claim = attestationUtil.latestClaim.collectAsState()
    claim.value?.data?.let {
        Claim(navController = navController, claim = it)
        return@ClaimWrapper
    }


    FadeInWithDelay(2000) {
        Text(text = "Claim data not found")
    }
}

@Composable
fun Claim(
    navController: NavController,
    claim: Claim,
) {
    println("CLAIM : $claim")
    Column(Modifier
        .fillMaxWidth()
        .verticalScroll(rememberScrollState())) {
        HeaderRoundedBottom {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 24.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    TablerIcons.InfoSquare,
                    contentDescription = null,
                    tint = White,
                    modifier = Modifier.size(40.dp),
                )
                Spacer(modifier = Modifier.size(8.dp))
                Text(
                    text = "Claim",
                    color = White,
                    fontSize = FONTSIZE_XXL,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
        Column(Modifier.padding(16.dp)) {
            TextWithSmallHeader(text = claim.itemid, header = "ID")
            TextWithSmallHeader(
                text = claim.getElementData()?.second ?: "Error",
                header = "Element",
                onClick = {
                    navController.navigate(Screen.Element.route,
                        bundleOf(Pair(ARG_ELEMENT_ID, claim.getElementData()?.first)))
                },
            )
            TextWithSmallHeader(
                text = claim.getPolicyData()?.second ?: "Error",
                header = "Policy",
                onClick = {
                    navController.navigate(Screen.Policy.route,
                        bundleOf(Pair(ARG_POLICY_ID, claim.getPolicyData()?.first)))
                },
            )
            TextWithSmallHeader(text = getTimeFormatted(
                claim.getTimestamps().first?.time.toString(),
                DatePattern.DateTimeMs,
            ), header = "Requested")
            TextWithSmallHeader(text = getTimeFormatted(
                claim.getTimestamps().second?.time.toString(),
                DatePattern.DateTimeMs,
            ), header = "Received")
            Divider(color = DividerColor, modifier = Modifier.padding(top = 16.dp))
            CTabs(claim)
        }
    }
}

@Composable
private fun CTabs(claim: Claim) {
    val tabs = listOf("Quote", "PCRs", "UEFI")
    val selectedIndex = remember {
        mutableStateOf(0)
    }

    fun changeSelected(i: Int) {
        selectedIndex.value = i
    }

    TabRow(
        modifier = Modifier.padding(bottom = 16.dp),
        selectedTabIndex = selectedIndex.value,
        backgroundColor = White,
        contentColor = Primary,
    ) {
        tabs.forEachIndexed { i, tab ->
            Tab(
                selected = selectedIndex.value == i,
                onClick = { changeSelected(i) },
            ) {
                Text(modifier = Modifier.padding(vertical = 16.dp), text = tab)
            }
        }
    }

    when (selectedIndex.value) {
        0 -> Quote(quote = claim.getQuote())
        1 -> PCRs(data = claim.getPCRs())
        2 -> ErrorMsg(msg = "Not implemented. add")
    }
}

@Composable
private fun PCRs(data: List<PCR>?) {
    if (data == null) {
        ErrorMsg(msg = "Claim does not contain PCRs, or the form is invalid.")
        return
    }

    data.forEachIndexed { i, pcrObject ->
        if (i != 0) Divider(modifier = Modifier.padding(vertical = 16.dp), color = DividerColor)
        Text(
            text = pcrObject.key,
            color = Primary,
            fontSize = FONTSIZE_XL,
            fontWeight = FontWeight.Bold,
        )
        pcrObject.values.forEach { p ->
            Row(modifier = Modifier.padding(top = 8.dp), verticalAlignment = Alignment.Top) {
                Text(modifier = Modifier
                    .padding(end = 6.dp)
                    .defaultMinSize(25.dp),
                    text = p.key,
                    color = Primary,
                    fontWeight = FontWeight.SemiBold)
                Text(text = p.value)
            }
        }
    }
    Spacer(modifier = Modifier.padding(16.dp))
}

@Composable
private fun Quote(quote: Quote?) {
    if (quote == null) {
        ErrorMsg(msg = "Claim does not contain a quote or the form is invalid.")
        return
    }

    val c = Primary
    TextWithSmallHeader(text = quote.digest, header = "PCR Digest", c = c)
    TextWithSmallHeader(text = quote.clock, header = "Clock", c = c)
    TextWithSmallHeader(text = quote.reset, header = "Reset", c = c)
    TextWithSmallHeader(text = quote.restart, header = "Restart", c = c)
    TextWithSmallHeader(text = quote.safe, header = "Safe", c = c)
    TextWithSmallHeader(text = quote.firmwareVersion, header = "Firmware version", c = c)
    TextWithSmallHeader(text = quote.extra, header = "Extra Data", c = c)
    TextWithSmallHeader(text = quote.magic, header = "Magic", c = c)
    TextWithSmallHeader(text = quote.type, header = "Type", c = c)
    TextWithSmallHeader(text = quote.signer, header = "Qualified Signer", c = c)
}

@Composable

private fun ErrorMsg(msg: String) {
    Text(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        text = msg,
        textAlign = TextAlign.Center,
    )
}