package com.answersolutions.extensions

import android.annotation.SuppressLint
import android.telephony.PhoneNumberUtils
import java.text.SimpleDateFormat
import java.util.*

fun String.toKiloInt32(): Int {
    val number = this.replace("K", "")
    return 1000 * (number.toIntOrNull() ?: 0)
}

fun String.normalizeSpeaker() : String {
    return try {
        val number = this.split("_").last()
        "Speaker_${number.toInt() + 1}"
    } catch (e: Exception) {
        "Speaker"
    }
}

fun String.toFormattedTimeWithSec(): String = toLong().let { rawTimestamp ->
    val formatter = SimpleDateFormat("mm:ss")
    formatter.format(Date(rawTimestamp))
}

fun String.toFormattedTime(): String = toLong().let { rawTimestamp ->
    val formatter = SimpleDateFormat("HH:mm")
    formatter.format(Date(rawTimestamp))
}

fun String.toFormattedDate(): String = toLong().let { rawTimestamp ->
    val formatter = SimpleDateFormat("MMM dd")
    formatter.format(Date(rawTimestamp))
}

fun String.toFormattedTimeEpoch(): String = toLong().let { rawTimestamp ->
    val formatter = SimpleDateFormat("HH:mm")
    formatter.format(Date(rawTimestamp * 1000))
}

fun String.toFormattedDateTime(): String = toLong().let { rawTimestamp ->
    val formatter = SimpleDateFormat("MMM dd, hh:mm a")
    formatter.format(Date(rawTimestamp * 1000))
}

fun String.toFormattedDateEpoch(): String = toLong().let { rawTimestamp ->
    val formatter = SimpleDateFormat("MMM dd")
    formatter.format(Date(rawTimestamp * 1000))
}

fun String.normalizePhoneNumber(): String {
    return PhoneNumberUtils.normalizeNumber(this).replace("+", "")
}

@SuppressLint("SimpleDateFormat")
fun String?.formatSecondsToHMS(): String {
    this?.let {
        val seconds = it.toFloat() * 1000L
        val d = Date(seconds.toLong())
        return if (it.toFloat() < 3600) {
            val df = SimpleDateFormat("mm:ss") // HH for 0-23
            df.timeZone = TimeZone.getTimeZone("GMT")
            df.format(d)
        } else {
            val df = SimpleDateFormat("HH:mm:ss") // HH for 0-23
            df.timeZone = TimeZone.getTimeZone("GMT")
            df.format(d)
        }
    }?: run {
       return "00:00"
    }
}