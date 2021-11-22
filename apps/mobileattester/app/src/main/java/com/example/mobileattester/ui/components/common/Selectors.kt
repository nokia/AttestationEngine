package com.example.mobileattester.ui.components.common


import android.hardware.lights.Light
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.SnackbarDefaults.backgroundColor
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.CornerRadius.Companion.Zero
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import com.example.mobileattester.ui.theme.DarkGrey
import com.example.mobileattester.ui.theme.ELEVATION_SM
import com.example.mobileattester.ui.theme.LightGrey
import com.example.mobileattester.ui.theme.Primary
import compose.icons.TablerIcons
import compose.icons.tablericons.ChevronDown
import compose.icons.tablericons.ChevronUp
import kotlinx.coroutines.selects.select


/**
 * Simple RadioGroup, expects all of the selections to be unique.
 */
@Composable
fun SimpleRadioGroup(
    selections: List<String>,
    selected: MutableState<String>,
    vertical: Boolean = true,
    onSelectionChanged: (value: String) -> Unit,
) {
    @Composable
    fun CreateButtons() {
        selections.forEach {
            Row(
                Modifier.padding(8.dp)
            ) {
                RadioButton(selected = it == selected.value, onClick = { onSelectionChanged(it) })
                Text(
                    text = it,
                    modifier = Modifier
                        .clickable(onClick = { onSelectionChanged(it) })
                        .padding(start = 4.dp)
                )
                Spacer(modifier = Modifier.size(4.dp))
            }
        }
    }

    if (vertical) {
        Column {
            CreateButtons()
        }
    } else {
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            CreateButtons()
        }
    }
}

/**
 * Provides a simple selector, expects that items in the list are unique.
 */
@Composable
fun <T> DropDown(
    items: List<T>,
    selectedValue: T,
    onSelectionChanged: (T) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentSize(Alignment.TopStart)
            .border(width = 1.dp, color = LightGrey, shape = RoundedCornerShape(4.dp))
    ) {
        Surface(modifier = Modifier.fillMaxWidth(), elevation = ELEVATION_SM) {
            Row(Modifier.fillMaxWidth()) {
                Text(
                    selectedValue.toString(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = { expanded = true })
                        .background(
                            Color.White
                        )
                        .padding(15.dp)
                )
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Color.White
                    )
                    .border(1.dp, DarkGrey)
            ) {
                items.forEach { value ->
                    DropdownMenuItem(
                        onClick = {
                            onSelectionChanged(value)
                            expanded = false
                        },
                    ) {
                        val color =
                            if (value == selectedValue) MaterialTheme.colors.primary
                            else MaterialTheme.colors.secondary

                        Text(modifier = Modifier.padding(vertical = 4.dp), text = value.toString(), color = color)
                    }
                }
            }
        }
    }
}
