package com.example.mobileattester.ui.pages

import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.mobileattester.data.util.AttestationUtil
import com.example.mobileattester.ui.components.anim.FadeInWithDelay
import com.example.mobileattester.ui.components.common.HeaderRoundedBottom
import com.example.mobileattester.ui.theme.FONTSIZE_XXL
import com.example.mobileattester.ui.theme.White
import com.example.mobileattester.ui.util.getResultIcon
import compose.icons.TablerIcons
import compose.icons.tablericons.InfoSquare
import org.json.JSONObject


@Composable
fun Claim(
    navController: NavController,
    attestationUtil: AttestationUtil,
) {
    val claim = attestationUtil.claim.collectAsState()
    val formatted = remember {
        try {
            JSONObject(claim.value?.data.toString()).toString(2)
        } catch (e: Exception) {
            "err"
        }
    }

    FadeInWithDelay(50) {
        Column(Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()))
        {
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
            Text(modifier = Modifier.padding(16.dp), text = formatted)
        }
    }
}