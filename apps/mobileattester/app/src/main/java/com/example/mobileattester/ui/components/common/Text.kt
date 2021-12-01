package com.example.mobileattester.ui.components.common

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mobileattester.ui.theme.FONTSIZE_XL
import com.example.mobileattester.ui.theme.FONTSIZE_XS
import com.example.mobileattester.ui.theme.LightGrey
import com.example.mobileattester.ui.theme.PrimaryDark
import compose.icons.TablerIcons
import compose.icons.tablericons.ChevronRight

/**
 * TODO More usable version
 */
@Composable
fun TextWithIconClickable(
    text: String,
    icon: ImageVector,
    color: Color = MaterialTheme.colors.primary,
    onClick: () -> Unit,
) {
    Row(Modifier
        .fillMaxWidth()
        .padding(horizontal = 24.dp, vertical = 8.dp)
        .clickable {
            onClick()
        },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center) {
        Text(AnnotatedString(text, SpanStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold)),
            color = color)
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
        if (!text.isNullOrEmpty()) Text(
            AnnotatedString(text, SpanStyle(fontSize = FONTSIZE_XL)),
            color = color,
        )
        Spacer(modifier = Modifier.size(8.dp))
        Icon(icon, contentDescription = "", tint = color)
    }
}

@Composable
fun DecorText(txt: String, color: Color, bold: Boolean = false) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Column() {
            Box(modifier = Modifier.size(5.dp))
            Box(modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color))
        }
        Text(modifier = Modifier.padding(top = 4.dp, start = 4.dp),
            text = txt,
            color = color,
            fontWeight = if (!bold) FontWeight.Normal else FontWeight.Bold)
    }
}


@Composable
fun TextWithSmallHeader(
    text: String,
    header: String,
    truncate: Boolean = false,
    c: Color? = null,
    icon: ImageVector? = null,
    onClick: (() -> Unit)? = null,
) {
    @Composable
    fun content() {
        Column() {
            Text(
                modifier = Modifier.padding(bottom = 3.dp),
                text = header,
                fontSize = FONTSIZE_XS,
                color = c ?: LightGrey,
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = PrimaryDark,
                    )
                }
                Text(
                    text = text,
                    Modifier.padding(start = if (icon == null) 0.dp else 8.dp),
                    overflow = TextOverflow.Ellipsis,
                    maxLines = if (truncate) 1 else 10000,
                )
            }
        }
    }

    when (onClick) {
        null -> Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
        ) {
            content()
        }
        else -> Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClick() }
                .padding(vertical = 8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom,
            ) {
                content()
                Icon(
                    imageVector = TablerIcons.ChevronRight,
                    contentDescription = null,
                    tint = PrimaryDark,
                )
            }
        }
    }
}

