package com.example.mobileattester.ui.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.mobileattester.data.model.CODE_RESULT_ERROR
import com.example.mobileattester.data.model.CODE_RESULT_OK
import com.example.mobileattester.data.model.CODE_RESULT_VERIFY_ERROR
import com.example.mobileattester.data.model.ElementResult
import com.example.mobileattester.data.network.Response
import com.example.mobileattester.data.network.Status
import com.example.mobileattester.ui.components.anim.FadeInWithDelay
import com.example.mobileattester.ui.components.common.ErrorIndicator
import com.example.mobileattester.ui.components.common.HeaderRoundedBottom
import com.example.mobileattester.ui.components.common.LoadingFullScreen
import com.example.mobileattester.ui.theme.*
import com.example.mobileattester.ui.util.*
import com.example.mobileattester.ui.viewmodel.AttestationViewModel
import compose.icons.TablerIcons
import compose.icons.tablericons.*
import kotlinx.coroutines.flow.MutableStateFlow
import java.text.DateFormat

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
            Result(result = it) {
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
                Result(res.data!!, ::navBack)
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
    onNavigateUp: () -> Unit,
) {
    println("Result render")
    FadeInWithDelay(50) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
        ) {
            HeaderRoundedBottom(getCodeColor(result.result)) {
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
                SpacerSmall()
                TextVertSpace(txt = "Result: ${result.result}")
                TextVertSpace(txt = "Message: ${result.ruleName}")
                TextVertSpace(txt = "Message: ${result.message}")
                TextVertSpace(txt = "Verified at: ${
                    getTimeFormatted(result.verifiedAt,
                        DatePattern.DateTime)
                }")
                Divider(
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp, horizontal = 16.dp),
                )
                TextVertSpace(txt = "ElementId: ${result.elementID}")
                TextVertSpace(txt = "PolicyId: ${result.policyID}")
                TextVertSpace(txt = "ClaimId: ${result.claimID}")
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
fun TextVertSpace(txt: String) {
    Text(modifier = Modifier.padding(vertical = 6.dp), text = txt)
}

