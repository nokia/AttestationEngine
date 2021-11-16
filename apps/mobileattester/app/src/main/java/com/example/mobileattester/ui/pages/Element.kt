package com.example.mobileattester.ui.pages

import androidx.compose.foundation.layout.*
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.mobileattester.ui.components.TagRow
import com.example.mobileattester.ui.components.common.HeaderRoundedBottom
import com.example.mobileattester.ui.components.common.OutlinedIconButton
import com.example.mobileattester.ui.components.common.TextWithIcon
import com.example.mobileattester.ui.theme.DarkGrey
import com.example.mobileattester.ui.theme.Error
import com.example.mobileattester.ui.theme.Ok
import com.example.mobileattester.ui.theme.PrimaryDark
import com.example.mobileattester.ui.viewmodel.AttestationViewModel
import compose.icons.TablerIcons
import compose.icons.tablericons.EyeCheck
import compose.icons.tablericons.ListCheck
import compose.icons.tablericons.ZoomCheck

const val ARG_ITEM_ID = "item_id"

@Composable
fun Element(navController: NavController, viewModel: AttestationViewModel) {
    val clickedElementId =
        navController.currentBackStackEntry?.arguments?.get(ARG_ITEM_ID).toString()
    val element = viewModel.getElementFromCache(clickedElementId)

    if (element == null) {
        Text(text = "Error...", style = MaterialTheme.typography.h3)
        return
    }

    Column() {
        // Render element header
        HeaderRoundedBottom {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 24.dp)) {
                Text(text = element.name, style = MaterialTheme.typography.h3)
                Text(text = element.endpoint,
                    style = MaterialTheme.typography.body1,
                    modifier = Modifier.padding(vertical = 8.dp, horizontal = 2.dp))
            }
        }

        // Content
        Column(Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
            TagRow(element.types)
            Spacer(modifier = Modifier.size(26.dp))
            ElementActions()
            Spacer(modifier = Modifier.size(26.dp))
            Text(
                text = element.description ?: "",
                color = DarkGrey,
            )
            Spacer(modifier = Modifier.size(26.dp))
            Divider(color = Color(197, 197, 197), thickness = 1.dp)
            Spacer(modifier = Modifier.size(26.dp))
            ElementResult()
        }

    }
}

@Composable
@Preview
fun ElementResult() {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text("Results", fontSize = 24.sp)
        TextWithIcon(text = "24h", icon = TablerIcons.ListCheck, color = PrimaryDark)
    }
    SpacerSmall()
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
        OutlinedIconButton(text = "5",
            rounded = true,
            width = 20.dp,
            height = 20.dp,
            color = MaterialTheme.colors.primary,
            filled = true
        ) // TODO: Use AspectRatio
        OutlinedIconButton(text = "4",
            rounded = true,
            width = 20.dp,
            height = 20.dp,
            color = Ok,
            filled = true
        ) // TODO: Use AspectRatio
        OutlinedIconButton(text = "1",
            rounded = true,
            width = 20.dp,
            height = 20.dp,
            color = Error,
            filled = true
        ) // TODO: Use AspectRatio
    }
}

@Composable
fun ElementActions() {
    Row(
        Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        OutlinedIconButton(TablerIcons.EyeCheck, text = "Attest") {}
        SpacerSmall()
        OutlinedIconButton(TablerIcons.ZoomCheck, text = "Verify") {}
    }
}


@Composable
fun SpacerSmall() {
    Spacer(modifier = Modifier.size(8.dp))
}