package com.example.mobileattester.ui.components.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * TODO More usable version
 */
@Composable
fun TextWithIconClickable(
    text: String,
    icon: ImageVector,
    color: Color = MaterialTheme.colors.primary,
    onClick: () -> Unit
) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp)
            .clickable {
                onClick()
            },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Text(
            AnnotatedString(text, SpanStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold)),
            color = color
        )
        Icon(icon, contentDescription = "")
    }
}

@Composable
fun TextWithIcon(
    icon: ImageVector,
    text: String? = null,
    color: Color = MaterialTheme.colors.primary,
) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (!text.isNullOrEmpty())
            Text(
                AnnotatedString(text, SpanStyle(fontSize = 24.sp)),
                color = color,
            )
        Spacer(modifier = Modifier.size(8.dp))
        Icon(icon, contentDescription = "", tint = color)
    }
}