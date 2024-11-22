package com.rwa.tellme.utils

import android.text.format.DateFormat
import java.text.SimpleDateFormat
import java.util.Locale

fun formatDateToLocale(createdAt: String): String {
    val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
    val date = format.parse(createdAt)

    return if (date != null) {
        val dayOfWeek = DateFormat.format("EEEE", date)
        val dateFormat = SimpleDateFormat("dd MMMM yyyy HH:mm:ss", Locale.getDefault())
        "$dayOfWeek, ${dateFormat.format(date)}"
    } else {
        "Invalid Date"
    }
}