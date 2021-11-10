package com.example.mobileattester.ui.pages

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.mobileattester.R
import com.example.mobileattester.ui.components.SearchBar
import com.example.mobileattester.ui.components.Tag
import com.example.mobileattester.ui.components.common.HeaderRoundedBottom
import com.example.mobileattester.ui.components.common.TextClickableWithIcon
import com.example.mobileattester.ui.theme.DividerColor
import com.example.mobileattester.ui.util.Screen
import compose.icons.TablerIcons
import compose.icons.tablericons.ChevronDown
import compose.icons.tablericons.ChevronRight

@Composable
fun Elements(navController: NavController) {
    fun onElementClicked() {
        navController.navigate(Screen.Element.route)
    }

    LazyColumn() {
        item {
            ElementListHeader()
            Spacer(modifier = Modifier.size(5.dp))
        }
        items(40) {
            Column(Modifier.padding(horizontal = 12.dp)) {
                ElementListItem() {
                    onElementClicked()
                }
                Divider(modifier = Modifier.fillMaxWidth(), color = DividerColor)
            }
        }
    }

}

@Composable
private fun ElementListHeader() {
    val text = remember {
        mutableStateOf(TextFieldValue())
    }

    HeaderRoundedBottom {
        SearchBar(text, stringResource(id = R.string.placeholder_search_elementlist))
        TextClickableWithIcon(text = stringResource(id = R.string.elementlist_additional),
            icon = TablerIcons.ChevronDown,
            onClick = {
                // TODO
            })
    }
}

@Composable
fun ElementListItem(
    onElementClick: (id: String) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp, bottom = 18.dp)
            .clickable {
                onElementClick("123") // TODO
            },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row {
            Column {
                Text(text = "Element", style = MaterialTheme.typography.h2)
                Spacer(modifier = Modifier.size(10.dp))
                Row {
                    // TODO Tags
                    Tag(text = "Tag for element")
                }
            }
        }
        Icon(
            imageVector = TablerIcons.ChevronRight,
            contentDescription = "",
            tint = MaterialTheme.colors.secondary,
        )
    }
}

