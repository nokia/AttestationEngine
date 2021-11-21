package com.example.mobileattester.ui.pages

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.mobileattester.R
import com.example.mobileattester.data.model.Element
import com.example.mobileattester.data.model.Policy
import com.example.mobileattester.data.model.Rule
import com.example.mobileattester.ui.components.common.DropDown
import com.example.mobileattester.ui.components.common.SimpleRadioGroup
import com.example.mobileattester.ui.viewmodel.AttestationViewModel
import kotlinx.coroutines.launch

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

    val policies = viewModel.useAttestationUtil().policyFlow.collectAsState().value.data ?: listOf()
    val selectedPolicy = remember {
        mutableStateOf("")
    }

    val rules = viewModel.useAttestationUtil().ruleFlow.collectAsState().value.data ?: listOf()
    val selectedRule = remember {
        mutableStateOf("")
    }

    val scope = rememberCoroutineScope()

    AttestationConfig(element, attestType, policies, selectedPolicy, rules, selectedRule) {
        scope.launch {
            viewModel.useAttestationUtil().attest(element.itemid, selectedPolicy.value)
        }
    }
}

@Composable
private fun AttestationConfig(
    element: Element,
    attestType: MutableState<String>,
    policies: List<Policy>,
    selectedPolicy: MutableState<String>,
    rules: List<Rule>,
    selectedRule: MutableState<String>,
    onSubmit: () -> Unit,
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
            items = policies.map { it.itemid },
            selectedValue = policies.find { it.itemid == selectedPolicy.value },
            onSelectionChanged = {
                selectedPolicy.value = it.toString()
            }
        )

        // Rule
        Spacer(modifier = Modifier.size(24.dp))
        Text(text = "Select rule")
        DropDown(
            items = rules,
            selectedValue = selectedRule.value,
            onSelectionChanged = {
                selectedRule.value = it.toString()
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