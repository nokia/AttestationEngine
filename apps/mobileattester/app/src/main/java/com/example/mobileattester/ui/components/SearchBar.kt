package com.example.mobileattester.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mobileattester.ui.theme.*

@Composable
fun SearchBar(
    state: MutableState<TextFieldValue>,
    placeholder: String? = null,
    onValueChange: ((TextFieldValue) -> Unit)? = null,
) {
    Surface(color = White,
        modifier = Modifier.padding(24.dp),
        elevation = ELEVATION_MD,
        shape = ROUNDED_MD) {
        TextField(value = state.value,
            onValueChange = { value ->
                onValueChange?.invoke(value)
                state.value = value
            },
            modifier = Modifier
                .fillMaxWidth()
                .clip(ROUNDED_MD),
            textStyle = TextStyle(fontSize = 18.sp),
            leadingIcon = {
                Icon(Icons.Default.Search,
                    contentDescription = "",
                    modifier = Modifier
                        .padding(15.dp)
                        .size(24.dp))
            },
            trailingIcon = {
                if (state.value != TextFieldValue("")) {
                    IconButton(onClick = {
                        state.value = TextFieldValue("")
                    }) {
                        Icon(Icons.Default.Close,
                            contentDescription = "",
                            modifier = Modifier
                                .padding(15.dp)
                                .size(24.dp))
                    }
                }
            },
            singleLine = true,
            shape = ROUNDED_MD,
            colors = TextFieldDefaults.textFieldColors(textColor = DarkGrey,
                cursorColor = DarkGrey,
                leadingIconColor = DarkGrey,
                trailingIconColor = DarkGrey,
                backgroundColor = Color.White,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent,
                placeholderColor = LightGrey),
            placeholder = { Text(text = placeholder ?: "") })
    }
}
