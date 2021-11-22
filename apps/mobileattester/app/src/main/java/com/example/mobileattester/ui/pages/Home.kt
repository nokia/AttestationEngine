package com.example.mobileattester.ui.pages

import android.net.InetAddresses
import android.os.Build
import android.util.Patterns
import android.widget.Toast
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.mobileattester.ui.components.common.LoadingIndicator
import com.example.mobileattester.ui.theme.*
import com.example.mobileattester.ui.util.Preferences
import com.example.mobileattester.ui.util.Screen
import com.example.mobileattester.ui.util.parseBaseUrl
import com.example.mobileattester.ui.viewmodel.AttestationViewModel
import compose.icons.TablerIcons
import compose.icons.tablericons.*
import kotlinx.coroutines.launch


@Composable
fun Home(navController: NavController? = null, viewModel: AttestationViewModel) {
    val context = LocalContext.current
    val currentUrl = viewModel.currentUrl.collectAsState()
    val currentEngine = parseBaseUrl(currentUrl.value)

    val preferences = Preferences(LocalContext.current)
    val list = preferences.engines.collectAsState(initial = sortedSetOf<String>())

    if (list.value.isNotEmpty() && !list.value.contains(currentEngine)) viewModel.switchBaseUrl("http://${list.value.first()}/")

    var showAllConfigurations by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val scrollState = ScrollState(0)


    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Primary)
            .border(0.dp, Color.Transparent)
            .verticalScroll(scrollState),
    ) {
        // Top Bar
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
                .border(0.dp, Color.Transparent),
        ) {

            Text(
                text = "Current Configuration",
                modifier = Modifier.padding(0.dp, 15.dp, 0.dp, 5.dp),
                fontSize = FONTSIZE_XXL,
                color = Color.White
            )

            // Current Engine
            ConfigurationButton(text = currentEngine,
                name = "Engine",
                icon = TablerIcons.AdjustmentsHorizontal,
                onClick = {
                    showAllConfigurations = !showAllConfigurations
                })

            if (showAllConfigurations) {
                (list.value.filter { it != currentEngine }).forEach { engineAddress ->
                    ConfigurationButton(
                        text = engineAddress,
                        onClick = {
                            viewModel.switchBaseUrl("http://${it}/")

                            // Refresh
                            showAllConfigurations = false
                        },
                        onIconClick = {
                            list.value.remove(it)

                            scope.launch {
                                preferences.saveEngines(list.value.toSortedSet())

                                // Refresh
                                showAllConfigurations = false
                                showAllConfigurations = true
                            }

                        },
                    )
                }


                ConfigurationButton(text = "Ipaddress:port",
                    name = "",
                    icon = TablerIcons.Plus,
                    editable = true,
                    onIconClick = { str ->
                        val port = str.takeLastWhile { it != ':' }
                        val validPort = port.toUShortOrNull() != null

                        val address = str.dropLast(port.length + 1)

                        val validAddress = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            InetAddresses.isNumericAddress(address) // Todo: DNS address resolution
                        } else Patterns.IP_ADDRESS.matcher(address).matches()

                        if (validAddress && validPort) {
                            list.value.add(str)

                            scope.launch {
                                preferences.saveEngines(list.value)

                                // Refresh
                                showAllConfigurations = false
                                showAllConfigurations = true
                            }
                        } else {
                            Toast.makeText(
                                context,
                                "${if (!validAddress) "Address" else "Port"} is invalid",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    })
            }
        }
        // Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(5, 5, 0, 0))
                .background(Color.White)
        ) {
            Column(Modifier.padding(4.dp)) {
                Content(navController, viewModel)
            }
        }


    }
}

