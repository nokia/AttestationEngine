package com.example.mobileattester.ui.pages

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.mobileattester.data.util.AttestationUtil
import org.json.JSONObject


@Composable
fun Claim(
    navController: NavController,
    attestationUtil: AttestationUtil,
) {
    val claim = attestationUtil.claim.collectAsState()
    val formatted = JSONObject(claim.value?.data.toString()).toString(2)


    LazyColumn(Modifier.fillMaxWidth()) {
        item {
            Text(modifier = Modifier.padding(16.dp), text = formatted)
        }
    }
}