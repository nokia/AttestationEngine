package com.example.mobileattester.ui.pages

import android.transition.Fade
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.mobileattester.data.model.CODE_RESULT_OK
import com.example.mobileattester.data.model.ElementResult
import com.example.mobileattester.data.network.Response
import com.example.mobileattester.data.network.Status
import com.example.mobileattester.data.util.AttestationUtil
import com.example.mobileattester.ui.components.anim.FadeInWithDelay
import com.example.mobileattester.ui.components.common.ErrorIndicator
import com.example.mobileattester.ui.components.common.LoadingFullScreen
import com.example.mobileattester.ui.components.common.LoadingIndicator
import com.example.mobileattester.ui.theme.DarkGrey
import com.example.mobileattester.ui.theme.FONTSIZE_XL
import com.example.mobileattester.ui.theme.FONTSIZE_XXL
import com.example.mobileattester.ui.theme.Ok
import com.example.mobileattester.ui.util.parseBaseUrl
import com.example.mobileattester.ui.viewmodel.AttestationViewModel
import compose.icons.TablerIcons
import compose.icons.tablericons.Checkbox
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
    result: ElementResult? = null,
) {
    if (resultFlow != null && result != null) {
        throw Error("Provide exactly one result parameter")
    }
    if (resultFlow == null && result == null) {
        throw Error("Provide exactly one result parameter")
    }

    // --------------------------------------------------

    result?.let {
        Result(result)
        return
    }


    // --------------------------------------------------

    val res = resultFlow?.collectAsState()?.value
    when (res?.status) {
        Status.LOADING -> LoadingFullScreen()
        Status.ERROR -> ErrorIndicator(msg = "Error loading result")
        Status.SUCCESS -> {
            FadeInWithDelay(50) {
                Result(res.data!!)
            }
            return
        }
    }


    // --------------------------------------------------

    navController.currentBackStackEntry?.arguments?.getString(ARG_RESULT_ID)?.let { id ->
        viewModel.findElementResult(id)?.let {
            Result(result = it)
            return
        }
    }

    FadeInWithDelay(2000) {
        Text(text = "Result data not found")
    }
}


@Composable
fun Result(
    result: ElementResult,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ResultIcon(result)
            Spacer(modifier = Modifier.size(8.dp))
            Text(text = "Result", fontSize = FONTSIZE_XXL)
        }
        SpacerSmall()
        TextVertSpace(txt = "Result: ${result.result}")
        TextVertSpace(txt = "Message: ${result.ruleName}")
        TextVertSpace(txt = "Message: ${result.message}")
        TextVertSpace(txt = "Verified at: ${result.verifiedAt}")
        SpacerSmall()
        SpacerSmall()
        TextVertSpace(txt = "ElementId: ${result.elementID}")
        TextVertSpace(txt = "PolicyId: ${result.policyID}")
        TextVertSpace(txt = "ClaimId: ${result.claimID}")
    }
}


@Composable
fun ResultIcon(
    result: ElementResult,
) {
    when (result.result) {
        CODE_RESULT_OK -> Icon(
            TablerIcons.Checkbox,
            contentDescription = null,
            tint = Ok,
            modifier = Modifier.size(40.dp),
        )
        else -> Icon(
            TablerIcons.QuestionMark,
            contentDescription = null,
            tint = DarkGrey,
            modifier = Modifier.size(40.dp),
        )
    }
}

@Composable
fun TextVertSpace(txt: String) {
    Text(modifier = Modifier.padding(vertical = 6.dp), text = txt)
}