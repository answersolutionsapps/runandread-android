package com.answersolutions.runandread.data.datasource

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ActivityScoped
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@ActivityScoped
class PrefsStoreImpl @Inject constructor(@ApplicationContext private val context: Context) :
    PrefsStore {
    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
        name = STORE_NAME
    )

    override suspend fun saveSelectedLanguages(recent: List<String>) {
        context.dataStore.edit {
            it[RECENT_LANGUAGE_SELECTIONS] = recent.toSet()
        }
    }

    override fun selectedLanguages(): Flow<List<String>> {
        return context.dataStore.data.map {
            it[RECENT_LANGUAGE_SELECTIONS]?.toList() ?: emptyList()
        }
    }

    companion object {
        private const val STORE_NAME = "data_store"
        val RECENT_LANGUAGE_SELECTIONS = stringSetPreferencesKey("RECENT_LANGUAGE_SELECTIONS")
    }
}