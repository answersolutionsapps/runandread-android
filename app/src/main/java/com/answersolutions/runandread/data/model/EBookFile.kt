package com.answersolutions.runandread.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TextPart(
    @SerialName("start_time_ms")
    val startTimeMms: Int,
    val text: String
)

data class EBookFile(
    val title: String,
    val author: String,
    val content: List<String>,

    val audioPath: String,
    val text: List<TextPart>,
    val language: String,
    val rate: Float,
    val voice: String,
    val model: String,
    val bookSource: String
)