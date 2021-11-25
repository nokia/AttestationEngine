package com.example.mobileattester.ui.util

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.*

const val CONFIG = "Config"

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = CONFIG)

class Preferences(
    private val context: Context,
) {
    // to make sure there's only one instance
    companion object {
        private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(CONFIG)
        private val enginesKey = stringSetPreferencesKey("addresses")

        val defaultConfig = mutableSetOf("0.0.0.0:8520")
    }

    // Access set of saved configs?
    val engines: Flow<SortedSet<String>> by lazy {
        context.dataStore.data.map { preferences ->
            if (preferences[enginesKey] == null || preferences[enginesKey]!!.isEmpty()) {
                defaultConfig.toSortedSet()
            } else {
                preferences[enginesKey]!!.ifEmpty { defaultConfig }.toSortedSet()
            }
        }
    }

    suspend fun saveEngines(engines: SortedSet<String>) {
        context.dataStore.edit { preferences ->
            preferences[enginesKey] = engines
        }
    }
}

