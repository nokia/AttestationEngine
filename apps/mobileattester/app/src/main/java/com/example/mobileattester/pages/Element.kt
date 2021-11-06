package com.example.mobileattester.pages

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview

class Element : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { One()
        }
    }
}

    @Composable
    @Preview
    fun One() {
        val context = LocalContext.current
        val intent = (context as Element).intent
        val id = intent.getStringExtra("ID")
        Text("Element ID: $id")
    }
