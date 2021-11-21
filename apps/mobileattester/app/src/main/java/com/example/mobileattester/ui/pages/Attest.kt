package com.example.mobileattester.ui.pages

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.mobileattester.R
import com.example.mobileattester.data.model.Element
import com.example.mobileattester.ui.components.common.DropDown
import com.example.mobileattester.ui.components.common.LoadingIndicator
import com.example.mobileattester.ui.components.common.SimpleRadioGroup
import com.example.mobileattester.ui.viewmodel.AttestationViewModel

/**
 * Provides attestation screen
 */
@Composable
fun Attest(navController: NavController, viewModel: AttestationViewModel) {
    val clickedElementId =
        navController.currentBackStackEntry?.arguments?.get(ARG_ITEM_ID).toString()

    val element = viewModel.getElementFromCache(clickedElementId) ?: run {
        Text(text = "Error getting element data for id $clickedElementId")
        return
    }

    val attestType = remember {
        mutableStateOf("Attest")
    }
    val policyDropDown = remember {
        mutableStateOf("Value 1")
    }
    val ruleDropDown = remember {
        mutableStateOf("Value 4")
    }
    val loading = remember {
        mutableStateOf(false)
    }

    if (loading.value) {
        LoadingIndicator()
        return
    }

    AttestationConfig(element, attestType, policyDropDown, ruleDropDown) {
        loading.value = true
    }
}

@Composable
private fun AttestationConfig(
    element: Element,
    attestType: MutableState<String>,
    policyDropDown: MutableState<String>,
    ruleDropDown: MutableState<String>,
    onSubmit: () -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 24.dp)) {

        // Element data
        Text(text = element.name, style = MaterialTheme.typography.h2)
        Text(
            text = element.endpoint,
            style = MaterialTheme.typography.body1,
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 2.dp)
        )

        // Attestation type
        Spacer(modifier = Modifier.size(24.dp))
        SimpleRadioGroup(
            selections = listOf(
                stringResource(id = R.string.attest),
                stringResource(id = R.string.attest_verify)
            ),
            selected = attestType,
            onSelectionChanged = { attestType.value = it },
            vertical = false
        )

        // Policy
        Spacer(modifier = Modifier.size(24.dp))
        Text(text = "Select policy")
        DropDown(
            items = listOf(
                "Value 1",
                "Value 2",
                " Value 3",
                " Value 4",
                " Value 5",
                " Value 6",
                " Value 7",
                "Value 8",
            ),
            selectedValue = policyDropDown.value,
            onSelectionChanged = {
                policyDropDown.value = it
            }
        )

        // Rule
        Spacer(modifier = Modifier.size(24.dp))
        Text(text = "Select rule")
        DropDown(
            items = listOf(
                "Value 1",
                "Value 2",
                " Value 3",
                " Value 4",
                " Value 5",
                " Value 6",
                " Value 7",
                "Value 8",
            ),
            selectedValue = ruleDropDown.value,
            onSelectionChanged = {
                ruleDropDown.value = it
            }
        )

        // Submit
        Spacer(modifier = Modifier.size(24.dp))
        Button(
            onClick = { onSubmit() }) {
            Text(text = "Submit", color = Color.White)
        }
    }
}