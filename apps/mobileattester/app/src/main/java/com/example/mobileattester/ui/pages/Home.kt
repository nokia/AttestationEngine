package com.example.mobileattester.ui.pages

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
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.mobileattester.ui.util.Preferences
import compose.icons.TablerIcons
import compose.icons.tablericons.AdjustmentsHorizontal
import compose.icons.tablericons.X


@Composable
@Preview
fun Home(navController: NavController? = null) {
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
            var list = Preferences.engines
            var selectedIndex by remember{mutableStateOf(list.indexOf(Preferences.currentEngine))}

            val composableScope = rememberCoroutineScope()
            var showAllConfigurations by remember { mutableStateOf(false) }

            Text(text = AnnotatedString("Home", SpanStyle(Color.White, fontSize = 24.sp)))
            Text(text = AnnotatedString("Current Configuration", SpanStyle(Color.White, fontSize = 24.sp)), modifier = Modifier.padding(0.dp, 20.dp,0.dp,5.dp))

            ConfigurationButton(text = list[selectedIndex], name= "Engine",icon = TablerIcons.AdjustmentsHorizontal, onClick = {
                showAllConfigurations = !showAllConfigurations
            })

            if (showAllConfigurations)
                list.filter { address -> address != Preferences.currentEngine }.forEach { engineAddress ->
                    ConfigurationButton(text = engineAddress,
                        onClick =
                        {
                            showAllConfigurations = false
                            Preferences.currentEngine = it
                            selectedIndex = list.indexOf(Preferences.currentEngine)
                        },
                        onIconClick = {
                            Preferences.engines.remove(it)
                            list.remove(it)

                            // Refresh
                            showAllConfigurations = false
                            showAllConfigurations = true
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
    onClick: (String) -> Unit = {},
    onIconClick: (String) -> Unit = {},
) {
    Button(
        modifier = Modifier.fillMaxWidth().padding(0.dp, 5.dp),
        onClick = { onClick(text) },
        colors = ButtonDefaults.buttonColors(
            backgroundColor = Color.Transparent,
            contentColor = Color.White
        ),
        elevation = null
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(0.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column() {
                Text(name)
                Text(text)
            }

            IconButton(onClick = {onIconClick(text)}) {
                Icon(imageVector = icon, null)
            }
        }
    }
}