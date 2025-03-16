package com.answersolutions.extensions

import java.util.Locale
import java.util.concurrent.TimeUnit

fun Double?.formatSecondsToHMS(): String {
    if (this == null || this <= 0) return "00:00"
    if (this.isNaN()) {
        return "00:00"
    }

    val totalSeconds = this.toLong()
    val hours = TimeUnit.SECONDS.toHours(totalSeconds)
    val minutes = TimeUnit.SECONDS.toMinutes(totalSeconds) % 60
    val seconds = totalSeconds % 60

    return if (hours > 0) {
        String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format(Locale.getDefault(),"%02d:%02d", minutes, seconds)
    }
}

fun Double.toFormattedDateTime(): String = this.toLong().toFormattedDateTime()

//fun Double?.formatSecondsToHMS(): String {
//    if (this == null || this <= 0) return "00:00"
//
//    val totalSeconds = this.toLong()
//    val hours = totalSeconds / 3600
//    val minutes = (totalSeconds % 3600) / 60
//    val seconds = totalSeconds % 60
//
//    return if (hours > 0) {
//        String.format("%d:%02d:%02d", hours, minutes, seconds) // HH:mm:ss
//    } else {
//        String.format("%02d:%02d", minutes, seconds) // mm:ss
//    }
//}
