package com.mauro.offlinefirst.data.utils

fun List<String?>.bestUrl(): String =
    firstOrNull { !it.isNullOrBlank() }.orEmpty()
