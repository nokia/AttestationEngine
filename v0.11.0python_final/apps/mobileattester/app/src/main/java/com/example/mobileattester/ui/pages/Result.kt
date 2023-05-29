package com.example.mobileattester.ui.pages

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.os.bundleOf
import androidx.navigation.NavController
import com.example.mobileattester.data.model.ElementResult
import com.example.mobileattester.data.network.Response
import com.example.mobileattester.data.network.Status
import com.example.mobileattester.ui.components.anim.FadeInWithDelay
import com.example.mobileattester.ui.components.common.ErrorIndicator
import com.example.mobileattester.ui.components.common.HeaderRoundedBottom
import com.example.mobileattester.ui.components.common.LoadingFullScreen
import com.example.mobileattester.ui.components.common.TextWithSmallHeader
import com.example.mobileattester.ui.theme.*
import com.example.mobileattester.ui.util.*
import com.example.mobileattester.ui.viewmodel.AttestationViewModel
import compose.icons.TablerIcons
import compose.icons.tablericons.DeviceDesktop
import compose.icons.tablericons.Id
import compose.icons.tablericons.QuestionMark
import kotlinx.coroutines.flow.MutableStateFlow

const val ARG_RESULT_ID = "arg_result_id"

/**
 * Provides different ways to use result screen.
 * Sometimes the result is already in cache and sometimes still loading.
 */
@Composable
fun ResultScreenProvider(
    viewModel: AttestationViewModel,
    navController: NavController,
    resultFlow: MutableStateFlow<Response<ElementResult>?>? = null,
) {
    fun navBack() {
        navController.navigateUp()
    }

    // --------------------------------------------------

    // Check first if there is id provided in the arguments
    navController.currentBackStackEntry?.arguments?.getString(ARG_RESULT_ID)?.let { id ->
        viewModel.findElementResult(id)?.let {
            Result(result = it, viewModel = viewModel, navController = navController) {
                // Clear the arg on navigate out
                navController.currentBackStackEntry?.arguments?.remove(ARG_RESULT_ID)
                navBack()
            }
        } ?: Text(text = "Element result was null")
        return
    }

    // --------------------------------------------------

    val res = resultFlow?.collectAsState()?.value
    when (res?.status) {
        Status.LOADING -> LoadingFullScreen()
        Status.ERROR -> ErrorIndicator(msg = "Error loading result")
        Status.SUCCESS -> {
            FadeInWithDelay(50) {
                Result(res.data!!, viewModel, navController, ::navBack)
            }
            return
        }
        else -> {
        }
    }

    // --------------------------------------------------

    FadeInWithDelay(2000) {
        Text(text = "Result data not found")
    }
}

@Composable
fun Result(
    result: ElementResult,
    viewModel: AttestationViewModel,
    navController: NavController,
    onNavigateUp: () -> Unit,
) {
    val element = remember {
        viewModel.getElementFromCache(result.elementID)
    }
    val policy = remember {
        viewModel.attestationUtil.getPolicyFromCache(result.policyID)
    }
    val color = remember {
        getCodeColor(result.result)
    }

    fun navElement() = navController.navigate(Screen.Element.route,
        bundleOf(Pair(ARG_ELEMENT_ID, element?.itemid)))

    fun navPolicy() =
        navController.navigate(Screen.Policy.route, bundleOf(Pair(ARG_POLICY_ID, policy?.itemid)))

    fun navClaim() =
        navController.navigate(Screen.Claim.route, bundleOf(Pair(ARG_CLAIM_ID, result.claimID)))


    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
    ) {
        // Header
        HeaderRoundedBottom(color) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 24.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    getResultIcon(result),
                    contentDescription = null,
                    tint = White,
                    modifier = Modifier.size(40.dp),
                )
                Spacer(modifier = Modifier.size(8.dp))
                Text(
                    text = result.ruleName,
                    color = White,
                    fontSize = FONTSIZE_XXL,
                    fontWeight = FontWeight.Bold,
                )
            }
        }

        // Content
        Column(Modifier.padding(horizontal = 16.dp)) {
            // Top part
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(horizontalAlignment = Alignment.Start) {
                    Text(
                        text = "Result ${result.result}",
                        fontWeight = FontWeight.Bold,
                        fontSize = FONTSIZE_XXL,
                        color = color,
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = getTimeFormatted(result.verifiedAt, DatePattern.DateWithYear),
                        fontWeight = FontWeight.Bold,
                        fontSize = FONTSIZE_XXL,
                        color = getCodeColor(result.result),
                    )
                    Text(
                        modifier = Modifier.padding(end = 4.dp),
                        text = getTimeFormatted(result.verifiedAt, DatePattern.TimeOnly),
                        color = LightGrey,
                        fontSize = FONTSIZE_LG,
                    )
                }
            }
            TextWithSmallHeader(text = result.message, header = "msg", c = color)

            Div()

            // Related element + policy + claim
            TextWithSmallHeader(text = element?.name ?: "ERROR",
                header = "elmnt.",
                icon = TablerIcons.DeviceDesktop,
                c = color,
                onClick = { navElement() })
            TextWithSmallHeader(text = policy?.name ?: "ERROR",
                header = "policy",
                icon = TablerIcons.QuestionMark,
                c = color,
                onClick = { navPolicy() })
            TextWithSmallHeader(text = result.claimID,
                header = "claim",
                icon = TablerIcons.Id,
                truncate = true,
                c = color,
                onClick = { navClaim() })

            Div()

            Button(
                modifier = Modifier
                    .align(CenterHorizontally)
                    .padding(24.dp),
                onClick = { onNavigateUp() },
            ) {
                Text(text = "Close")
            }
        }
    }

}

@Composable
fun Div() {
    Divider(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        color = DividerColor,
    )
}
