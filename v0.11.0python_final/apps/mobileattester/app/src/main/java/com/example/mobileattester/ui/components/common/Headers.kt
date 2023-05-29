package com.example.mobileattester.ui.components.common

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.mobileattester.ui.theme.Primary
import com.example.mobileattester.ui.theme.White

/**
 * Provides basic header template, with no other content than the rounded bottom corners.
 */
@Composable
fun HeaderRoundedBottom(
    color: Color = Primary,
    content: @Composable() () -> Unit = {},
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = color,
    ) {
        Column() {
            content()

            // Rounded corners for the bottom
            Surface(modifier = Modifier
                .fillMaxWidth()
                .height(10.dp),
                color = White,
                shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)) {}
        }
    }
}