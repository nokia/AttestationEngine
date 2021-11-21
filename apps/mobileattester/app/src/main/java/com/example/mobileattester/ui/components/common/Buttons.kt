package com.example.mobileattester.ui.components.common

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mobileattester.ui.theme.ELEVATION_SM
import com.example.mobileattester.ui.theme.ROUNDED_MD
import com.example.mobileattester.ui.theme.White
import com.example.mobileattester.ui.util.`if`

@Composable
fun OutlinedIconButton(
    icon: ImageVector? = null,
    text: String? = null,
    color: Color = MaterialTheme.colors.primary,
    filled: Boolean = false,
    rounded: Boolean = false,
    aspectRatio: Float? = null,
    width: Dp? = null,
    height: Dp? = null,
    border: BorderStroke? = BorderStroke(1.dp, color),
    onClick: (() -> Unit)? = null,
) {
    Surface(
        modifier = Modifier
            .clickable { onClick?.invoke() },
        border = border,
        color = if (filled) color else White,
        elevation = if (onClick != null) ELEVATION_SM else 0.dp,
        shape = if (rounded) ROUNDED_MD else CutCornerShape(0.dp),
    ) {
        Row(
            horizontalArrangement = if (icon != null && !text.isNullOrEmpty())
                Arrangement.SpaceBetween else Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(if (border != null) 14.dp else 0.dp)
                .`if`(aspectRatio != null) { aspectRatio(aspectRatio!!) }
                .`if`(width != null) { width(width!!) }
                .`if`(height != null) { height(height!!) }
        ) {
            if (icon != null)
                Icon(icon,
                    contentDescription = "",
                    tint = if (filled) Color.White else color,
                    modifier = Modifier.size(height?.plus(100.dp) ?: 32.dp))
            if (icon != null && !text.isNullOrEmpty()) Spacer(modifier = Modifier.size(8.dp))
            if (!text.isNullOrEmpty())
                Text(AnnotatedString(text,
                    SpanStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold)),
                    color = if (filled) Color.White else color)
        }

    }
}