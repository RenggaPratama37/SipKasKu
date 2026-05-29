package com.renium.sipkasku.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun formatDate(
    timestamp: Long
): String {

    val formatter = SimpleDateFormat(
        "dd MMM yyyy",
        Locale("id", "ID")
    )

    return formatter.format(
        Date(timestamp)
    )
}
