package com.example.mobileattester.ui.util

// Removes https & other excess from the url
// baseUrl: http://192.168.0.1:4050/
// result: 192.168.0.1:4050
fun parseBaseUrl(baseUrl : String) : String
{
    var nUrl = baseUrl.dropWhile { c -> c != '/' && !c.isDigit() }
    if(!nUrl.first().isDigit())
        nUrl = nUrl.dropWhile { c -> c == '/' }
    return nUrl.takeWhile { c -> c != '/' }
}

private object Epoch {
    private const val hourInSeconds = 3600
    private const val dayInSeconds = hourInSeconds * 24
    private const val weekInSeconds = dayInSeconds * 7
    private const val monthInSeconds = dayInSeconds * 30.436840278
    private const val yearInSeconds = dayInSeconds * 365.242199074
}