package com.example.mobileattester.ui.components.common

import androidx.compose.foundation.layout.size
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
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