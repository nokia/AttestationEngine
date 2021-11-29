package com.example.mobileattester.ui.util

import java.text.SimpleDateFormat
import java.util.*

sealed class DatePattern(val str: String) {
    object DateTime : DatePattern(str = "dd-M-yyyy hh:mm:ss")
    object DateOnly : DatePattern(str = "dd.MM.")
    object DateWithYear : DatePattern(str = "dd.MM.yy")
    object TimeOnly : DatePattern(str = "hh:mm:ss")
}

fun getTimeFormatted(timeStr: String, pattern: DatePattern): String {
    return SimpleDateFormat(pattern.str, Locale.getDefault()).format(timeStr.toFloat() * 1000L)
}