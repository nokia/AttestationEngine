package com.example.mobileattester.ui.pages

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.mobileattester.data.model.ElementResult
import com.example.mobileattester.data.network.Response
import com.example.mobileattester.data.network.Status
import com.example.mobileattester.ui.components.anim.FadeInWithDelay
import com.example.mobileattester.ui.components.common.ErrorIndicator
import com.example.mobileattester.ui.components.common.HeaderRoundedBottom
import com.example.mobileattester.ui.components.common.LoadingFullScreen
import com.example.mobileattester.ui.theme.*
import com.example.mobileattester.ui.util.DatePattern
import com.example.mobileattester.ui.util.getCodeColor
import com.example.mobileattester.ui.util.getResultIcon
import com.example.mobileattester.ui.util.getTimeFormatted
import com.example.mobileattester.ui.viewmodel.AttestationViewModel
import compose.icons.TablerIcons
import compose.icons.tablericons.ChevronRight
import compose.icons.tablericons.DeviceDesktop
import compose.icons.tablericons.Id
import compose.icons.tablericons.QuestionMark
import kotlinx.coroutines.flow.MutableStateFlow

const val ARG_RESULT_ID = "arg_result_id"

/**
 * Provides different ways to use result screen.
 * Sometimes the result is already in cache and sometimes still loading.
 * TODO Fix
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
            Result(result = it, viewModel) {
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
                Result(res.data!!, viewModel, ::navBack)
            }
            return
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
    onNavigateUp: () -> Unit,
) {
    val element = viewModel.getElementFromCache(result.elementID)
    val policy = viewModel.useAttestationUtil().getPolicyFromCache(result.policyID)
    val color = getCodeColor(result.result)

    FadeInWithDelay(50) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
        ) {
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
            Column(Modifier.padding(horizontal = 16.dp)) {
                Spacer(modifier = Modifier.size(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column(horizontalAlignment = Alignment.Start) {
                        Text(text = "Result ${result.result}",
                            fontWeight = FontWeight.Bold,
                            fontSize = FONTSIZE_XXL,
                            color = color)
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

                TextSmallH(text = result.message, header = "msg", c = color)

                Div()

                TextSmallH(text = element?.name ?: "ERROR",
                    header = "elmnt.",
                    icon = TablerIcons.DeviceDesktop,
                    c = color) {}
                TextSmallH(text = policy?.name ?: "ERROR",
                    header = "policy",
                    icon = TablerIcons.QuestionMark,
                    c = color) {}
                TextSmallH(text = result.claimID,
                    header = "claim",
                    icon = TablerIcons.Id,
                    truncate = true,
                    c = color) {}

                Div()
                Text(text = "Other stuff here")

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

@Composable
private fun TextSmallH(
    text: String,
    header: String,
    truncate: Boolean = false,
    c: Color? = null,
    icon: ImageVector? = null,
    onClick: (() -> Unit)? = null,

    ) {

    @Composable
    fun content() {
        Column() {
            Text(
                modifier = Modifier.padding(bottom = 3.dp),
                text = header,
                fontSize = FONTSIZE_XS,
                color = c ?: LightGrey,
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = PrimaryDark,
                    )
                }
                Text(
                    text = text,
                    Modifier.padding(start = if (icon == null) 0.dp else 8.dp),
                    overflow = TextOverflow.Ellipsis,
                    maxLines = if (truncate) 1 else 10000,
                )
            }
        }
    }

    when (onClick) {
        null -> Column(modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)) {
            content()
        }
        else -> Column(modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom,
            ) {
                content()
                Icon(
                    imageVector = TablerIcons.ChevronRight,
                    contentDescription = null,
                    tint = PrimaryDark,
                )
            }
        }
    }
}

