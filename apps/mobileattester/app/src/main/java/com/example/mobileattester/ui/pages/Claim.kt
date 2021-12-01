package com.example.mobileattester.ui.pages

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.ScrollableState
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.mobileattester.data.model.Claim
import com.example.mobileattester.data.util.AttestationUtil
import com.example.mobileattester.ui.components.anim.FadeInWithDelay
import com.example.mobileattester.ui.components.common.DecorText
import com.example.mobileattester.ui.components.common.HeaderRoundedBottom
import com.example.mobileattester.ui.components.common.TextWithSmallHeader
import com.example.mobileattester.ui.theme.FONTSIZE_XXL
import com.example.mobileattester.ui.theme.Primary
import com.example.mobileattester.ui.theme.White
import com.example.mobileattester.ui.util.DatePattern
import com.example.mobileattester.ui.util.getTimeFormatted
import compose.icons.TablerIcons
import compose.icons.tablericons.InfoSquare
import org.json.JSONObject

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
            Claim(claim = claim)
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
        Claim(claim = it)
        return@ClaimWrapper
    }


    FadeInWithDelay(2000) {
        Text(text = "Claim data not found")
    }
}


@Composable
fun Claim(
    claim: Claim,
) {
    FadeInWithDelay(50) {
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
                TextWithSmallHeader(
                    text = claim.itemid,
                    header = "ID"
                )
                TextWithSmallHeader(
                    text = getTimeFormatted(
                        claim.getTimestamps().first?.time.toString(),
                        DatePattern.DateTimeMs,
                    ),
                    header = "Requested"
                )
                TextWithSmallHeader(
                    text = getTimeFormatted(
                        claim.getTimestamps().second?.time.toString(),
                        DatePattern.DateTimeMs,
                    ),
                    header = "Received"
                )
                Div()
            }
        }
    }
}
