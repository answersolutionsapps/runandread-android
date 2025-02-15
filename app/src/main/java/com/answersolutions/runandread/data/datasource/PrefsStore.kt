package com.answersolutions.runandread.data.datasource

import kotlinx.coroutines.flow.Flow

interface PrefsStore {
    suspend fun saveSelectedLanguages(recent: List<String>)
    fun selectedLanguages(): Flow<List<String>>
}