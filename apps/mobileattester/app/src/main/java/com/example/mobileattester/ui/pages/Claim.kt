package com.example.mobileattester.ui.pages

import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.mobileattester.data.util.AttestationUtil
import com.example.mobileattester.ui.components.anim.FadeInWithDelay
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
            Text(modifier = Modifier.padding(16.dp), text = formatted)
        }
    }
}