package com.example.mobileattester.ui.components.common

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.mobileattester.ui.theme.ELEVATION_SM
import com.example.mobileattester.ui.theme.LightGrey
import com.example.mobileattester.ui.theme.Primary
import com.example.mobileattester.ui.theme.PrimaryDark

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
            val color = if (it == selected.value) Primary else PrimaryDark

            Row(Modifier
                .clickable {
                    onSelectionChanged(it)
                }
                .padding(8.dp)) {
                RadioButton(
                    selected = it == selected.value,
                    onClick = { onSelectionChanged(it) },
                )
                Text(
                    text = it,
                    modifier = Modifier.padding(start = 4.dp),
                    color = color,
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
        Row(Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center) {
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
                .border(width = 1.dp, color = LightGrey, shape = RoundedCornerShape(4.dp)),
        ) {
            Surface(modifier = Modifier.fillMaxWidth(), elevation = ELEVATION_SM) {
                Row(Modifier.fillMaxWidth()) {
                    Text(selectedValue.toString(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(onClick = { expanded = true })
                            .background(Color.White)
                            .padding(15.dp),
                        color = PrimaryDark)
                }
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier
                        .fillMaxWidth(0.95f)
                        .background(Color.White)
                        .border(1.dp, LightGrey),
                ) {
                    items.forEach { value ->
                        DropdownMenuItem(
                            onClick = {
                                onSelectionChanged(value)
                                expanded = false
                            },
                        ) {
                            val color = if (value == selectedValue) MaterialTheme.colors.primary
                            else MaterialTheme.colors.secondary

                            Text(
                                modifier = Modifier.padding(vertical = 4.dp),
                                text = value.toString(),
                                color = color,
                            )
                        }
                    }
                }
            }
        }

}
