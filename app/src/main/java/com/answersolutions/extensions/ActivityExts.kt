package com.answersolutions.extensions

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import androidx.core.content.FileProvider
import timber.log.Timber
import java.io.File



fun Context.pathToRecordedFile(name: String): String {
    val location = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        Environment.DIRECTORY_RECORDINGS
    } else {
        Environment.DIRECTORY_DOCUMENTS
    }
    getExternalFilesDir(location)?.let { folder ->
        val file = File(folder.absolutePath)
        val mkdir = if (!file.exists()) {
            file.mkdir()
        } else true
        if (mkdir) {
            return "${folder.absolutePath}${File.separator}$name"
        }
    }
    return "/"
}

fun Context.deleteRecordedFile(name: String): Boolean {
    val location = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        Environment.DIRECTORY_RECORDINGS
    } else {
        Environment.DIRECTORY_DOCUMENTS
    }
    try {
        getExternalFilesDir(location)?.let { folder ->
            val file = File("${folder.absolutePath}${File.separator}$name")
            file.delete()
            return true
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return false
}

fun Context.pathToTranscriptions(name: String): String {
    val location = Environment.DIRECTORY_DOCUMENTS
    getExternalFilesDir(location)?.let { folder ->
        val file = File(folder.absolutePath)
        val mkdir = if (!file.exists()) {
            file.mkdir()
        } else true
        if (mkdir) {
            return "${folder.absolutePath}${File.separator}$name"
        }
    }
    return "/"
}

fun Context.deleteTranscriptions(name: String?): Boolean {
    name?.let {
        val location = Environment.DIRECTORY_DOCUMENTS
        try {
            getExternalFilesDir(location)?.let { folder ->
                val file = File("${folder.absolutePath}${File.separator}$it")
                file.delete()
                return true
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    return false
}

fun Context.getTranscriptionFilesCount(): Int {
    val location = Environment.DIRECTORY_DOCUMENTS
    getExternalFilesDir(location)?.listFiles()?.let {
        return it.count { file ->
            file.extension.endsWith("json")
        }
    }
    return 0
}

fun Context.getAudioFilesCount(): Int {
    val location = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        Environment.DIRECTORY_RECORDINGS
    } else {
        Environment.DIRECTORY_DOCUMENTS
    }
    getExternalFilesDir(location)?.listFiles()?.let {
        return it.count { file ->
            file.extension.endsWith("flac")
        }
    }
    return 0
}

fun Context.deleteAllFiles(): Int {
    val location = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        Environment.DIRECTORY_RECORDINGS
    } else {
        Environment.DIRECTORY_DOCUMENTS
    }
    getExternalFilesDir(location)?.listFiles()?.filter {
        it.extension.endsWith("flac")
    }?.forEach {
        it.delete()
    }
    val location2 = Environment.DIRECTORY_DOCUMENTS
    getExternalFilesDir(location2)?.listFiles()?.filter {
        it.extension.endsWith("json")
    }?.forEach {
        it.delete()
    }
    return 0
}

@SuppressLint("IntentReset")
fun Context.shareFileViaEmail(path: String, date: String) {
    try {
        val fileUri =
            FileProvider.getUriForFile(this, "${packageName}.fileprovider", File(path))
        Timber.d("shareViaEmail.fileUri=>${fileUri}")
        grantUriPermission(packageName, fileUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
        val utype = contentResolver.getType(fileUri)
        Timber.d("shareViaEmail.utype=>${utype}")
        val message =
            "This call is recorded with the 'A Call Recorder App',\n\n\nplease visit https://acallrecorder.com for more information."

        val emailIntent = Intent(Intent.ACTION_SEND).apply {
            type = "*/*"
            data = Uri.parse("mailto:")
            putExtra(Intent.EXTRA_SUBJECT, "A Call Recorded at: $date")
            putExtra(Intent.EXTRA_TEXT, message)
            putExtra(Intent.EXTRA_STREAM, fileUri)
            setDataAndType(fileUri, utype)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        startActivity(Intent.createChooser(emailIntent, "Share Recorded Call"))
    } catch (e: Exception) {
        Timber.e("is exception raises during sending mail${e.localizedMessage}")
    }
}

//@SuppressLint("IntentReset")
//fun Context.shareHTMLViaEmail(speechSegments: List<SpeechSegment>, date: String) {
//    try {
//        println("shareHTMLViaEmail=>${extractAsHTML(speechSegments)}")
//        val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
//            type = "message/rfc822"
//            data = Uri.parse("mailto:")
//            putExtra(Intent.EXTRA_SUBJECT, "A Call Recorded at: $date")
//            putExtra(
//                Intent.EXTRA_TEXT,
//                extractAsHTML(speechSegments)
//            )//Html.fromHtml(constructEmailContent(speechSegments), FROM_HTML_MODE_COMPACT))
//
////            putExtra(Intent.EXTRA_HTML_TEXT, Html.fromHtml(constructEmailContent(speechSegments), FROM_HTML_MODE_COMPACT))//Html.fromHtml(constructEmailContent(speechSegments), FROM_HTML_MODE_COMPACT))
//        }
//        startActivity(emailIntent)
//    } catch (e: Exception) {
//        e.printStackTrace()
//        println("is exception raises during sending mail$e")
//    }
//}
//
//fun extractAsHTML(speechSegments: List<SpeechSegment>): String {
//    val result = StringBuilder()
//    result.append("\n==Transcription Begins==========\n")
//    speechSegments.forEach {
//        if (it.SentimentIsNegative == 1) {
//            result.append("\n:( ")
//        } else if (it.SentimentIsPositive == 1) {
//            result.append("\n:) ")
//        } else {
//            result.append("\n:| ")
//        }
//        result.append("${it.SegmentSpeaker.normalizeSpeaker()} ")
//        result.append("${it.SegmentStartTime.formatSecondsToHMS()} ")
//        result.append("\n")
//        result.append("${it.DisplayText} ")
//        result.append("\n")
//    }
//    result.append("\n==Transcription Ends==========")
//    result.append("\nThis call is recorded and transcribed with the 'A Call Recorder App'")
//    result.append("\nFor more information please visit:")
//    result.append("\nhttps://acallrecorder.com")
//    result.append("\n")
//    return result.toString()
//}

//fun Context.copyToClipboard(speechSegments: List<String>) {
//    val text = extractAsHTML(speechSegments)
//    val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
//    val clip = ClipData.newPlainText("label", text)
//    clipboard.setPrimaryClip(clip)
//}