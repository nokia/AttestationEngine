package com.example.mobileattester.pages

import android.net.InetAddresses
import android.os.Build
import android.util.Patterns
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.mobileattester.data.network.Response
import com.example.mobileattester.data.network.Status
import com.example.mobileattester.ui.util.Preferences
import com.example.mobileattester.ui.viewmodel.AttestationViewModel
import compose.icons.TablerIcons
import compose.icons.tablericons.AdjustmentsHorizontal
import compose.icons.tablericons.Plus
import compose.icons.tablericons.X
import kotlinx.coroutines.launch


@Composable
fun Home(navController: NavController? = null, viewModel: AttestationViewModel) {
    val ids: Response<List<String>> by viewModel.getElementIds()
        .observeAsState(Response(status = Status.LOADING))

    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(13, 110, 253))
            .border(0.dp, Color.Transparent),
    )
    {
        // Top Bar
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
                .border(0.dp, Color.Transparent),
        )
        {
            val preferences = Preferences(LocalContext.current)
            val list =
                preferences.engines.collectAsState(initial = sortedSetOf(Preferences.currentEngine))
            var showAllConfigurations by remember { mutableStateOf(false) }

            val scope = rememberCoroutineScope()

            Text(
                text = AnnotatedString(
                    "Current Configuration",
                    SpanStyle(Color.White, fontSize = 24.sp)
                ), modifier = Modifier.padding(0.dp, 15.dp, 0.dp, 5.dp)
            )

            // Current Engine
            ConfigurationButton(
                text = list.value.first { it == Preferences.currentEngine },
                name = "Engine",
                icon = TablerIcons.AdjustmentsHorizontal,
                onClick = {
                    showAllConfigurations = !showAllConfigurations
                })


            if (showAllConfigurations) {
                list.value
                    .filter { it != Preferences.currentEngine }
                    .forEach { engineAddress ->
                        ConfigurationButton(text = engineAddress,
                            onClick =
                            {
                                Preferences.currentEngine = it

                                // Refresh
                                showAllConfigurations = false
                                showAllConfigurations = true
                            },
                            onIconClick = {
                                list.value.remove(it)

                                scope.launch {
                                    preferences.saveEngines(list.value)

                                    // Refresh
                                    showAllConfigurations = false
                                    showAllConfigurations = true
                                }

                            }
                        )
                    }

                ConfigurationButton(text = "Ipaddress:port",
                    name = "",
                    icon = TablerIcons.Plus,
                    editable = true,
                    onIconClick = {
                        val port = it.takeLastWhile { it != ':' }
                        val validPort = port.toUShortOrNull() != null

                        val address = it.dropLast(port.length + 1)

                        val validAddress = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            InetAddresses.isNumericAddress(address) // Todo: DNS address resolution
                        } else
                            Patterns.IP_ADDRESS.matcher(address).matches()

                        if (validAddress && validPort) {
                            list.value.add(it)

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
                    }
                )
            }
        }
        // Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(5, 5, 0, 0))
                .background(Color.White)
        ) {
            Text(text = ids.data?.reduce { a, b -> a + b } ?: ids.message
            ?: "Data not received for some reason")
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
                } else
                    OutlinedTextField(
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