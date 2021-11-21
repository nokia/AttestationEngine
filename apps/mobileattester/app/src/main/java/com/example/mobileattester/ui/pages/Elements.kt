package com.example.mobileattester.ui.pages

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.core.os.bundleOf
import androidx.navigation.NavController
import com.example.mobileattester.R
import com.example.mobileattester.data.model.Element
import com.example.mobileattester.data.network.Status
import com.example.mobileattester.ui.components.SearchBar
import com.example.mobileattester.ui.components.TagRow
import com.example.mobileattester.ui.components.common.ErrorIndicator
import com.example.mobileattester.ui.components.common.HeaderRoundedBottom
import com.example.mobileattester.ui.theme.DarkGrey
import com.example.mobileattester.ui.theme.DividerColor
import com.example.mobileattester.ui.util.Screen
import com.example.mobileattester.ui.util.navigate
import com.example.mobileattester.ui.viewmodel.AttestationViewModel
import com.example.mobileattester.ui.viewmodel.AttestationViewModelImpl.Companion.FETCH_START_BUFFER
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import compose.icons.TablerIcons
import compose.icons.tablericons.ChevronRight

@Composable
fun Elements(navController: NavController, viewModel: AttestationViewModel) {
    val response = viewModel.elementFlowResponse.collectAsState().value

    when (response.status) {
        Status.ERROR -> ErrorIndicator(msg = response.message.toString())
        else -> RenderElementList(navController, viewModel)
    }

}

@Composable
private fun RenderElementList(navController: NavController, viewModel: AttestationViewModel) {

    // Navigate to single element view, pass clicked id as argument
    fun onElementClicked(itemid: String) {
        navController.navigate(Screen.Element.route, bundleOf(Pair(ARG_ITEM_ID, itemid)))
    }

    val elements = viewModel.elementFlowResponse.collectAsState().value.data ?: listOf()
    val lastIndex = viewModel.elementFlowResponse.collectAsState().value.data?.lastIndex ?: 0

    val isRefreshing = viewModel.isRefreshing.collectAsState()
    val filters = remember { mutableStateOf(TextFieldValue()) }
    val isLoading = viewModel.isLoading.collectAsState()

    SwipeRefresh(
        state = rememberSwipeRefreshState(isRefreshing.value),
        onRefresh = { viewModel.refreshElements() },
    ) {
        LazyColumn() {
            // Header
            item {
                HeaderRoundedBottom {
                    SearchBar(filters, stringResource(id = R.string.placeholder_search_elementlist))
                }
                Spacer(modifier = Modifier.size(5.dp))
            }

            // List of the elements
            itemsIndexed(if (filters.value.text.isEmpty()) elements else viewModel.filterElements(
                filters.value.text)) { index, element ->
                if (index + FETCH_START_BUFFER >= lastIndex) {
                    viewModel.getMoreElements()
                }

                Column(Modifier.padding(horizontal = 12.dp)) {
                    ElementListItem(element, onElementClick = ::onElementClicked)
                    Divider(modifier = Modifier.fillMaxWidth(), color = DividerColor)
                }
            }

            // Footer
            item {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalArrangement = Arrangement.Center) {
                    if (isLoading.value) {
                        CircularProgressIndicator(modifier = Modifier.size(32.dp),
                            color = MaterialTheme.colors.primary)
                    } else {
                        Text(text = "All elements loaded", color = DarkGrey)
                    }
                }
            }
        }
    }
}

@Composable
private fun ElementListItem(
    element: Element,
    onElementClick: (id: String) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onElementClick(element.itemid)
            }
            .padding(top = 12.dp, bottom = 18.dp, start = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row {
            Column {
                Text(text = element.name, style = MaterialTheme.typography.h2)
                Spacer(modifier = Modifier.size(10.dp))
                Row {
                    TagRow(tags = element.types)
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

