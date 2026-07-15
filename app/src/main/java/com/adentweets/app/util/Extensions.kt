package com.adentweets.app.util

import android.content.Context
import android.text.format.DateUtils
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun Long.toRelativeTime(): String {
    val now = System.currentTimeMillis()
    val diff = now - this
    return when {
        diff < 60_000 -> "${diff / 1000}s"
        diff < 3_600_000 -> "${diff / 60_000}m"
        diff < 86_400_000 -> "${diff / 3_600_000}h"
        diff < 604_800_000 -> "${diff / 86_400_000}d"
        else -> {
            val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            sdf.format(Date(this))
        }
    }
}

fun Long.toFormattedDate(context: Context): String {
    return DateUtils.getRelativeTimeSpanString(
        this, System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS,
        DateUtils.FORMAT_ABBREV_RELATIVE
    ).toString()
}

fun String.isValidEmail(): Boolean {
    return android.util.Patterns.EMAIL_ADDRESS.matcher(this).matches()
}

fun String.isValidUsername(): Boolean {
    return matches(Regex("^[a-zA-Z0-9_]{3,15}$"))
}

fun String.isStrongPassword(): Boolean {
    return length >= 8 &&
            any { it.isUpperCase() } &&
            any { it.isLowerCase() } &&
            any { it.isDigit() }
}

fun String.toBase64(): String {
    return android.util.Base64.encodeToString(
        toByteArray(Charsets.UTF_8),
        android.util.Base64.NO_WRAP
    )
}

fun String.fromBase64(): String {
    return String(
        android.util.Base64.decode(this, android.util.Base64.NO_WRAP),
        Charsets.UTF_8
    )
}

fun String.mentions(): List<String> {
    val regex = Regex("@(\\w+)")
    return regex.findAll(this).map { it.value }.distinct().toList()
}

fun String.hashtags(): List<String> {
    val regex = Regex("#(\\w+)")
    return regex.findAll(this).map { it.value }.distinct().toList()
}

fun String.truncate(maxLength: Int): String {
    return if (length > maxLength) take(maxLength - 3) + "..." else this
}

fun String.capitalizeFirst(): String {
    return replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
}