@Composable
fun ConfigurationButton(
    text: String,
    icon: ImageVector = TablerIcons.X,
    name: String = "History",
    editable: Boolean = false,
    onClick: (String) -> Unit = {},
    onTextChange: (String) -> Unit = {},
    onIconClick: (String) -> Unit = {},
) {
    Button(
        modifier = Modifier
            .fillMaxWidth()
            .padding(0.dp, 5.dp),
        onClick = { onClick(text) },
        colors = ButtonDefaults.buttonColors(
            backgroundColor = Color.Transparent,
            contentColor = Color.White
        ),
        elevation = null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(0.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            var input by remember { mutableStateOf("") }
            Column() {
                if (!editable) {
                    Text(name)
                    Text(text)
                } else OutlinedTextField(
                    value = input,
                    label = { Text(text) },
                    onValueChange = { input = it; onTextChange(input) },
                    singleLine = true,
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        unfocusedLabelColor = Color.White, // TODO: MaterialTheme.colors.primary
                        focusedLabelColor = Color.White,
                        unfocusedBorderColor = Color.White,
                        focusedBorderColor = Color.White,
                    )
                )
            }

            IconButton(onClick = { if (!editable) onIconClick(text) else onIconClick(input) }) {
                Icon(imageVector = icon, null)
            }
        }
    }
}

@Composable
fun Content(navController: NavController? = null, viewModel: AttestationViewModel) {
    val elementCount = viewModel.elementCount.collectAsState()
    val refreshing = viewModel.isRefreshing.collectAsState()

    Row(
        modifier = Modifier
            .padding(15.dp)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row() {
            Icon(
                TablerIcons.DeviceDesktop,
                contentDescription = null,
                modifier = Modifier
                    .padding(5.dp, 0.dp)
                    .align(Alignment.CenterVertically)
                    .size(25.dp)
            )
            Text(
                "System Devices",
                modifier = Modifier
                    .padding(5.dp, 0.dp)
                    .align(Alignment.CenterVertically),
                fontSize = 18.sp
            )
        }

        if (refreshing.value) {
            LoadingIndicator()
        } else {
            Text(
                AnnotatedString(elementCount.value.toString()),
                modifier = Modifier
                    .padding(5.dp, 0.dp)
                    .align(Alignment.CenterVertically)
                    .fillMaxWidth(),
                textAlign = TextAlign.End,
                fontSize = 24.sp
            )
        }
    }

    Text(
        text = "Attestation Overview",
        modifier = Modifier
            .fillMaxWidth()
            .padding(0.dp, 15.dp, 0.dp, 5.dp),
        textAlign = TextAlign.Center,
        fontSize = FONTSIZE_XXL
    )
    Spacer(modifier = Modifier.size(10.dp))
    Alert("24h") { navController!!.navigate(Screen.Elements.route) }
    Spacer(modifier = Modifier.size(20.dp))
    Alert("Past week") { navController!!.navigate(Screen.Elements.route) }

    Spacer(modifier = Modifier.size(200.dp)) // TODO: Change layout to be similar to Elements page
}

@Composable
fun Alert(
    alertDurationInfo: String = "",
    accepted: Int = 0,
    failed: Int = 0,
    onClick: () -> Unit = {},
) {
    Text(
        text = alertDurationInfo,
        modifier = Modifier.padding(10.dp, 5.dp),
        fontSize = FONTSIZE_XL
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        Arrangement.SpaceBetween,
        Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(text = "Verified Attestations", color = Primary)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    TablerIcons.ListSearch,
                    contentDescription = null,
                    tint = Primary,
                    modifier = Modifier.size(28.dp),
                )
                Text(
                    (accepted + failed).toString(),
                    color = Primary,
                    modifier = Modifier.padding(5.dp, 0.dp),
                    fontSize = FONTSIZE_LG,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
        Column(modifier = Modifier.padding(10.dp)) {
            Text(text = "Accepted", color = Ok)
            Row {
                Icon(TablerIcons.SquareCheck, contentDescription = null, tint = Ok)
                Text(
                    accepted.toString(),
                    color = Ok,
                    modifier = Modifier.padding(5.dp, 0.dp),
                    fontSize = FONTSIZE_LG,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
        Column(modifier = Modifier.padding(10.dp)) {
            Text(text = "Failed", color = Error)
            Row {
                Icon(TablerIcons.SquareX, contentDescription = null, tint = Error)
                Text(
                    failed.toString(),
                    color = Error,
                    modifier = Modifier.padding(5.dp, 0.dp),
                    fontSize = FONTSIZE_LG,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
        Column(
            modifier = Modifier
                .padding(10.dp)
                .align(Alignment.CenterVertically)
        ) {
            Icon(TablerIcons.ChevronRight, contentDescription = null)
        }
    }
}