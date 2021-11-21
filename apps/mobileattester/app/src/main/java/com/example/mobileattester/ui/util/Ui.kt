package com.example.mobileattester.ui.util

import androidx.compose.ui.Modifier

fun Modifier.`if`(
    condition: Boolean,
    then: Modifier.() -> Modifier
): Modifier =
    if (condition) {
        then()
    } else {
        this
    }