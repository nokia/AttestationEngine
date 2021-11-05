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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.mobileattester.util.Preferences
import compose.icons.TablerIcons
import compose.icons.tablericons.AdjustmentsHorizontal
import compose.icons.tablericons.Plus
import compose.icons.tablericons.X
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch


@Composable
@Preview
fun Home(navController: NavController? = null) {
    val context = LocalContext.current
    Column(modifier = Modifier
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
            var list = preferences.engines.collectAsState(initial = setOf("1.1.1.1:20")).value.toMutableList()
            Preferences.currentEngine = list.first()
            var selectedIndex by remember{mutableStateOf(0)}

            var showAllConfigurations by remember { mutableStateOf(false) }

            val scope = rememberCoroutineScope()

            Text(text = AnnotatedString("Home", SpanStyle(Color.White, fontSize = 24.sp)))
            Text(text = AnnotatedString("Current Configuration", SpanStyle(Color.White, fontSize = 24.sp)), modifier = Modifier.padding(0.dp, 20.dp,0.dp,5.dp))

            ConfigurationButton(text = list[selectedIndex], name= "Engine",icon = TablerIcons.AdjustmentsHorizontal, onClick = {
                showAllConfigurations = !showAllConfigurations
            })

            if (showAllConfigurations) {
                list.filter { address -> address != Preferences.currentEngine }
                    .forEach { engineAddress ->
                        ConfigurationButton(text = engineAddress,
                            onClick =
                            {
                                showAllConfigurations = false
                                Preferences.currentEngine = it
                                selectedIndex = list.indexOf(Preferences.currentEngine)
                            },
                            onIconClick = {
                                list.remove(it)

                                scope.launch {
                                    preferences.saveEngines(list.toSet())

                                    // Refresh
                                    showAllConfigurations = false
                                    showAllConfigurations = true
                                }

                            }
                        )
                    }

                ConfigurationButton(text = "Ipaddress:port", name= "",icon = TablerIcons.Plus, editable=true,
                    onIconClick = {
                        val port = it.takeLastWhile { it != ':' }
                        val validPort = port.toIntOrNull() != null

                        val address = it.dropLast(port.length+1)

                        var validAddress = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            InetAddresses.isNumericAddress(address) // Todo: DNS address resolution
                        } else
                            Patterns.IP_ADDRESS.matcher(address).matches()

                        if(validAddress && validPort) {
                            list.add(it)

                            scope.launch {
                                preferences.saveEngines(list.toSet())

                                // Refresh
                                showAllConfigurations = false
                                showAllConfigurations = true
                            }
                        }
                        else {
                            Toast.makeText(context, "${if (!validAddress) "Address" else "Port"} is invalid", Toast.LENGTH_SHORT).show()
                        }
                    }
                )
            }
        }
        // Content
        Column(modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(5, 5, 0, 0))
            .background(Color.White)
        ){}
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
                if(!editable) {
                    Text(name)
                    Text(text)
                }
                else
                    OutlinedTextField(
                        value = input,
                        label = {Text(text)},
                        onValueChange = {input = it; onTextChange(input)},
                        singleLine = true,
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            unfocusedLabelColor = Color.White, // TODO: MaterialTheme.colors.primary
                            focusedLabelColor = Color.White,
                            unfocusedBorderColor = Color.White,
                            focusedBorderColor = Color.White,
                        )
                    )
            }

            IconButton(onClick = {if(!editable) onIconClick(text) else onIconClick(input)}) {
                Icon(imageVector = icon, null)
            }
        }
    }
}