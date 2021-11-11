package com.example.mobileattester.ui.pages

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.mobileattester.R
import com.example.mobileattester.data.model.Element
import com.example.mobileattester.data.network.Response
import com.example.mobileattester.data.network.Status
import com.example.mobileattester.ui.components.SearchBar
import com.example.mobileattester.ui.components.Tag
import com.example.mobileattester.ui.components.common.HeaderRoundedBottom
import com.example.mobileattester.ui.components.common.TextClickableWithIcon
import com.example.mobileattester.ui.theme.DividerColor
import com.example.mobileattester.ui.util.Screen
import com.example.mobileattester.ui.viewmodel.AttestationViewModel
import compose.icons.TablerIcons
import compose.icons.tablericons.ChevronDown
import compose.icons.tablericons.ChevronRight

@Composable
fun Elements(navController: NavController, viewModel: AttestationViewModel) {
    val eIds: Response<List<String>> by viewModel.getElementIds()
        .observeAsState(Response(status = Status.LOADING))
    println("Eids null?: ${eIds.data == null}")

    val coroutineScope = rememberCoroutineScope()
    val (loadResult, setLoadResult) = remember {
        mutableStateOf<MutableMap<String, Element>>(
            mutableMapOf()
        )
    }

    fun onElementClicked(itemid: String) {
        navController.navigate(Screen.Element.route)
    }

    LazyColumn() {
        item {
            ElementListHeader()
            Spacer(modifier = Modifier.size(5.dp))
        }

        if (eIds.data != null) {
            itemsIndexed(eIds.data!!) { index, eid ->
                println(index)
                loadResult[eid]?.let { element ->
                    Column(Modifier.padding(horizontal = 12.dp)) {
                        ElementListItem(element, onElementClick = ::onElementClicked)
                        Divider(modifier = Modifier.fillMaxWidth(), color = DividerColor)
                    }
                }
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
    element: Element,
    onElementClick: (id: String) -> Unit,
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp, bottom = 18.dp)
            .clickable {
                onElementClick("itemid")
            },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row {
            Column {
                Text(
                    text = element.name,
                    style = MaterialTheme.typography.h2
                )
                Spacer(modifier = Modifier.size(10.dp))
                Row {
                    // TODO Tags
                    Tag(text = "element.types")
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

