package com.answersolutions.extensions

fun Double.toFormattedDateTime(): String = this.toLong().toFormattedDateTime()

fun Double?.formatSecondsToHMS(): String {
    if (this == null || this <= 0) return "00:00"

    val totalSeconds = this.toLong()
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60

    return if (hours > 0) {
        String.format("%d:%02d:%02d", hours, minutes, seconds) // HH:mm:ss
    } else {
        String.format("%02d:%02d", minutes, seconds) // mm:ss
    }
}
