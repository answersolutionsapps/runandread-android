package com.answersolutions.extensions

import java.text.SimpleDateFormat
import java.util.*

fun Long.toFormattedDateTime(): String = this.let { rawTimestamp ->
    val formatter = SimpleDateFormat("MMM dd, hh:mm a")
    formatter.format(Date(rawTimestamp * 1000))
}

fun Long.formatSecondsToHMS(): String {
    //String.format(Locale.ROOT, "%02d:%02d", mins, secs)
    val seconds = this * 1000L
    val d = Date(seconds.toLong())
    return if (this < 3600) {
        val df = SimpleDateFormat("mm:ss") // HH for 0-23
        df.timeZone = TimeZone.getTimeZone("GMT")
        df.format(d)
    } else {
        val df = SimpleDateFormat("HH:mm:ss") // HH for 0-23
        df.timeZone = TimeZone.getTimeZone("GMT")
        df.format(d)
    }
}