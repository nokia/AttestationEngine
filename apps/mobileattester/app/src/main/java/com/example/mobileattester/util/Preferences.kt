package com.example.mobileattester.util

import android.content.Context
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = "Config"
)

class Preferences(private val context: Context) {

    // to make sure there's only one instance
    companion object {
        private val Context.dataStore: DataStore<Preferences> by preferencesDataStore("Config")
        private val enginesKey = stringSetPreferencesKey("addresses")

        var currentEngine : String = "127.0.0.1:1883"
    }


    public val engines: Flow<Set<String>> by lazy {
        context.dataStore.data
        .map { preferences ->
            if(preferences[enginesKey] == null || preferences[enginesKey]!!.isEmpty())
                mutableSetOf("127.0.0.1:1883")
            else
                preferences[enginesKey]!!
        }
    }

    public suspend fun saveEngines(engines : Set<String>) {
        context.dataStore.edit { preferences ->
            preferences[enginesKey] = engines
        }
    }
}