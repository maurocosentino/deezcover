package com.mauro.deezcover.utils

import android.os.Build
import androidx.annotation.RequiresApi
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Locale

@RequiresApi(Build.VERSION_CODES.O)
private val InputDateFormatter: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE
@RequiresApi(Build.VERSION_CODES.O)
private val OutputDateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy", Locale.getDefault())

@RequiresApi(Build.VERSION_CODES.O)
fun formatDate(dateString: String?): String {
    if (dateString.isNullOrBlank()) return ""

    return try {
        LocalDate.parse(dateString, InputDateFormatter).format(OutputDateFormatter)
    } catch (_: DateTimeParseException) {
        dateString
    }
}
