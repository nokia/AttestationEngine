package com.example.mobileattester.components.common

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.mobileattester.ui.theme.ELEVATION_SM
import com.example.mobileattester.ui.theme.White

@Composable
fun OutlinedIconButton(
    icon: ImageVector,
    color: Color = MaterialTheme.colors.primary,
    onClick: () -> Unit,
) {
    Surface(
        modifier = Modifier.clickable { onClick() },
        border = BorderStroke(1.dp, color),
        color = White,
        elevation = ELEVATION_SM,
    ) {
        Surface(color = White, modifier = Modifier.padding(14.dp)) {
            Icon(icon, contentDescription = "", tint = color, modifier = Modifier.size(32.dp))
        }
    }
}