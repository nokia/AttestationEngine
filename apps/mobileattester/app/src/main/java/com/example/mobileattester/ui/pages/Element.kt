package com.example.mobileattester.ui.pages

import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.mobileattester.ui.components.TagRow
import com.example.mobileattester.ui.components.common.HeaderRoundedBottom
import com.example.mobileattester.ui.components.common.OutlinedIconButton
import com.example.mobileattester.ui.theme.DarkGrey
import com.example.mobileattester.ui.theme.Error
import com.example.mobileattester.ui.theme.Ok
import com.example.mobileattester.ui.viewmodel.AttestationViewModel
import compose.icons.TablerIcons
import compose.icons.tablericons.Checkbox
import compose.icons.tablericons.Checks
import compose.icons.tablericons.Edit
import compose.icons.tablericons.Trash

const val ARG_ITEM_ID = "item_id"

@Composable
fun Element(navController: NavController, viewModel: AttestationViewModel) {
    val clickedElementId =
        navController.currentBackStackEntry?.arguments?.get(ARG_ITEM_ID).toString()

    val element = viewModel.getElement(clickedElementId)

    if (element == null) {
        Text(text = "Error...", style = MaterialTheme.typography.h3)
        return
    }

    Column() {
        HeaderRoundedBottom {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 24.dp)) {
                Text(text = element.name, style = MaterialTheme.typography.h3)
                Text(text = element.endpoint,
                    style = MaterialTheme.typography.body1,
                    modifier = Modifier.padding(vertical = 8.dp, horizontal = 2.dp))
            }
        }

        Column(Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
            TagRow(element.types)
            Spacer(modifier = Modifier.size(26.dp))
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                OutlinedIconButton(TablerIcons.Checkbox, color = Ok) {}
                SpacerSmall()
                OutlinedIconButton(TablerIcons.Checks, color = Ok) {}
                SpacerSmall()
                OutlinedIconButton(TablerIcons.Edit, color = DarkGrey) {}
                SpacerSmall()
                OutlinedIconButton(TablerIcons.Trash, color = Error) {}
            }
            Spacer(modifier = Modifier.size(26.dp))

            Text(
                text = element.description ?: "",
                color = DarkGrey,
            )
        }

    }
}

@Composable
fun ElementResult() {

}

@Composable
fun SpacerSmall() {
    Spacer(modifier = Modifier.size(8.dp))
}