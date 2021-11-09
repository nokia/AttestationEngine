package com.example.mobileattester.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.mobileattester.ui.theme.*

/**
 * Creates (a) row(s) of provided tags.
 *
 * Orders the tags alphabetically and wraps tags on a new row if
 * all of them can't fit on a single one.
 * TODO Sorting + wrapping
 */
@Composable
fun TagRow(
    tags: List<String>,
) {
    Row() {
        tags.sorted().forEach {
            Tag(text = it)
            Spacer(modifier = Modifier.size(4.dp))
        }
    }
}

/**
 * Composable for a single tag
 */
@Composable
fun Tag(
    text: String,
) {
    Surface(
        elevation = ELEVATION_XS,
        shape = ROUNDED_MD,
//        modifier = Modifier.border(
//            width = 1.dp,
//            color = MaterialTheme.colors.primary,
//            shape = ROUNDED_SM,
//        ),
        color = DarkGrey,
    ) {
        Text(modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
            text = text,
            color = White,
            fontSize = FONTSIZE_SM,
            fontWeight = FontWeight.Medium)
    }
}