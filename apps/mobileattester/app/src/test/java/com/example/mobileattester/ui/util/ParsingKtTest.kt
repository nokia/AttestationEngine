package com.example.mobileattester.ui.util

import org.junit.Assert
import org.junit.Test

 class ParsingKtTest {
    @Test
    fun testParseBaseUrl1() {
        Assert.assertEquals(parseBaseUrl("http://192.168.0.1:4050/"), "192.168.0.1:4050")
    }
}