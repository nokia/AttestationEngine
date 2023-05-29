package com.example.mobileattester.data.model

data class Spec(
    val title: String? = null,
    val swagger: String? = null,
    val info: EngineInfo? = null
)

data class EngineInfo(
    val title: String? = null,
    val description: String? = null,
    val version: String? = null
)