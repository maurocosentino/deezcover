package com.mauro.deezcover.data.utils

fun List<String?>.bestUrl(): String =
    firstOrNull { !it.isNullOrBlank() }.orEmpty()
