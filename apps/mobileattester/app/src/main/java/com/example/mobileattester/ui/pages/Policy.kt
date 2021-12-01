package com.example.mobileattester.ui.pages

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.mobileattester.ui.components.anim.FadeInWithDelay
import com.example.mobileattester.ui.components.common.HeaderRoundedBottom
import com.example.mobileattester.ui.components.common.TextWithSmallHeader
import com.example.mobileattester.ui.theme.FONTSIZE_XXL
import com.example.mobileattester.ui.theme.Primary
import com.example.mobileattester.ui.theme.White
import com.example.mobileattester.ui.util.formatted
import com.example.mobileattester.ui.viewmodel.AttestationViewModel
import compose.icons.TablerIcons
import compose.icons.tablericons.QuestionMark

const val ARG_POLICY_ID = "arg_policy_id"


@Composable
fun Policy(
    navController: NavController,
    viewModel: AttestationViewModel,
) {
    val policyId = navController.currentBackStackEntry?.arguments?.getString(ARG_POLICY_ID) ?: ""
    val policy = viewModel.getPolicyFromCache(policyId)

    if (policy == null) {
        Text(text = "Error")
    } else {

        FadeInWithDelay(0) {
            Column(Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())) {
                HeaderRoundedBottom {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 24.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            TablerIcons.QuestionMark,
                            contentDescription = null,
                            tint = White,
                            modifier = Modifier.size(40.dp),
                        )
                        Spacer(modifier = Modifier.size(8.dp))
                        Text(
                            text = "Policy",
                            color = White,
                            fontSize = FONTSIZE_XXL,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
                Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
                    TextWithSmallHeader(text = policy.name, header = "Name", c = Primary)
                    TextWithSmallHeader(text = policy.intent, header = "Intent", c = Primary)
                    policy.description?.let {
                        TextWithSmallHeader(text = it, header = "Description", c = Primary)
                    }
                    TextWithSmallHeader(
                        text = policy.parameters.formatted(),
                        header = "Intent",
                        c = Primary,
                    )
                }
            }
        }
    }
}
