package com.example.mobileattester.ui.components.common

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun LoadingIndicator() {
    CircularProgressIndicator(
        modifier = Modifier
            .size(32.dp),
        color = MaterialTheme.colors.primary,
    )
}

@Composable
fun ErrorIndicator(msg: String) {
    Row(modifier = Modifier
        .fillMaxWidth()
        .padding(32.dp)) {
        Text(text = msg)
    }
}